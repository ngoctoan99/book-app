package com.sanghm2.bookapp

import android.app.AlertDialog
import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sanghm2.bookapp.databinding.ActivityPdfAddBinding

class PdfAddActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfAddBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private var pdfUri : Uri? = null
    private val TAG = "PDF_ADD_TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.categoyTv.setOnClickListener {
            categoryPickDialog()
        }
        binding.attackbtn.setOnClickListener {
            pdfPickIntent()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
    }
    private var title = ""
    private var description = ""
    private var category = ""
    private fun validateData() {
        Log.d(TAG, "validateData: calidating data")
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoyTv.text.toString().trim()

        if(title.isEmpty()){
            Toast.makeText(this, "Enter Title...",Toast.LENGTH_SHORT).show()
        }else if(description.isEmpty()){
            Toast.makeText(this, "Enter Description...",Toast.LENGTH_SHORT).show()
        }else if(category.isEmpty()){
            Toast.makeText(this, "Enter Category...",Toast.LENGTH_SHORT).show()
        }else if (pdfUri == null){
            Toast.makeText(this,"Add PDF...",Toast.LENGTH_SHORT).show()
        }
        else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()
        val timeStamp = System.currentTimeMillis()
        val filePathAndName  = "Books/$timeStamp"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!).addOnSuccessListener {taskSnapshot->
            Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url")
            val uriTask : Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadPdfUrl = "${uriTask.result}"
            uploadPdfInfoToDb(uploadPdfUrl, timeStamp)
        }.addOnFailureListener{
            Log.d(TAG,"uploadPdfToStorage: failed to upload due to ${it.message}")
            progressDialog.dismiss()
            Toast.makeText(this,"Failed to upload due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPdfInfoToDb(uploadPdfUrl: String, timeStamp: Long) {
            Log.d(TAG,"uploadPdfInfoToDb: uploading to do")
        progressDialog.setMessage("Uploading pdf info...")
        val uid = firebaseAuth.uid
        val hashMap : HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timeStamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadPdfUrl"
        hashMap["timestamp"] = timeStamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timeStamp").setValue(hashMap).addOnSuccessListener {
            Log.d(TAG,"uploadPdfInfoToDb: uploaded to db ")
            progressDialog.dismiss()
            Toast.makeText(this,"Uploaded...",Toast.LENGTH_SHORT).show()
            pdfUri = null
        }.addOnFailureListener {
            Log.d(TAG,"uploadPdfInfoToDb: failed to upload due to ${it.message}")
            progressDialog.dismiss()
            Toast.makeText(this,"Failed to upload due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPdfCategories() {
        Log.d(TAG , "loadPdfCategories: Loading pdf categories")
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog(){
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for(i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category").setItems(categoriesArray){dialog ,which ->
            selectedCategoryId = categoryArrayList[which].id
            selectedCategoryTitle = categoryArrayList[which].category
            binding.categoyTv.text = selectedCategoryTitle
            Log.d(TAG , "categoryPickDialog: Selected Category ID: $selectedCategoryId")
            Log.d(TAG , "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
        }.show()
    }
    private fun pdfPickIntent(){
        Log.d(TAG , "pdfIntent :  starting pdf pick intent")
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result->
            if(result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked: ")
                pdfUri = result.data!!.data
            }else {
                Log.d(TAG, "PDF Picked cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
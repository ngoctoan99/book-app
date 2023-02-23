package com.sanghm2.bookapp.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.databinding.ActivityPdfEditBinding

class PdfEditActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfEditBinding
    private var  bookId  = ""
    private lateinit var progressDialog : ProgressDialog
    private  lateinit var categoryTitleArrayList: ArrayList<String>
    private  lateinit var categoryIdArrayList: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId").toString()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.categoyTv.setOnClickListener {
            categoryDialog()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }
        loadCategories()
        loadBookInfo()
    }

    private fun loadBookInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                selectedCategoryId = "${snapshot.child("categoryId").value}"
                val title = "${snapshot.child("title").value}"
                val description = "${snapshot.child("description").value}"

                binding.titleEt.setText(title)
                binding.descriptionEt.setText(description)

                val ref  = FirebaseDatabase.getInstance().getReference("Categories")
                ref.child(selectedCategoryId).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val  category = "${snapshot.child("category").value}"
                        binding.categoyTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var title =""
    private var description = ""

    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        if(title.isEmpty()){
            Toast.makeText(this,"Enter Title",Toast.LENGTH_SHORT).show()
        }else if(description.isEmpty()){
            Toast.makeText(this,"Enter Description",Toast.LENGTH_SHORT).show()
        }else if(selectedCategoryId.isEmpty()){
            Toast.makeText(this,"Enter Category",Toast.LENGTH_SHORT).show()
        }else {
            updateInFoPDF()
        }
    }

    private fun updateInFoPDF() {
        progressDialog.setTitle("Uploading book info")
        progressDialog.show()
        val hashMap = HashMap<String , Any>()
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = selectedCategoryId
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).updateChildren(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            startActivity(Intent(this, DashboardAdminActivity::class.java))
            finish()
        }.addOnFailureListener{
            progressDialog.dismiss()
            Toast.makeText(this, "Failed Upload due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category").setItems(categoriesArray){dialog , position ->
            selectedCategoryId = categoryIdArrayList[position]
            selectedCategoryTitle = categoryTitleArrayList[position]
            binding.categoyTv.text = selectedCategoryTitle
        }.show()
    }

    private fun loadCategories() {
        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()
                for(ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category= "${ds.child("category").value}"
                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }
}
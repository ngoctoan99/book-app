package com.sanghm2.bookapp.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sanghm2.bookapp.MyApplication
import com.sanghm2.bookapp.R
import com.sanghm2.bookapp.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProfileEditBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        binding.profileIv.setOnClickListener{
            showImageAttachMenu()
        }
        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }
    private var name = ""
    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        if(name.isEmpty()){
            Toast.makeText(this ,"Enter name",Toast.LENGTH_SHORT).show()
        }else {
            if(imageUri == null){
                updateProfile("")
            }else {
                updateImage()
            }
        }
    }

    private fun updateImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()
        val filePathAndName = "ProfileImages/"+firebaseAuth.uid

        val ref = FirebaseStorage.getInstance().getReference(filePathAndName)
        ref.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
            val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadImageUrl = "${uriTask.result}"
            updateProfile(uploadImageUrl)
        }.addOnFailureListener{
            progressDialog.dismiss()
            Toast.makeText(this,"Failed upload image due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(uploadedImage : String) {
        progressDialog.setMessage("Uploading profile...")

        val hashMap : HashMap<String , Any> = HashMap()
        hashMap["name"] = "${name}"
        if(imageUri!= null){
            hashMap["profileImage"] = uploadedImage
        }
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).updateChildren(hashMap).addOnSuccessListener {
            progressDialog.show()
            Toast.makeText(this, "Profile uploaded",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed upload profile due to ${it.message}",Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = "${snapshot.child("name").value}"
                val profileImage = "${snapshot.child("profileImage").value}"
                val timestamp = "${snapshot.child("timestamp").value}"
                binding.nameEt.setText(name)
                try{
                    Glide.with(this@ProfileEditActivity).load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIv)
                }catch (e: Exception){
                    Glide.with(this@ProfileEditActivity).load(R.drawable.ic_person_gray).into(binding.profileIv)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    private fun showImageAttachMenu(){
        val popopMenu =PopupMenu(this,binding.profileIv)
        popopMenu.menu.add(Menu.NONE,0,0,"Camera")
        popopMenu.menu.add(Menu.NONE,1,1,"Gallery")

        popopMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if(id == 0){
                pickImageCamera()
            }else if(id == 1 ){
                pickImageStorage()
            }
            true
        }
    }

    private fun pickImageStorage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivitiResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivitiResultLauncher.launch(intent)
    }

    private val cameraActivitiResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ActivityResultCallback <ActivityResult>{result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                binding.profileIv.setImageURI(imageUri)
            }else {
                Toast.makeText(this, "Cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    )
    private val galleryActivitiResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ActivityResultCallback <ActivityResult>{result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                binding.profileIv.setImageURI(imageUri)
            }else {
                Toast.makeText(this, "Cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    )
}
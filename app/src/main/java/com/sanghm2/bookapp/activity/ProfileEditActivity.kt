package com.sanghm2.bookapp.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private companion object {
        private const val  CAMERA_REQUEST_CODE = 100
        private const val  STORAGE_REQUEST_CODE = 101
    }

    private var  imageUri : Uri? = null
    private lateinit var cameraPermission : Array<String>
    private lateinit var storagePermission : Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        binding.profileIv.setOnClickListener{
            showImageAttachMenu()
        }
        binding.updateBtn.setOnClickListener {
            validateData()
        }
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this@ProfileEditActivity, ProfileActivity::class.java))
            finish()
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
        progressDialog.show()
        val hashMap : HashMap<String , Any> = HashMap()
        hashMap["name"] = "$name"
        if(imageUri != null){
            hashMap["profileImage"] = uploadedImage
        }
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).updateChildren(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Profile uploaded",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,ProfileActivity::class.java))
            finish()
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
        popopMenu.show()
        popopMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if(id == 0){
                if(checkCameraPermission()){
                    pickImageCamera()
                }else {
                    requestCameraPermission()
                }
            }else if(id == 1){
                if(checkStoragePermission()){
                    pickImageStorage()
                }
                else {
                    requestStoragePermission()
                }
            }
            true
        }
    }

    private fun pickImageStorage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
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
        ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == Activity.RESULT_OK){
                binding.profileIv.setImageURI(null)
                binding.profileIv.setImageURI(imageUri)
            }else {
                Toast.makeText(this, "Cancelled",Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                binding.profileIv.setImageURI(imageUri)
            }else {
                Toast.makeText(this, "Cancelled",Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkStoragePermission() : Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    private fun checkCameraPermission(): Boolean {
        val cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return cameraResult && storageResult
    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission, STORAGE_REQUEST_CODE)
    }
    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(cameraAccepted && storageAccepted){
                        pickImageCamera()
                    }else {
                        showToast("Camera & Storage permission are required...")
                    }
                }
            }
            STORAGE_REQUEST_CODE->{
                if(grantResults.isNotEmpty()){
                    val storageAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if(storageAccepted){
                        pickImageStorage()
                    }else {
                        showToast("Storage permission are required...")
                    }
                }
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
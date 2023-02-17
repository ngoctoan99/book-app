package com.sanghm2.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sanghm2.bookapp.databinding.ActivityCategoryAddBinding

class CategoryAddActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCategoryAddBinding
    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var progressDialog : ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
        binding.submitBtn.setOnClickListener {
            validatePressed()
        }
    }
    private var category = ""
    private fun validatePressed() {
        category = binding.categoryEt.text.toString().trim()
        if(category.isEmpty()){
            Toast.makeText(this, "Enter Category..." ,Toast.LENGTH_SHORT).show()
        }else {
            addCategory()
        }
    }

    private fun addCategory() {
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String , Any?>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref  = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("${timestamp}").setValue(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Added successfully..." ,Toast.LENGTH_SHORT).show()
            startActivity(Intent(this , DashboardAdminActivity::class.java))
            finish()
        }.addOnFailureListener{
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to add due to  ${it.message}" ,Toast.LENGTH_SHORT).show()
        }
    }
}
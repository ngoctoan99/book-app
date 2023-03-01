package com.sanghm2.bookapp.activity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.sanghm2.bookapp.R
import com.sanghm2.bookapp.databinding.ActivityForgotPasswordBinding
import java.util.regex.Pattern

class ForgotPassword : AppCompatActivity() {
    private lateinit var binding : ActivityForgotPasswordBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.sendBtn.setOnClickListener {
            validateData()
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }
    private var email = ""
    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        if(email.isEmpty()){
            Toast.makeText(this , "Enter Email", Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalidate Email",Toast.LENGTH_SHORT).show()
        }else {
            recoverPassword()
        }
    }
    private fun recoverPassword() {
        progressDialog.setMessage("Sending reset password to $email")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Successfully recover password due to $email",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed recover password due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }
}
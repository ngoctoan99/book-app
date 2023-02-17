package com.sanghm2.bookapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sanghm2.bookapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityRegisterBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog =  ProgressDialog(this )
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.registerBtn.setOnClickListener{
            validateData()
        }
        binding.backBtn.setOnClickListener{
            onBackPressed();
        }
    }
    private var name = "" ;
    private var email = "" ;
    private var password = "" ;

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.confirmpasswordEt.text.toString().trim()
        if(name.isEmpty()){
            Toast.makeText(this, "Enter your name..." , Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Pattern..." , Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Enter your password..." , Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty()){
            Toast.makeText(this, "Comfirm password..." , Toast.LENGTH_SHORT).show()
        }
        else if (cPassword != password){
            Toast.makeText(this, "Password doesn't match..." , Toast.LENGTH_SHORT).show()
        }
        else {
            creatUserAccount()
        }
    }

    private fun creatUserAccount() {
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
            updateUserInfor()

        }.addOnFailureListener{
            progressDialog.dismiss()
            Toast.makeText(this , "Failed creating account due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserInfor() {
       progressDialog.setMessage("Saving user info ...")
        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid
        val hashMap :HashMap<String , Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!).setValue(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this , "Account created...",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this , DashboardUserActivity::class.java))
            finish()
        }.addOnFailureListener{
            progressDialog.dismiss()
            Toast.makeText(this , "Failed saving account due to ${it.message}",Toast.LENGTH_SHORT).show()
        }

    }
}
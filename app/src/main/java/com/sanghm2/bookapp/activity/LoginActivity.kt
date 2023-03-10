package com.sanghm2.bookapp.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.MyApplication
import com.sanghm2.bookapp.R
import com.sanghm2.bookapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private var  isClick : Boolean  = false
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left)
        initVar()
        actionView()
    }

    private fun validateDate() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        if(email.isEmpty()){
            Toast.makeText(this, "Enter email..." , Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Pattern..." , Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Enter password..." , Toast.LENGTH_SHORT).show()
        }
        else {
            loginUser()
        }
    }

    private fun actionView(){
        binding.loginBtn.setOnClickListener {
            validateDate()
        }
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java ))
        }
        binding.layoutLogin.setOnClickListener {
            hideKeyboard(binding.emailEt)
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }
    }
    private fun initVar(){
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog =  ProgressDialog(this )
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        MyApplication.hideAndShowText(binding.passwordEt, binding.passwordTil,isClick)
    }
    private fun loginUser() {
        progressDialog.setMessage("Logging In...")
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
            checkUser()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this , "${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUser() {
        progressDialog.setMessage("Checking User ...")

        val firebaseUser  = firebaseAuth.currentUser!!

        val  ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()

                val userType = snapshot.child("userType").value
                if(userType == "user"){
                    startActivity(Intent(this@LoginActivity , DashboardUserActivity::class.java))
                    finish()
                }else if(userType == "admin"){
                    startActivity(Intent(this@LoginActivity , DashboardAdminActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }
}
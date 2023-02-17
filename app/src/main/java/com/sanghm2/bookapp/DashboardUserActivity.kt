package com.sanghm2.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.sanghm2.bookapp.databinding.ActivityDashboardUserBinding

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityDashboardUserBinding
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            binding.subtTitleTv.text = "Not Logged In"
        }else {
            val email  = firebaseUser.email
            binding.subtTitleTv.text = email
        }
    }
}
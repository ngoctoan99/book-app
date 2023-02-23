package com.sanghm2.bookapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sanghm2.bookapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener{
            startActivity( Intent(this , LoginActivity::class.java))
        }
        binding.btnWithoutLogin.setOnClickListener {
            startActivity(Intent(this , DashboardUserActivity::class.java))
        }
    }
}
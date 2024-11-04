package com.sanghm2.bookapp.activity


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings

import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.BuildConfig
import com.sanghm2.bookapp.adapter.AdapterCategory
import com.sanghm2.bookapp.databinding.ActivityDarhboardAdminBinding
import com.sanghm2.bookapp.model.ModelCategory
import java.lang.Exception

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDarhboardAdminBinding

    private lateinit var firebaseAuth  : FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategory: AdapterCategory
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDarhboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

        if(ContextCompat.checkSelfPermission(this , Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

        }else {
            val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    uri
                )
            )
        }
        hideKeyboard()
        checkUser()
        loadCategories()

        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(p0)
                }catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }
        binding.addCategory.setOnClickListener {
            startActivity(Intent(this , CategoryAddActivity::class.java))
        }
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
    private fun hideKeyboard(){
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
    private fun loadCategories() {
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                }
                adapterCategory = AdapterCategory(this@DashboardAdminActivity , categoryArrayList)
                binding.categoryRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        }else {
            val email  = firebaseUser.email
            binding.subtTitleTv.text = email
        }
    }
}
package com.sanghm2.bookapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.MyApplication
import com.sanghm2.bookapp.R
import com.sanghm2.bookapp.adapter.AdapterBookFavorite
import com.sanghm2.bookapp.databinding.ActivityProfileBinding
import com.sanghm2.bookapp.model.ModelPdf

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProfileBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var bookArrayList: ArrayList<ModelPdf>
    private lateinit var adapterFavorite :AdapterBookFavorite
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.editBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val email = "${snapshot.child("email").value}"
                val name = "${snapshot.child("name").value}"
                val profileImage = "${snapshot.child("profileImage").value}"
                val timestamp = "${snapshot.child("timestamp").value}"
                val uid = "${snapshot.child("uid").value}"
                val userType = "${snapshot.child("userType").value}"

                val formatDate = MyApplication.formatTimeStamp(timestamp.toLong())
                binding.nameTv.text = name
                binding.emailTv.text = email
                binding.memberDateTv.text = formatDate
                binding.accoutnType.text = userType

                try{
                    Glide.with(this@ProfileActivity).load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIv)
                }catch (e: Exception){
                    Glide.with(this@ProfileActivity).load(R.drawable.ic_person_gray).into(binding.profileIv)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    private fun loadFavoriteBook(){
        bookArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                bookArrayList.clear()
                for (ds in snapshot.children){
                    val bookId = "${snapshot.child("bookId").value}"
                    val modelPdf = ModelPdf()
                    modelPdf.id  =bookId
                    bookArrayList.add(modelPdf)
                }
                binding.favoriteBookTv.text = "${bookArrayList.size}"
                adapterFavorite = AdapterBookFavorite(this@ProfileActivity,bookArrayList)
                binding.favoriteBookRv.adapter = adapterFavorite
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
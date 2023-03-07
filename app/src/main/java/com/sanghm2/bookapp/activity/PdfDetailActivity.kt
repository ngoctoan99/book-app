package com.sanghm2.bookapp.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sanghm2.bookapp.MyApplication
import com.sanghm2.bookapp.R
import com.sanghm2.bookapp.adapter.AdapterBookFavorite
import com.sanghm2.bookapp.databinding.ActivityPdfDetailBinding
import com.sanghm2.bookapp.ultil.Constants
import java.io.FileOutputStream
import java.lang.Exception

class PdfDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfDetailBinding
    private var pdfId = ""
    private var bookTitle = ""
    private var bookUrl = ""
    private  lateinit var progressDialog : ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private  var isInMyFavorite = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pdfId = intent.getStringExtra("pdfId")!!
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            checkFavorite()
        }
        Log.d("toanpdfId", pdfId)
        MyApplication.incrementBookViewCount(pdfId)
        loadBookDetail()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PDFViewActivity::class.java)
            intent.putExtra("bookId", pdfId)
            startActivity(intent)
        }
        binding.downBookBtn.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    downloadBook()
            }else {
                    requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        binding.addFavoriteBtn.setOnClickListener {
            if(firebaseAuth.currentUser == null){
                Toast.makeText(this , "Login to add favorite",Toast.LENGTH_SHORT).show()
            }
            else {
                if(isInMyFavorite){
                    MyApplication.removeFavorite(this,pdfId)
                }else {
                    addFavorite()
                }
            }
        }
    }

    private fun addFavorite() {
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = pdfId
        hashMap["timestamp"] = timestamp
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(pdfId).setValue(hashMap).addOnSuccessListener {

        }.addOnFailureListener { e->
            Toast.makeText(this, "Failed due to ${e.message}",Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkFavorite(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(pdfId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                isInMyFavorite = snapshot.exists()!!
                if(isInMyFavorite){
                    binding.addFavoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0)
                    binding.addFavoriteBtn.text = "Favorited"
                }else {
                    binding.addFavoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white,0,0)
                    binding.addFavoriteBtn.text = "Favorite"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun loadBookDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(pdfId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = "${snapshot.child("categoryId").value}"
                val description = "${snapshot.child("description").value}"
                val downloadsCount = "${snapshot.child("downloadsCount").value}"
                val id = "${snapshot.child("id").value}"
                val timestamp = "${snapshot.child("timestamp").value}"
                bookTitle = "${snapshot.child("title").value}"
                val uid = "${snapshot.child("uid").value}"
                bookUrl = "${snapshot.child("url").value}"
                val viewsCount = "${snapshot.child("viewsCount").value}"

                val date = MyApplication.formatTimeStamp(timestamp.toLong())
                MyApplication.loadCategory(categoryId, binding.categoryTv)
                MyApplication.loadPdfFromUrlSinglePage(
                    bookUrl, bookTitle, binding.pdfView, binding.pageTv
                )
                MyApplication.loadPdfSize(bookUrl, bookTitle, binding.sizeTv)

                binding.titleTv.text = bookTitle
                binding.descriptionTv.text = description
                binding.viewTv.text = viewsCount
                binding.downTv.text = downloadsCount
                binding.dateTv.text = date

                Handler().postDelayed({
                    binding.detailbookRl.visibility = View.VISIBLE
                    binding.descriptionTv.visibility = View.VISIBLE
                    binding.shimmer.stopShimmerAnimation()
                    binding.shimmer.visibility = View.GONE
                }, 3000)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Toan", "Failled due to ${error.message}")
            }

        })
    }
    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted:Boolean->
        if(isGranted){

        }else {
            Toast.makeText(this, "Permission denied",Toast.LENGTH_SHORT).show()
        }
    }
    private fun downloadBook(){
        progressDialog.setMessage("Downloading Book")
        progressDialog.show()
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->
            saveToDownloadFolder(bytes)
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed download due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToDownloadFolder(bytes: ByteArray) {
        val nameWithExtention = "$bookTitle.pdf"
        try {
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder.mkdirs()
            val filePaths = downloadFolder.path + "/" + nameWithExtention
            val out  =FileOutputStream(filePaths)
            out.write(bytes)
            out.close()
            Toast.makeText(this , "Saved to Download Folder : ${filePaths}",Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            incrementDownLoadCount()
        }catch (e: Exception){
            progressDialog.dismiss()
            Toast.makeText(this, "Failed download due to ${e.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownLoadCount() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(pdfId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var downloadsCount = "${snapshot.child("downloadsCount").value}"
                if(downloadsCount == null && downloadsCount == ""){
                    downloadsCount ="0"
                }
                val newDownloadsCount : Long  = downloadsCount.toLong() + 1
                val hashMap : HashMap<String, Any> = HashMap()
                hashMap["downloadsCount"] = newDownloadsCount

                val db = FirebaseDatabase.getInstance().getReference("Books")
                db.child(pdfId).updateChildren(hashMap).addOnSuccessListener {

                }.addOnFailureListener{

                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onStart() {
        super.onStart()
        binding.shimmer.startShimmerAnimation()
    }
    override fun onResume() {
        super.onResume()
        binding.shimmer.startShimmerAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmer.stopShimmerAnimation()
    }
}
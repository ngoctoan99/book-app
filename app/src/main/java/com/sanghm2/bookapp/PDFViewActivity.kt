package com.sanghm2.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sanghm2.bookapp.databinding.ActivityPdfviewBinding

class PDFViewActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfviewBinding
    var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetail()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val pdfUrl = snapshot.child("url").value
                loadBookFromUrl("$pdfUrl")

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadBookFromUrl(pdfUrl : String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes->
            binding.pdfView.fromBytes(bytes)
                .swipeHorizontal(false)
                .onPageChange{page , pageCount ->
                    val currentPage =  page + 1
                    binding.toolBarSubTitleTv.text = "${currentPage}/${pageCount}"
                }
                .onError {
                    Log.e("Errorsss" , "loadBookFromUrl  ${it.message}")
                }.onPageError{page , t ->
                    Log.e("Errorsss" , "loadBookFromUrl  ${t.message}")
                }.load()
            binding.progressBar.visibility = View.GONE
        }.addOnFailureListener{
            Toast.makeText(this, "Failed load pdf due to ${it.message}",Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
    }
}
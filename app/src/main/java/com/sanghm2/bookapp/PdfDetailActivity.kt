package com.sanghm2.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.databinding.ActivityPdfDetailBinding

class PdfDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfDetailBinding
    private var pdfId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdfId = intent.getStringExtra("pdfId")!!
        MyApplication.incrementBookViewCount(pdfId)
        loadBookDetail()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this , PDFViewActivity::class.java)
            intent.putExtra("bookId" ,pdfId)
            startActivity(intent)
        }
    }



    private fun loadBookDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(pdfId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = "${snapshot.child("categoryId").value}"
                val description = "${snapshot.child("description").value}"
                val downloadsCount = "${snapshot.child("downloadsCount").value}"
                val id = "${snapshot.child("id").value}"
                val timestamp = "${snapshot.child("timestamp").value}"
                val title = "${snapshot.child("title").value}"
                val uid = "${snapshot.child("uid").value}"
                val url = "${snapshot.child("url").value}"
                val viewsCount = "${snapshot.child("viewsCount").value}"

                val date = MyApplication.formatTimeStamp(timestamp.toLong())
                MyApplication.loadCategory(categoryId, binding.categoryTv)
                MyApplication.loadPdfFromUrlSinglePage("${url}","${title}",binding.pdfView,binding.progressBar,binding.pageTv)
                MyApplication.loadPdfSize("${url}","${title}",binding.sizeTv)

                binding.titleTv.text = title
                binding.descriptionTv.text = description
                binding.viewTv.text = viewsCount
                binding.downTv.text = downloadsCount
                binding.dateTv.text = date
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}
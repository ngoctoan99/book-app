package com.sanghm2.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sanghm2.bookapp.databinding.ActivityPdfDetailBinding

class PdfDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfDetailBinding
    private var pdfId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdfId = intent.getStringExtra("pdfId").toString()
    }
}
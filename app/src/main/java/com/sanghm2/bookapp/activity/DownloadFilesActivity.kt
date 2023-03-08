package com.sanghm2.bookapp.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.sanghm2.bookapp.R
import com.sanghm2.bookapp.adapter.AdapterFile
import com.sanghm2.bookapp.databinding.ActivityDownloadFilesBinding
import com.sanghm2.bookapp.model.ModelFile
import java.io.File

class DownloadFilesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDownloadFilesBinding
    private lateinit var pdfArrayList : ArrayList<ModelFile>
    private lateinit var adapterFile : AdapterFile
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        firebaseAuth = FirebaseAuth.getInstance()
        loadFileDocuments()
        binding.backBtn.setOnClickListener {
            val intent = Intent(this , ProfileActivity::class.java)
            intent.putExtra("profile","profile")
            startActivity(intent)
            finish()
        }
    }

    private fun loadFileDocuments() {
        pdfArrayList = ArrayList()
        adapterFile = AdapterFile(this , pdfArrayList)
        binding.myFileRv.adapter = adapterFile
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"book app/${firebaseAuth.currentUser!!.email}")
        if(folder.exists()){
            val files = folder.listFiles()
            if(files != null && files.isEmpty()){
                    binding.imageEmpty.visibility = View.VISIBLE
                    binding.myFileRv.visibility = View.GONE
            }else {
                for(fileEntry in files){
                    binding.imageEmpty.visibility = View.GONE
                    binding.myFileRv.visibility = View.VISIBLE
                    val uri = Uri.fromFile(fileEntry).toString()
                    val modelPdf = ModelFile(fileEntry,uri)
                    pdfArrayList.add(modelPdf)
                    Log.d("toandatafile",modelPdf.toString())
                    adapterFile.notifyItemInserted(pdfArrayList.size)
                }
            }
        }else {
            binding.imageEmpty.visibility = View.VISIBLE
            binding.myFileRv.visibility = View.GONE
        }
    }
}
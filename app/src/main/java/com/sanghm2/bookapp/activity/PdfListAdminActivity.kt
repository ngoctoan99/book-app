package com.sanghm2.bookapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.adapter.AdapterPDFAdmin
import com.sanghm2.bookapp.databinding.ActivityPdfListAdminBinding
import com.sanghm2.bookapp.model.ModelPdf

class PdfListAdminActivity : AppCompatActivity() {
    private var categoryId = ""
    private var category = ""
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterpdf : AdapterPDFAdmin
    private lateinit var binding : ActivityPdfListAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId= intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!
        binding.subTitleTv.text = category
        loadPdfList()
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try{
                    adapterpdf.filter.filter(p0)
                }catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
    }

    private fun loadPdfList() {
        pdfArrayList= ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)
                    if(model != null){
                        pdfArrayList.add(model)
                    }
                }
                adapterpdf = AdapterPDFAdmin(this@PdfListAdminActivity,pdfArrayList)
                binding.bookRvs.adapter = adapterpdf
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}
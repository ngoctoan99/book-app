package com.sanghm2.bookapp.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.adapter.AdapterPdfUser
import com.sanghm2.bookapp.databinding.FragmentBookUserBinding
import com.sanghm2.bookapp.model.ModelPdf
import java.lang.Exception


class BookUserFragment : Fragment {

    public companion object{
        public fun newInstance(categoryId: String, category: String, uid: String): BookUserFragment {
            val fragment = BookUserFragment()
            val args = Bundle()
            args.putString("categoryId",categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }
    }
    private lateinit var binding : FragmentBookUserBinding
    private  var categoryId = ""
    private  var category = ""
    private  var uid = ""

    private lateinit var pdfArrayList : ArrayList<ModelPdf>
    private lateinit var adapterPdfUser  : AdapterPdfUser
    constructor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args  =arguments
        if(args != null){
            category = args.getString("category")!!
            categoryId = args.getString("categoryId")!!
            uid = args.getString("uid")!!

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentBookUserBinding.inflate(LayoutInflater.from(context), container , false)
        if(category == "All"){
            loadAllBook()
        }else if(category == "Most Viewed") {
            loadMostViewedDownloadedBook("viewsCount")
        }else if (category == "Most Downloaded") {
            loadMostViewedDownloadedBook("downloadsCount")
        }else {
            loadCategorizeBook()
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try {
                        adapterPdfUser.filter.filter(p0)
                    }catch (e: Exception){
                    }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        return binding.root
    }

    private fun loadAllBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val modelPdf = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(modelPdf!!)
                }
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.bookRv.adapter  = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("toanerror" , "Failed to due ${error.message}")
            }

        })
    }

    private fun loadMostViewedDownloadedBook(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToFirst(10).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val modelPdf = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(modelPdf!!)
                }
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.bookRv.adapter  = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("toanerror" , "Failed to due ${error.message}")
            }

        })
    }

    private fun loadCategorizeBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val modelPdf = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(modelPdf!!)
                }
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.bookRv.adapter  = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("toanerror" , "Failed to due ${error.message}")
            }

        })
    }
}
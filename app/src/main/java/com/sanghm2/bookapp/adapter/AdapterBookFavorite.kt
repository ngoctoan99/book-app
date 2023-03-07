package com.sanghm2.bookapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.MyApplication
import com.sanghm2.bookapp.activity.PdfDetailActivity
import com.sanghm2.bookapp.databinding.RowBookFavoriteBinding
import com.sanghm2.bookapp.model.ModelPdf

class AdapterBookFavorite : RecyclerView.Adapter<AdapterBookFavorite.HolderPdfFavorite>{
    private val context : Context
    private var bookArrayList : ArrayList<ModelPdf>
    private lateinit var binding : RowBookFavoriteBinding

    constructor(context: Context, bookArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.bookArrayList = bookArrayList
    }

    inner class HolderPdfFavorite(itemView : View) : RecyclerView.ViewHolder(itemView){
        var pdfView = binding.pdfView
        var titleTv = binding.titleTv
        var removeFavoriteIv = binding.removeFavoriteBook
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = RowBookFavoriteBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        Log.d("toansssssdsdsd","$position")
        val model = bookArrayList[position]
        loadBookDetails(model, holder)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("pdfId",model.id)
            context.startActivity(intent)
        }
        holder.removeFavoriteIv.setOnClickListener {
            MyApplication.removeFavorite(context,model.id)
        }
    }
    private fun loadBookDetails(model: ModelPdf, holder: AdapterBookFavorite.HolderPdfFavorite) {
        val bookId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = "${snapshot.child("categoryId").value}"
                val description = "${snapshot.child("description").value}"
                val downloadsCount = "${snapshot.child("downloadsCount").value}"
                val timestamp = "${snapshot.child("timestamp").value}"
                val title = "${snapshot.child("title").value}"
                val uid = "${snapshot.child("uid").value}"
                val url = "${snapshot.child("url").value}"
                val viewsCount = "${snapshot.child("viewsCount").value}"

                model.isFavorite=  true
                model.title = title
                model.description = description
                model.categoryId = categoryId
                model.timestamp = timestamp.toLong()
                model.uid = uid
                model.url = url
                model.viewsCount = viewsCount.toLong()
                model.downloadsCount = downloadsCount.toLong()

                val date = MyApplication.formatTimeStamp(timestamp.toLong())
                MyApplication.loadCategory(categoryId,holder.categoryTv)
                MyApplication.loadPdfSize("$url","$title",holder.sizeTv)
                MyApplication.loadPdfFromUrlSinglePage("$url","$title",holder.pdfView,null)
                holder.titleTv.text = title
                holder.descriptionTv.text = description
                holder.dateTv.text = date
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    override fun getItemCount(): Int {
        return bookArrayList.size
    }
}
package com.sanghm2.bookapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.sanghm2.bookapp.MyApplication
import com.sanghm2.bookapp.activity.PdfDetailActivity
import com.sanghm2.bookapp.databinding.RowPdfUserBinding
import com.sanghm2.bookapp.filter.FilterPdfAdmin
import com.sanghm2.bookapp.filter.FilterPdfUser
import com.sanghm2.bookapp.model.ModelPdf
import kotlin.math.sign

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable{
    private  var context : Context
    public var pdfList  : ArrayList<ModelPdf>
    private lateinit var binding : RowPdfUserBinding

    private var filterList : ArrayList<ModelPdf>

    private var filter : FilterPdfUser? = null
    constructor(context: Context, pdfList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfList = pdfList
        this.filterList = pdfList
    }

    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView){
            var pdfView  = binding.pdfView
            var titleTv  = binding.titleTv
            var descriptionTv  = binding.descriptionTv
            var progressBar  = binding.progressBar
            var sizeTv  = binding.sizeTv
            var dateTv  = binding.dateTv
            var categoryTv  = binding.categoryTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
            binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent,false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val  model = pdfList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        val date = MyApplication.formatTimeStamp(timestamp)

        holder.titleTv.text =  title
        holder.descriptionTv.text = description
        holder.dateTv.text = date
        MyApplication.loadPdfFromUrlSinglePage(url ,title,holder.pdfView,holder.progressBar,null )
        MyApplication.loadPdfSize(url,title,holder.sizeTv)
        MyApplication.loadCategory(categoryId,holder.categoryTv)

        holder.itemView.setOnClickListener {
            val intent = Intent(context , PdfDetailActivity::class.java)
            intent.putExtra("pdfId",bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }
}
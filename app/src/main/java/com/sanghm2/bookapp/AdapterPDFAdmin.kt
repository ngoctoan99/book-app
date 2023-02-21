package com.sanghm2.bookapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.sanghm2.bookapp.databinding.RowPdfAdminBinding

class AdapterPDFAdmin : RecyclerView.Adapter<AdapterPDFAdmin.HolderPDFAdmin>,Filterable {
    private lateinit var binding : RowPdfAdminBinding
    private var context : Context
    public var pdfAdminArrayList : ArrayList<ModelPdf>
    private val filterList : ArrayList<ModelPdf>
    var filter : FilterPdfAdmin? = null

    constructor(context: Context, pdfAdminArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfAdminArrayList = pdfAdminArrayList
        this.filterList = pdfAdminArrayList
    }


    inner class HolderPDFAdmin(itemView: View) :RecyclerView.ViewHolder(itemView){
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPDFAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent,false)
        return HolderPDFAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPDFAdmin, position: Int) {

        val modelPdf = pdfAdminArrayList[position]
        val pdfId = modelPdf.id
        val categoryId = modelPdf.categoryId
        val title = modelPdf.title
        val description = modelPdf.description
        val pdfUrl = modelPdf.url
        val timeStamp = modelPdf.timestamp
        val format = MyApplication.formatTimeStamp(timeStamp)
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = format
        MyApplication.loadCategory(categoryId,holder.categoryTv)
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl,title,holder.pdfView,holder.progressBar,null)
        MyApplication.loadPdfSize(pdfUrl,title,holder.sizeTv)

    }

    override fun getItemCount(): Int {
        return pdfAdminArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }
}
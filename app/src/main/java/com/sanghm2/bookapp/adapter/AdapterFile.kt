package com.sanghm2.bookapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sanghm2.bookapp.activity.PDFViewActivity
import com.sanghm2.bookapp.databinding.RowPdfBinding
import com.sanghm2.bookapp.model.ModelFile

class AdapterFile(private var context: Context, private var pdfArrayList: ArrayList<ModelFile>) :
    RecyclerView.Adapter<AdapterFile.HolderPdf>() {

    private lateinit var binding: RowPdfBinding


    inner class HolderPdf(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameFile = binding.pdfName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdf {
        binding = RowPdfBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdf(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdf, position: Int) {

        val modelFile = pdfArrayList[position]
        val name =  modelFile.file.name
        holder.nameFile.text = name
        holder.itemView.setOnClickListener {
            val intent =  Intent(context , PDFViewActivity::class.java)
            intent.putExtra("uri",modelFile)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

}

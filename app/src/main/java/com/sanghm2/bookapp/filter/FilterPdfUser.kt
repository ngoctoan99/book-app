package com.sanghm2.bookapp.filter

import android.widget.Filter
import com.sanghm2.bookapp.adapter.AdapterPDFAdmin
import com.sanghm2.bookapp.adapter.AdapterPdfUser
import com.sanghm2.bookapp.model.ModelPdf

class FilterPdfUser :Filter{
    private var filterPdfUser  :ArrayList<ModelPdf>
    private var adapterPDFUser: AdapterPdfUser

    constructor(filterPdfUser: ArrayList<ModelPdf>, adapterPDFUser: AdapterPdfUser):super() {
        this.filterPdfUser = filterPdfUser
        this.adapterPDFUser = adapterPDFUser
    }


    override fun performFiltering(constrant: CharSequence?): Filter.FilterResults {
        var constraint  = constrant
        val results = Filter.FilterResults()
        if(constraint!= null && constraint.isNotEmpty()){
            constraint= constrant.toString().lowercase()
            val filteredModels = ArrayList<ModelPdf>()
            for( i in filterPdfUser.indices){
                if(filterPdfUser[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterPdfUser[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else {
            results.count = filterPdfUser.size
            results.values = filterPdfUser
        }
        return results

    }

    override fun publishResults(constrant: CharSequence?, results: Filter.FilterResults?) {
        adapterPDFUser.pdfList =  results?.values as ArrayList<ModelPdf>
        adapterPDFUser.notifyDataSetChanged()
    }
}
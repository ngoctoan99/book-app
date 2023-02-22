package com.sanghm2.bookapp

import android.widget.Filter

class FilterPdfAdmin : Filter{
    private var filterPdfAdmin  :ArrayList<ModelPdf>
    private var adapterPDFAdmin: AdapterPDFAdmin

    constructor(filterPdfAdmin: ArrayList<ModelPdf>, adapterPDFAdmin: AdapterPDFAdmin):super(){
        this.filterPdfAdmin = filterPdfAdmin
        this.adapterPDFAdmin = adapterPDFAdmin
    }

    override fun performFiltering(constrant: CharSequence?): FilterResults {
        var constraint  = constrant
        val results = FilterResults()
        if(constraint!= null && constraint.isNotEmpty()){
            constraint= constrant.toString().lowercase()
            val filteredModels = ArrayList<ModelPdf>()
            for( i in filterPdfAdmin.indices){
                if(filterPdfAdmin[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterPdfAdmin[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else {
            results.count = filterPdfAdmin.size
            results.values = filterPdfAdmin
        }
        return results

    }

    override fun publishResults(constrant: CharSequence?, results: FilterResults?) {
        adapterPDFAdmin.pdfAdminArrayList =  results?.values as ArrayList<ModelPdf>
        adapterPDFAdmin.notifyDataSetChanged()
    }
}
package com.sanghm2.bookapp.filter

import android.widget.Filter
import com.sanghm2.bookapp.adapter.AdapterCategory
import com.sanghm2.bookapp.model.ModelCategory

class FilterCategory : Filter {
    private var filterList : ArrayList<ModelCategory>
    private var adapterCategory : AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var  constraint = constraint
        val results  = FilterResults()

        if(constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().uppercase()
            val filteredModel:ArrayList<ModelCategory> = ArrayList()
            for( i in 0 until filterList.size){
                if(filterList[i].category.uppercase().contains(constraint)){
                    filteredModel.add(filterList[i])
                }
            }
            results.count = filteredModel.size
            results.values = filteredModel
        }else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
        adapterCategory.categoryArrayList = p1?.values as ArrayList<ModelCategory>
        adapterCategory.notifyDataSetChanged()
    }
}
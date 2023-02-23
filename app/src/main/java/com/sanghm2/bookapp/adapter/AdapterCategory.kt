package com.sanghm2.bookapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.sanghm2.bookapp.filter.FilterCategory
import com.sanghm2.bookapp.model.ModelCategory
import com.sanghm2.bookapp.activity.PdfListAdminActivity
import com.sanghm2.bookapp.databinding.RowCategoryBinding


class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HodelCategory>, Filterable {

    private val context : Context
    public var categoryArrayList : ArrayList<ModelCategory>
    private lateinit var binding : RowCategoryBinding
    private var filterList : ArrayList<ModelCategory>

    private var filter: FilterCategory? =  null

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    inner class HodelCategory(itemView : View): RecyclerView.ViewHolder(itemView){
        var  categoryTv : TextView = binding.categoyTv
        var  deleteBtn : ImageButton = binding.deleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HodelCategory {
       binding = RowCategoryBinding.inflate(LayoutInflater.from(context) ,parent,false)
        return HodelCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HodelCategory, position: Int) {
        val model  = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        holder.categoryTv.text = category
        holder.deleteBtn.setOnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you want to delete this category?")
                .setPositiveButton("Confirm"){a, d->
                    Toast.makeText(context,"Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }.setNegativeButton("Cancel"){a,d->
                    a.dismiss()
                }.show()
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId",id)
            intent.putExtra("category",category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HodelCategory) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id).removeValue().addOnSuccessListener {
            Toast.makeText(context,"Deleted...",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(context,"Unable to delete due to ${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}
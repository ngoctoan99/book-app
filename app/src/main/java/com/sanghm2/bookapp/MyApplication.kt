package com.sanghm2.bookapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sanghm2.bookapp.ultil.Constants
import java.util.*
import kotlin.collections.HashMap

class MyApplication : Application() {

    companion object {
        fun formatTimeStamp(timeStamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timeStamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata.addOnSuccessListener { storageMetaData ->
                Log.d(TAG, "loadPdfSize : got metada")
                val byte = storageMetaData.sizeBytes.toDouble()
                Log.d(TAG, "loadPdfSize : Size bytes $byte")

                val kb = byte / 1024
                val mb = kb / 1024
                if (mb > 1) {
                    sizeTv.text = "${String.format("%.2f", mb)} MB"
                } else if (kb >= 1) {
                    sizeTv.text = "${String.format("%.2f", kb)} KB"
                } else {
                    sizeTv.text = "${String.format("%.2f", kb)} bytes"
                }
            }.addOnFailureListener {
                Log.d(TAG, "loadPdfSize : Failed to get metada due to ${it.message}")
            }
        }


        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
//            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { byte ->
                pdfView.recycle()
                pdfView.fromBytes(byte).pages(0).spacing(0).swipeHorizontal(false)
                    .enableSwipe(false)
                    .onError { t -> 
//                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG,"loadPdfFromURLSinglePage : ${t.message}")
                    }.onPageError { page, t ->
//                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG,"loadPdfFromURLSinglePage : ${t.message}")
                    }.onLoad { s->
//                        progressBar.visibility = View.INVISIBLE
                        if(pagesTv !=null){
                            pagesTv.text = "${s}"
                        }
                    }
                    .load()
            }.addOnFailureListener {
                Log.d(TAG, "loadPdfSize : Failed to get metada due to ${it.message}")
            }
        }
        fun loadPdfFromUrlSinglePageLoading(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            shimmerFrameLayout: ShimmerFrameLayout,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { byte ->
                pdfView.recycle()
                pdfView.fromBytes(byte).pages(0).spacing(0).swipeHorizontal(false)
                    .enableSwipe(false)
                    .onError { t ->
                        shimmerFrameLayout.visibility = View.INVISIBLE
                        Log.d(TAG,"loadPdfFromURLSinglePage : ${t.message}")
                    }.onPageError { page, t ->
                        shimmerFrameLayout.visibility = View.INVISIBLE
                        Log.d(TAG,"loadPdfFromURLSinglePage : ${t.message}")
                    }.onLoad { s->
                        shimmerFrameLayout.visibility = View.INVISIBLE
                        if(pagesTv !=null){
                            pagesTv.text = "${s}"
                        }
                    }
                    .load()
            }.addOnFailureListener {
                Log.d(TAG, "loadPdfSize : Failed to get metada due to ${it.message}")
            }
        }
        fun loadCategory(categoryId : String , categoryTv :TextView){
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val category :String = "${snapshot.child("category").value}"
                    categoryTv.text = category
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })

        }
        fun deleteBook(context: Context , bookId : String , bookUrl: String , bookTitle:String){
            val  progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete().addOnSuccessListener {
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).removeValue().addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(context,"Successfully Deleted ...",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context,"Failed due to ${it.message}",Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(context,"Failed due to ${it.message}",Toast.LENGTH_SHORT).show()
            }
        }
        fun incrementBookViewCount(bookId : String){
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var viewsCount = "${snapshot.child("viewsCount").value}"

                    if(viewsCount == "" || viewsCount == null) {
                        viewsCount = "0"
                    }
                    val newViewCount = viewsCount.toLong() + 1
                    val hashMap = HashMap<String,Any>()
                    hashMap["viewsCount"] = newViewCount
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId).updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

        fun hideAndShowText(editText : EditText ,textInputLayout: TextInputLayout , isClick: Boolean){
            var isClicks = isClick
            editText.inputType =  InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            textInputLayout.setEndIconDrawable(R.drawable.eyeslash)
            textInputLayout.setEndIconOnClickListener {
                if(!isClicks){
                    editText.inputType = InputType.TYPE_CLASS_TEXT
                    editText.setSelection(editText.length())
                    isClicks = true
                    textInputLayout.setEndIconDrawable(R.drawable.eye)
                }else{
                    editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    editText.setSelection(editText.length())
                    isClicks = false
                    textInputLayout.setEndIconDrawable(R.drawable.eyeslash)
                }

            }
        }
        fun  hideKeyboardWhenNotFocus(editText: EditText) : Boolean{
            var notFocus : Boolean= false
            editText.onFocusChangeListener = View.OnFocusChangeListener { view, focus ->
                notFocus = !focus
            }
            Log.d("ToanClick" ,"$notFocus")
            return notFocus
        }
        fun removeFavorite(context:Context ,pdfId : String){
            val firebaseAuth = FirebaseAuth.getInstance()
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(pdfId).removeValue().addOnSuccessListener {

            }.addOnFailureListener { e->
                Toast.makeText(context,"Failed due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
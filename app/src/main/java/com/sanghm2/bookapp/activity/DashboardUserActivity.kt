package com.sanghm2.bookapp.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sanghm2.bookapp.fragment.BookUserFragment
import com.sanghm2.bookapp.databinding.ActivityDashboardUserBinding
import com.sanghm2.bookapp.model.ModelCategory

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityDashboardUserBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPaperAdapter: ViewPaperAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        setupWithViewPaperAdapter(binding.viewPager)
        binding.tabeLayout.setupWithViewPager(binding.viewPager)
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this , ProfileActivity::class.java))
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }
    private fun setupWithViewPaperAdapter(viewpaper: ViewPager){
        viewPaperAdapter = ViewPaperAdapter(supportFragmentManager,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this)
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               categoryArrayList.clear()
                val modelAll= ModelCategory("01","All",1,"")
                val modelMostViewed= ModelCategory("01","Most Viewed",1,"")
                val modelMostDownloaded= ModelCategory("01","Most Downloaded",1,"")

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostDownloaded)
                categoryArrayList.add(modelMostViewed)
                viewPaperAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                    "${modelAll.uid}"
                    ),modelAll.category
                )
                viewPaperAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${modelMostDownloaded.id}",
                            "${modelMostDownloaded.category}",
                            "${modelMostDownloaded.uid}"
                        ),modelMostDownloaded.category
                )
                viewPaperAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ),modelMostViewed.category
                )
                viewPaperAdapter.notifyDataSetChanged()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    viewPaperAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ),model.category
                    )
                    viewPaperAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

        viewpaper.adapter = viewPaperAdapter
    }
    class ViewPaperAdapter(fm : FragmentManager , behavior : Int , context: Context): FragmentPagerAdapter(fm,behavior){
        private val fragmentsList : ArrayList<BookUserFragment> = ArrayList()
        private val fragmentsTitleList : ArrayList<String> = ArrayList()
        private val context : Context
        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentsTitleList[position]
        }
        public fun addFragment(fragment: BookUserFragment, title : String){
            fragmentsList.add(fragment)
            fragmentsTitleList.add(title)
        }
    }
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            binding.subtTitleTv.text = "Not Logged In"
            binding.logoutBtn.visibility = View.GONE
            binding.profileBtn.visibility = View.GONE
            binding.backBtn.visibility = View.VISIBLE
        }else {
            val email  = firebaseUser.email
            binding.subtTitleTv.text = email
            binding.logoutBtn.visibility = View.VISIBLE
            binding.profileBtn.visibility = View.VISIBLE
            binding.backBtn.visibility = View.GONE
        }
    }
}
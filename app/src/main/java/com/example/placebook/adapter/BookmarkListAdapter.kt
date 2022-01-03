package com.example.placebook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.placebook.R
import com.example.placebook.ui.MapsActivity
import com.example.placebook.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.bookmark_item.view.*

class BookmarkListAdapter(
    private var bookmarkData:List<MapsViewModel.BookmarView>?,
    private val mapsActivity:MapsActivity): RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>()
{
        class ViewHolder(
            v: View,
            private val mapsActivity:MapsActivity
            ):RecyclerView.ViewHolder(v){
            init{
                v.setOnClickListener{
                    val bookmarkView = itemView.tag as MapsViewModel.BookmarView
                    mapsActivity.moveToBookmark(bookmarkView)
                }
            }
                val nameTextView: TextView = v.bookmarkNameTextView
            val categoryImageView: ImageView = v.bookmarkIcon
            }

    fun setBookmarkData(bookmarks:List<MapsViewModel.BookmarView>){
        this.bookmarkData = bookmarks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vh = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bookmark_item,parent,false),mapsActivity)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkData = bookmarkData?:return
        val bookmarkViewData = bookmarkData[position]
        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        bookmarkViewData.categoryResourceId?.let{
            holder.categoryImageView.setImageResource(it)
        }
    }

    override fun getItemCount(): Int {
        return bookmarkData?.size?:0
    }

}
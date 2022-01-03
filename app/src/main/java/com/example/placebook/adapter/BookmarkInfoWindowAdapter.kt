package com.example.placebook.adapter

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.placebook.R
import com.example.placebook.ui.MapsActivity
import com.example.placebook.viewmodel.MapsViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookmarkInfoWindowAdapter(val context: Activity): GoogleMap.InfoWindowAdapter {

    private val contents: View

    init {
        contents = context.layoutInflater.inflate(R.layout.content_bookmark_info,null)
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

    override fun getInfoContents(p0: Marker?): View ?{
       val titleView = contents.findViewById<TextView>(R.id.title)
        if (p0 != null) {
            titleView.text = p0.title ?: ""
        }
        val phoneView = contents.findViewById<TextView>(R.id.phone)
        if (p0 != null) {
            phoneView.text = p0.snippet ?: ""
        }
        val imageView = contents.findViewById<ImageView>(R.id.photo)
        if (p0 != null) {
            when(p0.tag){
                is MapsActivity.PlaceInfo -> {
                    imageView.setImageBitmap((p0.tag as MapsActivity.PlaceInfo).image)
                }
                is MapsViewModel.BookmarView -> {
                    var bookMarkview = p0.tag as MapsViewModel.BookmarView
                    imageView.setImageBitmap(bookMarkview.getImage(context))
                }
            }
        }
       // imageView.setImageBitmap((p0?.tag as MapsActivity.PlaceInfo).image)
        return contents
    }
}
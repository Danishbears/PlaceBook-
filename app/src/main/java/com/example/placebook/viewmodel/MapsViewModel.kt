package com.example.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.placebook.model.Bookmark
import com.example.placebook.repository.BookmarkRepo
import com.example.placebook.util.ImageUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class MapsViewModel(application: Application) :
    AndroidViewModel(application) {
    private var bookmarks:LiveData<List<BookmarView>>? = null


    private val TAG = "MapsViewModel"
    // 2
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(
        getApplication())
    // 3
    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        // 4
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()
        // 5
        bookmark.category = getPlaceCategory(place)
        val newId = bookmarkRepo.addBookmark(bookmark)
        image?.let {
            bookmark.setImage(it,getApplication())
        }
        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark):
        MapsViewModel.BookmarView{
            return MapsViewModel.BookmarView(bookmark.id,LatLng(bookmark.latitude,bookmark.longitude),
            bookmark.name,
            bookmark.phone,
            bookmarkRepo.getCategoryResourcesId(bookmark.category))
        }
    private fun mapBookmarksToBookmarkView()
    {
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks){
            repobookmarks ->
            repobookmarks.map { bookmark -> bookmarkToBookmarkView(bookmark) }
        }
    }

    fun getBookmarkViews():LiveData<List<BookmarView>>?{
        if(bookmarks == null){
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    data class BookmarView(
        var id : Long? = null,
        var location: LatLng = LatLng(0.0,0.0),
        var name:String = "",
        var phone:String = "",
        val categoryResourceId: Int? = null
    ){
        fun getImage(context: Context):Bitmap?{
            id?.let{
                return ImageUtils.loadBitmapFromFile(context,Bookmark.generateImageFile(it))
            }
            return null
        }
    }

    private fun getPlaceCategory(place: Place):String{
        var category = "Other"
        var placeTypes = place.types

        placeTypes?.let{
            placeTypes ->
            if(placeTypes.size > 0 ){
                val placeType = placeTypes[0]
                category = bookmarkRepo.placeTypeCategory(placeType).toString()
            }
        }
        return category
    }

    fun addBookmark(latLng: LatLng):Long?{
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.name = "Unitiled"
        bookmark.latitude = latLng.latitude
        bookmark.longitude = latLng.longitude
        bookmark.category = "Other"
        return bookmarkRepo.addBookmark(bookmark)
    }

}
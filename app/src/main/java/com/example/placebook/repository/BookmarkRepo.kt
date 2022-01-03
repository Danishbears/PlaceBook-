package com.example.placebook.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.example.placebook.R
import com.example.placebook.db.BookmarkDao
import com.example.placebook.db.PlaceBookDatabase
import com.example.placebook.model.Bookmark
import com.google.android.libraries.places.api.model.Place

// 1
class BookmarkRepo(val context: Context) {

    // 2
    private var db = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()

    private var categoryMap:HashMap<Place.Type,String> = buildCategoryMap()

    private var allCategories : HashMap<String,Int> = buildCategories()
    // 3
    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }
    // 4
    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    fun getLiveBookmark(bookmarkId:Long):LiveData<Bookmark>{
        val bookmark = bookmarkDao.loadLiveBookmark(bookmarkId)
        return bookmark
    }
    // 5
    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }

    fun updateBookmark(bookmark:Bookmark){
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId:Long):Bookmark{
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    private fun buildCategoryMap():HashMap<Place.Type,String>{
        return hashMapOf(
            Place.Type.BAKERY to "Restaurant",
            Place.Type.BAR to "Restaurant",
            Place.Type.CAFE to "Restaurant",
            Place.Type.FOOD to "Restaurant",
            Place.Type.RESTAURANT to "Restaurant",
            Place.Type.MEAL_DELIVERY to "Restaurant",
            Place.Type.MEAL_TAKEAWAY to "Restaurant",
            Place.Type.GAS_STATION to "Gas",
            Place.Type.CLOTHING_STORE to "Shopping",
            Place.Type.DEPARTMENT_STORE to "Shopping",
            Place.Type.FURNITURE_STORE to "Shopping",
            Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
            Place.Type.HARDWARE_STORE to "Shopping",
            Place.Type.HOME_GOODS_STORE to "Shopping",
            Place.Type.JEWELRY_STORE to "Shopping",
            Place.Type.SHOE_STORE to "Shopping",
            Place.Type.SHOPPING_MALL to "Shopping",
            Place.Type.STORE to "Shopping",
            Place.Type.LODGING to "Lodging",
            Place.Type.ROOM to "Lodging"
        )
    }

    fun placeTypeCategory(placeType:Place.Type):String?{
        var category = "Other"
        if(categoryMap.containsKey(placeType)){
            category = categoryMap[placeType].toString()
        }
        return category
    }

    private fun buildCategories(): HashMap<String,Int>{
        return hashMapOf(
            "Gas" to R.drawable.ic_gas,
            "Restaurant" to R.drawable.ic_restaurant_24,
            "Lodging" to R.drawable.ic_lodging,
            "Other" to R.drawable.ic_other,
            "Shopping" to R.drawable.ic_shopping_cart
        )
    }

    fun getCategoryResourcesId(placeCategory: String):Int?{

        return allCategories[placeCategory]
    }

    val categories:List<String>
    get() = ArrayList(allCategories.keys)


    fun deleteBookmark(bookmark:Bookmark){

        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }

}
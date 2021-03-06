package com.example.placebook.ui


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.placebook.R
import com.example.placebook.adapter.BookmarkInfoWindowAdapter
import com.example.placebook.adapter.BookmarkListAdapter

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.placebook.databinding.ActivityMapsBinding
import com.example.placebook.viewmodel.MapsViewModel
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.drawer_view_maps.*
import kotlinx.android.synthetic.main.main_view_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val mapsViewModel by viewModels<MapsViewModel>()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
   // private  var locationRequst : LocationRequest? = null
    private lateinit var placesClient:PlacesClient

    private lateinit var bookmarkListAdapter:BookmarkListAdapter
    private var markers = HashMap<Long,Marker>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setUpLocationClient()
        setupToolbar()
        setupPlacesClient()
        setupNavigationDrawer()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
      //  mMap.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        // Add a marker in Sydney and move the camera
       // val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        setupMapListener()
        createBookmarkObserver()
        getCurrentLocation()
        /*mMap.setOnPoiClickListener {
            displayPoi(it)
        }*/

    }

    private fun setupMapListener(){
        mMap.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        mMap.setOnPoiClickListener {
            displayPoi(it)
        }
        mMap.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
        fab.setOnClickListener{
            searchAtCurrentLocation()
        }
        mMap.setOnMapClickListener {
            latLng -> newBookmark(latLng)
        }
    }

    private fun setupPlacesClient(){
        Places.initialize(application,getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setUpLocationClient(){
        fusedLocationClient = FusedLocationProviderClient(this)
    }

    private fun requestLocationPermissions(){
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun displayPoiDisplayStep(place:Place, photo: Bitmap?){
        hideProgress()
      /*  val iconPhoto = if(photo == null){
            BitmapDescriptorFactory.defaultMarker()
        }
        else{
            BitmapDescriptorFactory.fromBitmap(photo)
        }*/
        val marker = mMap.addMarker(MarkerOptions().position(place.latLng as LatLng)
            //.icon(iconPhoto)
            .title(place.name)
            .snippet(place.phoneNumber))
        marker?.tag = PlaceInfo(place,photo)
        marker?.showInfoWindow() //This instructs the map to display the Info window for the marker
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        showProgress()
        displayPoiGetPlaceStep(pointOfInterest)
    }
    private fun displayPoiGetPlaceStep(pointOfInterest:
                                       PointOfInterest) {
        val placeId = pointOfInterest.placeId
        val placeFields = listOf(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG)
        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
              /*  Toast.makeText(this,
                    "${place.name}, " +
                            "${place.phoneNumber}",
                    Toast.LENGTH_LONG).show()*/
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode)
                    hideProgress()
                }
            }
    }

    private fun displayPoiGetPhotoStep(place:Place){

        val placeFields = listOf(Place.Field.ID,
        Place.Field.NAME,
        Place.Field.PHONE_NUMBER,
        Place.Field.PHOTO_METADATAS,
        Place.Field.ADDRESS,
        Place.Field.LAT_LNG,
        Place.Field.TYPES)

        val photoMetaData = place.photoMetadatas?.get(0)

        if(photoMetaData == null ){
            displayPoiDisplayStep(place,null)
            return
        }

        val photorequest = FetchPhotoRequest.builder(photoMetaData)
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))
            .build()

        placesClient.fetchPhoto(photorequest)
            .addOnSuccessListener {
                fetchPhotoRequst ->
                val bitmap = fetchPhotoRequst.bitmap
                displayPoiDisplayStep(place,bitmap)
            }.addOnFailureListener {
                exception ->
                if (exception is ApiException){
                    val statusCode = exception.statusCode
                    Log.e(TAG,"Place not found: " + exception.message + ", " + "statusCode" + statusCode)
                }
                hideProgress()
            }
    }

    private fun getCurrentLocation(){


        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestLocationPermissions()
        }else{

           /* if(locationRequst == null){   // to check user Location + move center of map to your current location!
                locationRequst = LocationRequest.create()
                locationRequst?.let {locationRequest ->
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    locationRequest.interval = 5000
                    locationRequest.fastestInterval = 1000
                    val locationCallback = object : LocationCallback(){
                        override fun onLocationResult(locationResult: LocationResult?) {
                            getCurrentLocation()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)
                }
            }*/
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener{
                val location = it.result
                if(location != null){
                    val latLng = LatLng(location.latitude,location.longitude)
                  //  mMap.clear()
                   // mMap.addMarker(MarkerOptions().position(latLng)
                    //    .title("You are here"))
                    val update = CameraUpdateFactory.newLatLngZoom(latLng,16.0f)
                    mMap.moveCamera(update)
                }else{
                    Log.e(TAG,"No location found")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_LOCATION){
            if(grantResults.size == 1  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }else{
                Log.e(TAG,"Location permission denied suka")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun handleInfoWindowClick(marker:Marker){
      /*  val placeInfo = (marker.tag as PlaceInfo)
        if(placeInfo.place !=null){
            GlobalScope.launch {
               mapsViewModel.addBookmarkFromPlace(placeInfo.place,placeInfo.image)
            }
            //mapsViewModel.addBookmarkFromPlace(placeInfo.place,placeInfo.image)
        }*/
        when(marker.tag){
            is MapsActivity.PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if(placeInfo.place != null && placeInfo.image !=null){
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place,placeInfo.image)
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarView ->{
                val bookmarkMarkView = (marker.tag as MapsViewModel.BookmarView)
                marker.hideInfoWindow()
                bookmarkMarkView.id?.let{
                    startBookmarkDetails(it)
                }
            }
        }

    }

    private fun addPlaceMarker(bookmark:MapsViewModel.BookmarView):Marker{ // add one Bookmark(that u tap on)
        val marker = mMap.addMarker(MarkerOptions()
            .position(bookmark.location)
            .title(bookmark.name)
            .snippet(bookmark.phone)
            .icon(bookmark.categoryResourceId?.let {
                BitmapDescriptorFactory.fromResource(it)
            })
            .alpha(0.8f))
        marker.tag = bookmark
        bookmark.id?.let{markers.put(it,marker)}
        return marker
    }

    private fun displatAllBookmarks(bookmarks:List<MapsViewModel.BookmarView>){ //add all bookmarks
        for(bookmark in bookmarks){
            addPlaceMarker(bookmark)
        }
    }

    private fun createBookmarkObserver(){
        //1
        mapsViewModel.getBookmarkViews()?.observe(
            this, Observer<List<MapsViewModel.BookmarView>>{
                //2
                mMap.clear()
                markers.clear()
                //3
                it?.let {
                    displatAllBookmarks(it)
                    bookmarkListAdapter.setBookmarkData(it)
                }
            }
        )
    }

    private fun startBookmarkDetails(bookmark:Long){
        val intent = Intent(this,BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID,bookmark)
        startActivity(intent)
    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer)
        toggle.syncState()

    }

    private fun setupNavigationDrawer(){
        val layoutManager = LinearLayoutManager(this)
        bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null,this)
        bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    private fun updateMapToLocation(location: Location){
        val latLng = LatLng(location.latitude,location.longitude)
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng,
            16.0f)
        )

    }

     fun moveToBookmark(bookmark:MapsViewModel.BookmarView){
        drawerLayout.closeDrawer(drawerView)
        val marker = markers[bookmark.id]
        marker?.showInfoWindow()
        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun searchAtCurrentLocation(){
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHOTO_METADATAS,
            Place.Field.PHONE_NUMBER,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.TYPES
        )

        val bounds = RectangularBounds.newInstance(mMap.projection.visibleRegion.latLngBounds)
        try{
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, placeFields
            ).setLocationBias(bounds)
                .build(this)
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }catch (e:GooglePlayServicesRepairableException){

        }catch (e:GooglePlayServicesNotAvailableException){

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            AUTOCOMPLETE_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val place = Autocomplete.getPlaceFromIntent(data)

                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?:0.0
                    location.longitude = place.latLng?.longitude?:0.0
                    updateMapToLocation(location)

                    displayPoiGetPhotoStep(place)
                }
            }
        }
    }

    private fun newBookmark(latLng: LatLng){
        GlobalScope.launch {
            val bookmarkId = mapsViewModel.addBookmark(latLng)
            bookmarkId?.let{
                startBookmarkDetails(it)
            }
        }
    }

    private fun disableUserInteraction(){
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun enableUserInteraction(){
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun showProgress(){
        progressBar.visibility = ProgressBar.VISIBLE
        disableUserInteraction()
    }

    private fun hideProgress(){
        progressBar.visibility = ProgressBar.GONE
        enableUserInteraction()
    }

    companion object{
        const val EXTRA_BOOKMARK_ID = "com.example.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
    }

    class PlaceInfo(val place:Place? = null, val image:Bitmap? = null)
}
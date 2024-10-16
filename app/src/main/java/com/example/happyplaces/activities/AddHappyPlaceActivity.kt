package com.example.happyplaces.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*



class AddHappyPlaceActivity : AppCompatActivity(),  View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalSorage : Uri? = null
    private var mlatitude : Double = 0.0
    private var mlongitude : Double = 0.0
    private var mHappyPlaceDetails : HappyPlaceModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        val toolbar_add_place = findViewById<Toolbar>(R.id.toolbar_add_place)
        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddHappyPlaceActivity,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(
                MainActivity.EXTRA_PLACE_DETAILS
            ) as HappyPlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
         view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()
         if(mHappyPlaceDetails != null){
             supportActionBar?.title = "Edit Happy Place"
             var et_title = findViewById<AppCompatEditText>(R.id.et_title)
             var et_description = findViewById<AppCompatEditText>(R.id.et_description)
             var et_location = findViewById<AppCompatEditText>(R.id.et_location)
             var et_date = findViewById<AppCompatEditText>(R.id.et_date)
             var iv_place_iamge = findViewById<AppCompatImageView>(R.id.iv_place_image)
             var btn_save = findViewById<Button>(R.id.btn_save)

             et_title.setText(mHappyPlaceDetails!!.title)
             et_description.setText(mHappyPlaceDetails!!.description)
             et_date.setText(mHappyPlaceDetails!!.date)
             et_location.setText(mHappyPlaceDetails!!.location)
             mlatitude = mHappyPlaceDetails!!.latitude
             mlatitude = mHappyPlaceDetails!!.longitude
             iv_place_iamge.setImageURI(saveImageToInternalSorage)
             btn_save.text = "UPDATE"

             saveImageToInternalSorage = Uri.parse(
                 mHappyPlaceDetails!!.image)

         }


        var et_date = findViewById<AppCompatEditText>(R.id.et_date)
        et_date.setOnClickListener(this)
        var tv_add_image = findViewById<TextView>(R.id.tv_add_image)
        tv_add_image.setOnClickListener(this)
        var btn_save = findViewById<TextView>(R.id.btn_save)
        btn_save.setOnClickListener(this)
        var et_location = findViewById<AppCompatEditText>(R.id.et_location)
        et_location.setOnClickListener(this)
        var tv_select_current_location = findViewById<TextView>(R.id.tv_select_current_location)
        tv_select_current_location.setOnClickListener(this)
    }

    private  fun isLocationEnabled() : Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallBack, Looper.myLooper())
    }

    private  val mLocationCallBack = object  : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mlatitude = mLastLocation!!.latitude
            Log.i("Current Lattitude", "$mlatitude")
            mlongitude = mLastLocation.longitude
            Log.i("Current Longitude", "$mlongitude")

            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, mlatitude, mlongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener{
             override fun   onAddressFound(address:String?){
                 val et_location = findViewById<AppCompatEditText>(R.id.et_location)
                 et_location.setText(address)
             }

            override fun onError(){
                Log.e("Get Address:: ", "Something went wrong")
            }

            })
            addressTask.getAddress()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from gallery",
                    "Capture photo from camera"
                )
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save -> {
                val et_title = findViewById<AppCompatEditText>(R.id.et_title)
                val et_description = findViewById<AppCompatEditText>(R.id.et_description)
                val et_location = findViewById<AppCompatEditText>(R.id.et_location)
                when{
                      et_title.text.isNullOrEmpty() ->{
                      Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.isNullOrEmpty() ->{
                        Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a Location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalSorage == null ->{
                        Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
                    }else ->{
                    val et_date = findViewById<AppCompatEditText>(R.id.et_date)
                    val happyPlaceModel = HappyPlaceModel(
                            if(mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            et_title.text.toString(),
                            saveImageToInternalSorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mlatitude,
                            mlongitude
                        )
                    val dbHandler = DatabaseHandler(this)
                    if (mHappyPlaceDetails == null){
                        val addHappyPlce = dbHandler.addHappyPlace(happyPlaceModel)
                        if (addHappyPlce > 0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }else{
                        val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                        if (updateHappyPlace > 0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                 }
               }
            }

            R.id.et_location ->{
                try{
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

            R.id.tv_select_current_location ->{

                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // This will redirect you to settings from where you need to turn on the location provider.
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {
                                    requestNewLocationData()
                                }
                            }
                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogForPermissions()
                            }
                        }).onSameThread()
                        .check()
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY){
                if (data != null){
                    val contentURI = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        saveImageToInternalSorage = saveImagesToInternalStorage(selectedImageBitmap)
                        Log.e("Save image", "Path :: $saveImageToInternalSorage")

                        var iv_place_iamge = findViewById<AppCompatImageView>(R.id.iv_place_image)
                        iv_place_iamge.setImageBitmap(selectedImageBitmap)
                    }catch(e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this,
                            "Failed to load the image from gallery",
                            Toast.LENGTH_SHORT).show()

                    }
                }
            }
            else if(requestCode == CAMERA){
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalSorage = saveImagesToInternalStorage(thumbnail)
                Log.e("Save image", "Path :: $saveImageToInternalSorage")

                var iv_place_iamge = findViewById<AppCompatImageView>(R.id.iv_place_image)
                iv_place_iamge.setImageBitmap(thumbnail)
            }
            else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place : Place = Autocomplete.getPlaceFromIntent(data!!)
                val et_location = findViewById<AppCompatEditText>(R.id.et_location)
                et_location.setText(place.address)
                mlatitude = place.latLng!!.latitude
                mlatitude = place.latLng!!.longitude
            }
        }
    }
    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(
                report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent,   CAMERA)
                }
            }override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken)
            {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }


    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(
                report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){
                   val galleryIntent = Intent(Intent.ACTION_PICK,
                       MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent,   GALLERY)
                }
            }override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken)
            {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }
           private fun showRationalDialogForPermissions(){
                     AlertDialog.Builder(this).setMessage("It looks like you turned off permission required for this feature. It can be enabled under the application settings")
                         .setPositiveButton("GO TO SETTINGS")
                         {_, _ ->
                             try{
                                 val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                 val uri = Uri.fromParts("package", packageName, null)
                                 intent.data = uri
                                 startActivity(intent)
                             }catch (e: ActivityNotFoundException){
                                 e.printStackTrace()
                             }
                         }.setNegativeButton("Cancel"){dialog, _ ->
                             dialog.dismiss()
                         }.show()

                }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        var et_date = findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.et_date)
        et_date.setText(sdf.format(cal.time).toString())
    }

    private fun saveImagesToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(application)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try{
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e : IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }

}
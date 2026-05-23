package com.example.campusbustracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var userRole = "student"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_map
        )


        // FIREBASE
        auth =
            FirebaseAuth.getInstance()

        firestore =
            FirebaseFirestore.getInstance()

        loadUserRole()


        // MAP
        val mapFragment =
            supportFragmentManager.findFragmentById(
                R.id.mapFragment
            ) as SupportMapFragment

        mapFragment.getMapAsync(this)


        // ZOOM IN
        findViewById<TextView>(
            R.id.zoomInBtn
        ).setOnClickListener {

            googleMap.animateCamera(
                CameraUpdateFactory.zoomIn()
            )
        }


        // ZOOM OUT
        findViewById<TextView>(
            R.id.zoomOutBtn
        ).setOnClickListener {

            googleMap.animateCamera(
                CameraUpdateFactory.zoomOut()
            )
        }


        // CURRENT LOCATION BUTTON
        findViewById<CardView>(
            R.id.currentLocationBtn
        ).setOnClickListener {

            getLiveLocation()
        }


        // SEARCH
        val searchEt =
            findViewById<EditText>(
                R.id.searchEt
            )


        if (!Places.isInitialized()) {

            Places.initialize(
                applicationContext,
                "AIzaSyDnjEF-DBxKE0G4t-gdtapoelaioTJ8U_4"
            )
        }


        searchEt.setOnClickListener {

            val fields =
                listOf(
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )


            val intent =
                Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    fields
                ).build(this)

            startActivityForResult(
                intent,
                100
            )
        }


        BottomNavigationManager.setup(
            this,
            findViewById(R.id.footer)
        )
    }



    private fun loadUserRole() {

        val uid =
            auth.currentUser?.uid
                ?: return


        firestore.collection("users")
            .document(uid)
            .get()

            .addOnSuccessListener { document ->

                userRole =
                    document.getString(
                        "role"
                    ) ?: "student"
            }
    }



    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )


        if (requestCode == 100) {

            if (
                resultCode == RESULT_OK &&
                data != null
            ) {

                val place =
                    Autocomplete.getPlaceFromIntent(
                        data
                    )

                val latLng =
                    place.latLng ?: return


                googleMap.clear()


                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(place.name)
                )


                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        16f
                    )
                )


                findViewById<EditText>(
                    R.id.searchEt
                ).setText(
                    place.name
                )
            }
        }
    }



    override fun onMapReady(
        map: GoogleMap
    ) {

        googleMap = map

        getLiveLocation()
    }



    private fun getLiveLocation() {

        val fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(
                    this
                )


        if (

            ContextCompat.checkSelfPermission(

                this,

                Manifest.permission.ACCESS_FINE_LOCATION

            ) != PackageManager.PERMISSION_GRANTED

        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )

            return
        }


        fusedLocationClient.lastLocation

            .addOnSuccessListener { location ->

                if (location != null) {

                    val latLng =
                        LatLng(
                            location.latitude,
                            location.longitude
                        )


                    googleMap.clear()


                    // DRIVER = BUS ICON
                    if (
                        userRole == "driver"
                    ) {

                        googleMap.isMyLocationEnabled =
                            false


                        googleMap.addMarker(

                            MarkerOptions()

                                .position(
                                    latLng
                                )

                                .title(
                                    "Your Bus"
                                )

                                .icon(

                                    bitmapFromDrawable(
                                        R.drawable.ic_bus_marker
                                    )
                                )
                        )

                    }

                    // STUDENT = BLUE DOT
                    else {

                        googleMap.isMyLocationEnabled =
                            true
                    }


                    googleMap.animateCamera(

                        CameraUpdateFactory
                            .newLatLngZoom(

                                latLng,

                                17f
                            )
                    )


                    val geocoder =
                        Geocoder(
                            this,
                            Locale.getDefault()
                        )


                    val addresses =
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )


                    if (
                        !addresses.isNullOrEmpty()
                    ) {

                        findViewById<TextView>(
                            R.id.addressTxt
                        ).text =

                            addresses[0]
                                .getAddressLine(0)
                    }
                }
            }
    }



    private fun bitmapFromDrawable(
        drawableId: Int
    ) =

        ContextCompat.getDrawable(
            this,
            drawableId
        )?.let { drawable ->

            // SVG MARKER SIZE
            val width = 120
            val height = 120

            val bitmap =
                Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
                )

            val canvas =
                Canvas(bitmap)

            drawable.setBounds(
                0,
                0,
                width,
                height
            )

            drawable.draw(canvas)

            BitmapDescriptorFactory
                .fromBitmap(bitmap)
        }

    override fun onResume() {

        super.onResume()

        ActivityCompat.requestPermissions(

            this,

            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),

            101
        )
    }

    override fun onRequestPermissionsResult(

        requestCode: Int,

        permissions: Array<out String>,

        grantResults: IntArray

    ) {

        super.onRequestPermissionsResult(

            requestCode,

            permissions,

            grantResults
        )


        if (

            requestCode == 101 &&

            grantResults.isNotEmpty() &&

            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED

        ) {

            getLiveLocation()
        }
    }
}
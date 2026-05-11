package com.example.campusbustracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler

import androidx.core.content.ContextCompat
import android.content.Intent

import com.google.android.gms.maps.model.BitmapDescriptor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.google.maps.android.PolyUtil

import org.json.JSONObject

import java.net.URL
import java.util.Locale


class RouteMapActivity :
    AppCompatActivity(),
    OnMapReadyCallback {


    private lateinit var googleMap:
            GoogleMap

    private lateinit var firestore:
            FirebaseFirestore

    private lateinit var auth:
            FirebaseAuth

    private var userRole = "student"

    private var routeId = ""

    private var driverId = ""

    private var myBusMarker:
            Marker? = null


    private var remoteBusMarker:
            Marker? = null


    private val tripStops =
        mutableListOf<String>()


    private var currentStopIndex = 0


    private var tripStarted = false

    private lateinit var etaHandler:
            android.os.Handler


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_route_map
        )


        firestore =
            FirebaseFirestore.getInstance()

        auth =
            FirebaseAuth.getInstance()


        routeId =

            intent.getStringExtra(
                "routeId"
            ) ?: ""


        driverId =

            intent.getStringExtra(
                "driverId"
            ) ?: ""


        if (
            routeId.isEmpty()
        ) {

            firestore.collection(
                "activeTrips"
            )

                .document(
                    auth.currentUser!!.uid
                )

                .get()

                .addOnSuccessListener { document ->

                    routeId =

                        document.getString(
                            "routeId"
                        ) ?: ""
                }
        }

        val rootLayout =

            findViewById<android.widget.LinearLayout>(
                R.id.rootLayout
            )


        rootLayout.alpha =
            0f

        etaHandler =

            android.os.Handler(
                mainLooper
            )

        firestore =
            FirebaseFirestore.getInstance()

        auth =
            FirebaseAuth.getInstance()


        findViewById<ImageView>(
            R.id.backBtn
        ).setOnClickListener {

            finish()
        }


        BottomNavigationManager.setup(
            this,
            findViewById(R.id.footer)
        )

        setupTripButtons()

        findViewById<androidx.cardview.widget.CardView>(
            R.id.tripCard
        ).alpha = 0f

        val mapFragment =

            supportFragmentManager
                .findFragmentById(
                    R.id.mapFragment
                ) as SupportMapFragment


        mapFragment.getMapAsync(
            this
        )
    }



    override fun onMapReady(
        map: GoogleMap
    ) {

        googleMap =
            map


        drawRoute()


        loadUserRole()
    }



    private fun loadUserRole() {

        val uid =
            auth.currentUser?.uid
                ?: return


        val backBtn =
            findViewById<ImageView>(
                R.id.backBtn
            )


        val tripBtn =
            findViewById<android.widget.TextView>(
                R.id.tripBtn
            )


        val updateStatusBtn =
            findViewById<android.widget.TextView>(
                R.id.updateStatusBtn
            )

        val statusTxt =
            findViewById<android.widget.TextView>(
                R.id.statusTxt
            )


        val statusNoteTxt =
            findViewById<android.widget.TextView>(
                R.id.statusNoteTxt
            )

        val rootLayout =
            findViewById<android.widget.LinearLayout>(
                R.id.rootLayout
            )



        firestore.collection(
            "users"
        )

            .document(uid)

            .get()

            .addOnSuccessListener { document ->


                userRole =

                    document.getString(
                        "role"
                    ) ?: "student"



                if (
                    userRole == "student"
                ) {

                    backBtn.setImageResource(
                        R.drawable.ic_back_p
                    )


                    tripBtn.visibility =
                        android.view.View.GONE


                    updateStatusBtn.visibility =
                        android.view.View.GONE

                    statusTxt.visibility =
                        android.view.View.VISIBLE


                    statusNoteTxt.visibility =
                        android.view.View.VISIBLE


                }

                else {

                    backBtn.setImageResource(
                        R.drawable.ic_back_g
                    )


                    tripBtn.visibility =
                        android.view.View.VISIBLE


                    updateStatusBtn.visibility =
                        android.view.View.VISIBLE

                    statusTxt.visibility =
                        android.view.View.GONE


                    statusNoteTxt.visibility =
                        android.view.View.GONE
                }



                askLocationPermission()

                showDriverLiveLocation()

                loadTripState()



            }
    }



    private fun askLocationPermission() {

        ActivityCompat.requestPermissions(

            this,

            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),

            100
        )
    }



    override fun onRequestPermissionsResult(

        requestCode: Int,

        permissions: Array<out String>,

        grantResults: IntArray

    ) {

        if (
            requestCode == 100
        ) {

            if (

                grantResults.isNotEmpty()

                &&

                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED

            ) {

                enableLocation()
            }
        }

        super.onRequestPermissionsResult(

            requestCode,

            permissions,

            grantResults
        )
    }



    private fun enableLocation() {

        if (

            ActivityCompat.checkSelfPermission(

                this,

                Manifest.permission.ACCESS_FINE_LOCATION

            )

            !=

            PackageManager.PERMISSION_GRANTED

        ) return


        // STUDENT BLUE DOT
        if (
            userRole == "student"
        ) {

            googleMap.isMyLocationEnabled =
                true


            startStudentLiveLocation()
        }



        // DRIVER
        if (
            userRole == "driver"
        ) {

            startDriverLiveLocation()
        }
    }


    private fun startStudentLiveLocation() {

        if (

            ActivityCompat.checkSelfPermission(

                this,

                Manifest.permission.ACCESS_FINE_LOCATION

            )

            !=

            PackageManager.PERMISSION_GRANTED

        ) return



        val fusedClient =

            LocationServices
                .getFusedLocationProviderClient(
                    this
                )



        val request =

            com.google.android.gms.location.LocationRequest.Builder(

                com.google.android.gms.location.Priority
                    .PRIORITY_HIGH_ACCURACY,

                5000L

            ).build()



        val callback =

            object :
                com.google.android.gms.location.LocationCallback() {

                override fun onLocationResult(
                    result: com.google.android.gms.location.LocationResult
                ) {

                    val location =

                        result.lastLocation
                            ?: return



                    val point =

                        LatLng(

                            location.latitude,

                            location.longitude
                        )



                    // SAVE TO FIRESTORE
                    firestore.collection(
                        "users"
                    )

                        .document(
                            auth.currentUser!!.uid
                        )

                        .update(

                            "latitude",
                            location.latitude,

                            "longitude",
                            location.longitude
                        )



                    // FOLLOW STUDENT
                    googleMap.animateCamera(

                        CameraUpdateFactory
                            .newLatLngZoom(

                                point,

                                16f
                            )
                    )
                }
            }



        fusedClient.requestLocationUpdates(

            request,

            callback,

            mainLooper
        )
    }


    private fun drawRoute() {

        val locations =

            intent.getStringArrayListExtra(
                "locations"
            ) ?: return


        val geocoder =

            Geocoder(
                this,
                Locale.getDefault()
            )


        val points =
            mutableListOf<LatLng>()

        tripStops.clear()

        tripStops.addAll(
            locations
        )

        for (
        name in locations
        ) {

            val address =

                geocoder.getFromLocationName(
                    name,
                    1
                )


            if (
                !address.isNullOrEmpty()
            ) {

                val point =

                    LatLng(

                        address[0].latitude,

                        address[0].longitude
                    )


                points.add(
                    point
                )


                googleMap.addMarker(

                    MarkerOptions()

                        .position(
                            point
                        )

                        .title(
                            name
                        )
                )
            }
        }


        if (
            points.size >= 2
        ) {

            drawRoadRoute(
                points
            )


            googleMap.animateCamera(

                CameraUpdateFactory
                    .newLatLngZoom(

                        points[0],

                        13f
                    )
            )
        }
    }



    private fun drawRoadRoute(
        points: List<LatLng>
    ) {

        val apiKey =
            getString(
                R.string.google_maps_key
            )


        val origin =

            "${points.first().latitude}," +
                    "${points.first().longitude}"


        val destination =

            "${points.last().latitude}," +
                    "${points.last().longitude}"


        // ALL MIDDLE STOPS
        val waypoints =

            points.drop(1)

                .dropLast(1)

                .joinToString("|") {

                    "${it.latitude},${it.longitude}"
                }



        val url =

            "https://maps.googleapis.com/maps/api/directions/json?" +

                    "origin=$origin&" +

                    "destination=$destination&" +

                    "waypoints=$waypoints&" +

                    "mode=driving&" +

                    "key=$apiKey"



        Thread {

            try {

                val response =

                    java.net.URL(url)
                        .readText()


                val polyline =

                    org.json.JSONObject(
                        response
                    )

                        .getJSONArray(
                            "routes"
                        )

                        .getJSONObject(0)

                        .getJSONObject(
                            "overview_polyline"
                        )

                        .getString(
                            "points"
                        )



                val route =

                    PolyUtil.decode(
                        polyline
                    )



                runOnUiThread {

                    googleMap.addPolyline(

                        PolylineOptions()

                            .addAll(
                                route
                            )

                            .width(
                                12f
                            )
                    )
                }

            }

            catch (
                e: Exception
            ) {

                e.printStackTrace()
            }

        }.start()
    }

    private fun startDriverLiveLocation() {

        if (

            ActivityCompat.checkSelfPermission(

                this,

                Manifest.permission.ACCESS_FINE_LOCATION

            )

            !=

            PackageManager.PERMISSION_GRANTED

        ) return



        val fusedClient =

            LocationServices
                .getFusedLocationProviderClient(
                    this
                )



        val request =

            com.google.android.gms.location.LocationRequest.Builder(

                com.google.android.gms.location.Priority
                    .PRIORITY_HIGH_ACCURACY,

                5000L

            ).build()



        val callback =

            object :
                com.google.android.gms.location.LocationCallback() {

                override fun onLocationResult(
                    result: com.google.android.gms.location.LocationResult
                ) {

                    val location =

                        result.lastLocation
                            ?: return



                    val point =

                        LatLng(

                            location.latitude,

                            location.longitude
                        )



                    // SAVE TO FIRESTORE
                    firestore.collection(
                        "users"
                    )

                        .document(
                            auth.currentUser!!.uid
                        )

                        .update(

                            "latitude",
                            location.latitude,

                            "longitude",
                            location.longitude
                        )



                    // UPDATE DRIVER MARKER
                    myBusMarker?.remove()


                    myBusMarker =

                        googleMap.addMarker(

                            MarkerOptions()

                                .position(
                                    point
                                )

                                .title(
                                    "Your Bus"
                                )

                                .icon(

                                    getBusMarkerBitmap()
                                )
                        )

                    googleMap.animateCamera(

                        CameraUpdateFactory
                            .newLatLngZoom(

                                point,

                                16f
                            )
                    )
                }
            }



        fusedClient.requestLocationUpdates(

            request,

            callback,

            mainLooper
        )
    }

    private fun getBusMarkerBitmap():

            BitmapDescriptor {

        val drawable =

            ContextCompat.getDrawable(

                this,

                R.drawable.ic_bus_marker
            ) ?: return BitmapDescriptorFactory.defaultMarker()


        val bitmap =

            Bitmap.createBitmap(

                96,

                96,

                Bitmap.Config.ARGB_8888
            )


        val canvas =
            Canvas(bitmap)


        drawable.setBounds(

            0,

            0,

            canvas.width,

            canvas.height
        )


        drawable.draw(
            canvas
        )


        return BitmapDescriptorFactory
            .fromBitmap(
                bitmap
            )
    }

    private fun showDriverLiveLocation() {

        val driverId =

            intent.getStringExtra(
                "driverId"
            ) ?: return


        firestore.collection(
            "users"
        )

            .document(
                driverId
            )

            .addSnapshotListener { document, _ ->


                if (
                    document == null
                ) return@addSnapshotListener



                val lat =

                    document.getDouble(
                        "latitude"
                    ) ?: return@addSnapshotListener



                val lng =

                    document.getDouble(
                        "longitude"
                    ) ?: return@addSnapshotListener



                val point =

                    LatLng(
                        lat,
                        lng
                    )



                runOnUiThread {


                    remoteBusMarker?.remove()



                    remoteBusMarker =

                        googleMap.addMarker(

                            MarkerOptions()

                                .position(
                                    point
                                )

                                .title(
                                    "Driver Bus"
                                )

                                .icon(
                                    getBusMarkerBitmap()
                                )
                        )



                    // STUDENT FOLLOW DRIVER
                    if (
                        userRole == "student"
                    ) {

                        googleMap.animateCamera(

                            CameraUpdateFactory
                                .newLatLngZoom(

                                    point,

                                    16f
                                )
                        )
                    }
                }
            }
    }

    private fun setupTripButtons() {

        val nextStopTxt =
            findViewById<android.widget.TextView>(
                R.id.nextStopTxt
            )


        val etaTxt =
            findViewById<android.widget.TextView>(
                R.id.etaTxt
            )


        val tripBtn =
            findViewById<android.widget.TextView>(
                R.id.tripBtn
            )


        val updateStatusBtn =

            findViewById<android.widget.TextView>(
                R.id.updateStatusBtn
            )



        updateStatusBtn.setOnClickListener {
            android.util.Log.d(
                "ROUTE_DEBUG",
                "Route ID = $routeId"
            )
            startActivity(

                Intent(

                    this,

                    TripStatusActivity::class.java

                )

                    .putExtra(

                        "routeId",

                        routeId
                    )

                    .putExtra(

                        "driverId",

                        driverId
                    )
            )
        }



        // DO NOT RESET HERE
        // Firestore will restore trip state



        tripBtn.setOnClickListener {


            // START TRIP
            if (
                !tripStarted
            ) {

                tripStarted = true


                // START LOCATION = INDEX 0
                if (
                    currentStopIndex == 0
                ) {

                    currentStopIndex = 1
                }



                showNextStop()



                firestore.collection(
                    "activeTrips"
                )

                    .document(
                        auth.currentUser!!.uid
                    )

                    .set(

                        hashMapOf(

                            "routeId" to routeId,

                            "started" to true,

                            "currentStop" to currentStopIndex
                        )
                    )



                android.widget.Toast.makeText(

                    this,

                    "Trip Started",

                    android.widget.Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }



            // NEXT STOP
            currentStopIndex++



            // END TRIP
            if (
                currentStopIndex >= tripStops.size
            ) {

                tripStarted = false

                currentStopIndex = 0



                showNextStop()



                etaTxt.text =
                    "--"



                firestore.collection(
                    "activeTrips"
                )

                    .document(
                        auth.currentUser!!.uid
                    )

                    .delete()

                firestore.collection(
                    "tripStatus"
                )

                    .whereEqualTo(
                        "driverId",
                        auth.currentUser!!.uid
                    )

                    .get()

                    .addOnSuccessListener { result ->

                        for (
                        document in result.documents
                        ) {

                            document.reference.delete()
                        }
                    }


                firestore.collection(
                    "droppedStudents"
                )

                    .whereEqualTo(
                        "driverId",
                        auth.currentUser!!.uid
                    )

                    .get()

                    .addOnSuccessListener { result ->

                        for (
                        document in result.documents
                        ) {

                            document.reference.delete()
                        }
                    }

                // CLEAR TOTAL SEAT BOOKED
                firestore.collection(
                    "totalSeatBooked"
                )

                    .whereEqualTo(
                        "driverId",
                        auth.currentUser!!.uid
                    )

                    .get()

                    .addOnSuccessListener { result ->

                        for (
                        document in result.documents
                        ) {

                            document.reference.delete()
                        }
                    }



                android.widget.Toast.makeText(

                    this,

                    "Trip Ended",

                    android.widget.Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }



            // SAVE PROGRESS
            firestore.collection(
                "activeTrips"
            )

                .document(
                    auth.currentUser!!.uid
                )

                .update(

                    "currentStop",
                    currentStopIndex
                )



            showNextStop()
        }
    }

    private fun showNextStop() {

        if (
            currentStopIndex >= tripStops.size
        ) return


        val nextStopTxt =
            findViewById<android.widget.TextView>(
                R.id.nextStopTxt
            )


        val etaTxt =
            findViewById<android.widget.TextView>(
                R.id.etaTxt
            )


        val nextStop =

            tripStops[
                currentStopIndex
            ]


        nextStopTxt.text =
            nextStop

        val tripBtn =
            findViewById<android.widget.TextView>(
                R.id.tripBtn
            )



        if (
            !tripStarted
        ) {

            tripBtn.text =
                "Start Trip"

            tripBtn.setBackgroundResource(
                R.drawable.trip_btn_green
            )

        }

        else if (

            currentStopIndex ==
            tripStops.size - 1

        ) {

            tripBtn.text =
                "End Trip"

            tripBtn.setBackgroundResource(
                R.drawable.trip_btn_red
            )

        }

        else {

            tripBtn.text =
                "Reached Stop"

            tripBtn.setBackgroundResource(
                R.drawable.trip_btn_green
            )
        }

        calculateEta(
            nextStop
        )


        firestore.collection(
            "activeTrips"
        )

            .document(
                auth.currentUser!!.uid
            )

            .set(

                hashMapOf(

                    "routeId" to routeId,

                    "currentStop" to currentStopIndex,

                    "nextStop" to nextStop
                )
            )
    }




    private fun calculateEta(
        destinationName: String
    ) {

        val etaTxt =
            findViewById<android.widget.TextView>(
                R.id.etaTxt
            )


        val uid =
            auth.currentUser!!.uid


        firestore.collection(
            "users"
        )

            .document(
                uid
            )

            .get()

            .addOnSuccessListener { document ->


                val currentLat =

                    document.getDouble(
                        "latitude"
                    ) ?: return@addOnSuccessListener


                val currentLng =

                    document.getDouble(
                        "longitude"
                    ) ?: return@addOnSuccessListener



                val geocoder =

                    Geocoder(
                        this,
                        Locale.getDefault()
                    )



                val address =

                    geocoder.getFromLocationName(
                        destinationName,
                        1
                    )



                if (
                    address.isNullOrEmpty()
                ) return@addOnSuccessListener



                val destinationLat =
                    address[0].latitude


                val destinationLng =
                    address[0].longitude



                val apiKey =
                    getString(
                        R.string.google_maps_key
                    )



                val url =

                    "https://maps.googleapis.com/maps/api/directions/json?" +

                            "origin=$currentLat,$currentLng&" +

                            "destination=$destinationLat,$destinationLng&" +

                            "mode=driving&" +

                            "key=$apiKey"



                Thread {

                    try {

                        val response =

                            java.net.URL(url)
                                .readText()



                        val duration =

                            org.json.JSONObject(
                                response
                            )

                                .getJSONArray(
                                    "routes"
                                )

                                .getJSONObject(0)

                                .getJSONArray(
                                    "legs"
                                )

                                .getJSONObject(0)

                                .getJSONObject(
                                    "duration"
                                )

                                .getString(
                                    "text"
                                )



                        runOnUiThread {

                            etaTxt.text =
                                duration
                        }

                    }

                    catch (
                        e: Exception
                    ) {

                        e.printStackTrace()
                    }

                }.start()
            }



        // REMOVE OLD TIMER
        etaHandler.removeCallbacksAndMessages(
            null
        )


        // REFRESH EVERY 5 SEC
        etaHandler.postDelayed(

            {

                calculateEta(
                    destinationName
                )

            },

            5000
        )
    }

    private fun loadTripStatusForStudent() {

        val statusTxt =
            findViewById<android.widget.TextView>(
                R.id.statusTxt
            )


        val noteTxt =
            findViewById<android.widget.TextView>(
                R.id.statusNoteTxt
            )


        firestore.collection(
            "tripStatus"
        )

            .whereEqualTo(
                "driverId",
                driverId
            )

            .addSnapshotListener { result, _ ->

                if (
                    result == null ||
                    result.documents.isEmpty()
                ) {

                    statusTxt.text =
                        "Active"

                    noteTxt.text =
                        "Bus is running normally"

                    return@addSnapshotListener
                }


                val latestDocument =

                    result.documents

                        .sortedByDescending {

                            it.getLong(
                                "timestamp"
                            ) ?: 0L
                        }

                        .first()


                statusTxt.text =

                    latestDocument.getString(
                        "status"
                    ) ?: "Active"


                noteTxt.text =

                    latestDocument.getString(
                        "note"
                    ) ?: ""
            }
    }

    private fun loadTripState() {

        val rootLayout =
            findViewById<android.widget.LinearLayout>(
                R.id.rootLayout
            )

        val uid =
            auth.currentUser!!.uid


        val tripCard =
            findViewById<androidx.cardview.widget.CardView>(
                R.id.tripCard
            )


        firestore.collection(
            "activeTrips"
        )

            .document(
                uid
            )

            .get()

            .addOnSuccessListener { document ->


                // NO ACTIVE TRIP
                if (
                    !document.exists()
                ) {

                    tripStarted = false

                    currentStopIndex = 0

                }

                else {

                    tripStarted =

                        document.getBoolean(
                            "started"
                        ) ?: false



                    currentStopIndex =

                        document.getLong(
                            "currentStop"
                        )?.toInt() ?: 0
                }



                showNextStop()


                if (
                    userRole == "student"
                ) {

                    loadTripStatusForStudent()
                }


                // SHOW ONLY AFTER REAL DATA LOADED
                tripCard.alpha =
                    1f

                rootLayout.alpha =
                    1f
            }


    }

    private fun reloadLatestRoute() {

        if (
            routeId.isEmpty()
        ) return


        firestore.collection(
            "routes"
        )

            .whereEqualTo(
                "routeId",
                routeId
            )

            .get()

            .addOnSuccessListener { result ->

                if (
                    result.documents.isEmpty()
                ) return@addOnSuccessListener


                val document =
                    result.documents[0]


                val locations =
                    ArrayList<String>()


                val startLocation =

                    document.getString(
                        "startLocation"
                    ) ?: ""


                val endLocation =

                    document.getString(
                        "endLocation"
                    ) ?: ""


                locations.add(
                    startLocation
                )


                val stops =

                    document.get(
                        "stops"
                    ) as? List<HashMap<String, Any>>

                        ?: emptyList()


                for (
                stop in stops
                ) {

                    locations.add(

                        stop["name"].toString()
                    )
                }


                locations.add(
                    endLocation
                )


                intent.putStringArrayListExtra(
                    "locations",
                    locations
                )


                if (
                    ::googleMap.isInitialized
                ) {

                    googleMap.clear()

                    drawRoute()
                }
            }
    }


    override fun onResume() {

        super.onResume()

        reloadLatestRoute()
    }
}
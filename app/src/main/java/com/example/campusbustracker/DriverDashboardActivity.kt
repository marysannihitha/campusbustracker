package com.example.campusbustracker


import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DriverDashboardActivity :
    AppCompatActivity() {

    private lateinit var firestore:
            FirebaseFirestore

    private lateinit var auth:
            FirebaseAuth


    private val tripStops =
        mutableListOf<String>()

    private var currentStopIndex = 0

    private var tripStarted = false

    private var tripStartLat =
        0.0

    private var tripStartLng =
        0.0

    private var routeId = ""


    private lateinit var etaHandler:
            android.os.Handler

    private lateinit var durationHandler:
            android.os.Handler

    private var driverLat = 0.0

    private var driverLng = 0.0

    private var currentDriverLat =
        0.0

    private var currentDriverLng =
        0.0

    private var tripStartTime =
        0L

    private var driverSpeed =
        0f

    private val LOCATION_PERMISSION =
        1001



    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_driver_dashboard
        )

        ActivityCompat.requestPermissions(

            this,

            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),

            LOCATION_PERMISSION
        )

        etaHandler =

            android.os.Handler(
                mainLooper
            )

        durationHandler =

            android.os.Handler(
                mainLooper
            )

        firestore =
            FirebaseFirestore.getInstance()

        auth =
            FirebaseAuth.getInstance()


        setupFooter()

        loadDriverName()

        listenTripState()

        askDriverLocation()

        updateTripAnalyticsVisibility(
            false
        )


// HIDE DASHBOARD CONTENT
        findViewById<android.widget.ScrollView>(
            R.id.mainScrollView
        ).alpha = 0f

        findViewById<android.widget.ProgressBar>(
            R.id.loadingBar
        ).visibility =
            android.view.View.VISIBLE

// ASK LOCATION FIRST



// END TRIP BUTTON
        setupEndTripButton()

        setupStatusButton()
    }



    private fun setupFooter() {

        val footerLayout =
            findViewById<LinearLayout>(
                R.id.footer
            )


        BottomNavigationManager.setup(

            this,

            footerLayout
        )


        val profileBtn =
            footerLayout.findViewById<ImageView>(
                R.id.profileBtn
            )


        profileBtn.setOnClickListener {

            startActivity(

                Intent(

                    this,

                    ProfileActivity::class.java
                )
            )
        }
    }

    private fun loadDriverName() {

        val uid =
            auth.currentUser?.uid
                ?: return


        firestore.collection(
            "users"
        )

            .document(
                uid
            )

            .get()

            .addOnSuccessListener { document ->


                val driverName =

                    document.getString(
                        "name"
                    ) ?: "Driver"



                findViewById<TextView>(
                    R.id.nameTxt
                ).text =

                    "Hello, $driverName! 👋"
            }
    }
    private fun askDriverLocation() {

        if (

            ContextCompat.checkSelfPermission(

                this,

                Manifest.permission.ACCESS_FINE_LOCATION

            )

            != PackageManager.PERMISSION_GRANTED

        ) {

            ActivityCompat.requestPermissions(

                this,

                arrayOf(

                    Manifest.permission.ACCESS_FINE_LOCATION
                ),

                LOCATION_PERMISSION
            )

            return
        }


        val fusedClient =

            LocationServices.getFusedLocationProviderClient(
                this
            )



        val locationRequest =

            com.google.android.gms.location.LocationRequest.create()

                .apply {

                    interval = 5000

                    fastestInterval = 3000

                    priority =

                        com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                }



        val locationCallback =

            object :
                com.google.android.gms.location.LocationCallback() {


                override fun onLocationResult(

                    result:
                    com.google.android.gms.location.LocationResult

                ) {

                    val location =

                        result.lastLocation
                            ?: return



                    // LIVE LOCATION
                    currentDriverLat =
                        location.latitude

                    currentDriverLng =
                        location.longitude


                    driverSpeed =
                        location.speed * 3.6f



                    // UPDATE ANALYTICS LIVE
                    if (
                        tripStarted
                    ) {

                        loadTripAnalytics()
                    }



                    // UPDATE USER LOCATION
                    firestore.collection(
                        "users"
                    )

                        .document(
                            auth.currentUser!!.uid
                        )

                        .update(

                            "latitude",
                            currentDriverLat,

                            "longitude",
                            currentDriverLng
                        )



                    // SAVE LIVE TRIP LOCATION
                    if (
                        tripStarted
                    ) {

                        firestore.collection(
                            "activeTrips"
                        )

                            .document(
                                auth.currentUser!!.uid
                            )

                            .update(

                                "currentLat",
                                currentDriverLat,

                                "currentLng",
                                currentDriverLng
                            )
                    }



                    // UPDATE ETA LIVE
                    if (

                        tripStops.isNotEmpty() &&

                        currentStopIndex < tripStops.size

                    ) {

                        calculateEta(

                            tripStops[
                                currentStopIndex
                            ]
                        )
                    }



                    // FIRST LOAD ONLY
                    if (

                        findViewById<android.widget.ScrollView>(
                            R.id.mainScrollView
                        ).alpha == 0f

                    ) {

                        findViewById<android.widget.ProgressBar>(
                            R.id.loadingBar
                        ).visibility =
                            android.view.View.GONE


                        loadTodayRoute()

                        setupTripButtons()


                        findViewById<android.widget.ScrollView>(
                            R.id.mainScrollView
                        ).alpha = 1f
                    }
                }
            }



        fusedClient.requestLocationUpdates(

            locationRequest,

            locationCallback,

            mainLooper
        )
    }


    private fun loadTodayRoute() {

        val uid =
            auth.currentUser!!.uid


        firestore.collection(
            "routes"
        )

            .whereEqualTo(
                "driverId",
                uid
            )

            .limit(1)

            .get()

            .addOnSuccessListener { result ->


                if (
                    result.documents.isEmpty()
                ) return@addOnSuccessListener


                val document =
                    result.documents[0]


                routeId =

                    document.getString(
                        "routeId"
                    ) ?: ""


                tripStops.clear()


                // START
                tripStops.add(

                    document.getString(
                        "startLocation"
                    ) ?: ""
                )


                // MIDDLE
                val stops =

                    document.get(
                        "stops"
                    ) as? List<HashMap<String, Any>>
                        ?: emptyList()


                for (
                stop in stops
                ) {

                    tripStops.add(

                        stop["name"].toString()
                    )
                }


                // END
                tripStops.add(

                    document.getString(
                        "endLocation"
                    ) ?: ""
                )


                showNextStop()
            }
    }



    private fun listenTripState() {

        val uid =
            auth.currentUser!!.uid


        firestore.collection(
            "activeTrips"
        )

            .document(
                uid
            )

            .addSnapshotListener { document, _ ->


                // NO ACTIVE TRIP
                if (

                    document == null ||

                    !document.exists()

                ) {

                    tripStarted =
                        false


                    currentStopIndex =
                        0


                    tripStartTime =
                        0L


                    tripStartLat =
                        0.0


                    tripStartLng =
                        0.0


                    updateTripAnalyticsVisibility(
                        false
                    )
                }


                // ACTIVE TRIP FOUND
                else {

                    currentStopIndex =

                        document.getLong(
                            "currentStop"
                        )?.toInt() ?: 0



                    if (
                        tripStartTime == 0L
                    ) {

                        tripStartTime =

                            document.getLong(
                                "tripStartTime"
                            ) ?: 0L
                    }



                    tripStartLat =

                        document.getDouble(
                            "tripStartLat"
                        ) ?: 0.0



                    tripStartLng =

                        document.getDouble(
                            "tripStartLng"
                        ) ?: 0.0




                    tripStarted = true

                    startDurationUpdater()

                    updateTripAnalyticsVisibility(
                        true
                    )



                    // REFRESH ANALYTICS IMMEDIATELY
                    if (
                        tripStarted
                    ) {

                        loadTripAnalytics()
                    }
                }



                // UPDATE UI
                showNextStop()
            }
    }


    private fun updateTripAnalyticsVisibility(
        show: Boolean
    ) {

        val visibility =

            if (show)

                android.view.View.VISIBLE

            else

                android.view.View.GONE



        findViewById<android.view.View>(
            R.id.tripAnalyticsCard
        ).visibility = visibility


        findViewById<android.view.View>(
            R.id.metricsSection
        ).visibility = visibility


        findViewById<android.view.View>(
            R.id.passengerTitleTxt
        ).visibility = visibility


        findViewById<android.view.View>(
            R.id.passengerSummaryCard
        ).visibility = visibility


        findViewById<android.view.View>(
            R.id.endTripBtn
        ).visibility = visibility
    }

    private fun setupTripButtons() {

        val tripBtn =
            findViewById<TextView>(
                R.id.tripBtn
            )


        tripBtn.setOnClickListener {


            val uid =
                auth.currentUser!!.uid


            // START
            if (
                !tripStarted
            ) {

                tripStarted = true

                currentStopIndex = 1


                val nextStop =

                    tripStops[
                        currentStopIndex
                    ]

                calculateEta(

                    tripStops[
                        currentStopIndex
                    ]
                )

                tripStartTime =
                    System.currentTimeMillis()

                tripStartLat =
                    currentDriverLat

                tripStartLng =
                    currentDriverLng

                loadTripAnalytics()

                startDurationUpdater()

                firestore.collection(
                    "activeTrips"
                )

                    .document(
                        uid
                    )

                    .set(

                        hashMapOf(

                            "routeId" to routeId,

                            "currentStop" to currentStopIndex,

                            "nextStop" to nextStop,

                            "tripStartTime" to tripStartTime,

                            "tripStartLat" to tripStartLat,

                            "tripStartLng" to tripStartLng,

                            // IMPORTANT
                            "currentLat" to currentDriverLat,

                            "currentLng" to currentDriverLng
                        )
                    )

                return@setOnClickListener
            }



            // NEXT STOP
            currentStopIndex++



            // END TRIP
            if (
                currentStopIndex >= tripStops.size
            ) {

                firestore.collection(
                    "activeTrips"
                )

                    .document(
                        uid
                    )

                    .delete()



                // RESET VARIABLES
                tripStarted = false

                durationHandler.removeCallbacksAndMessages(
                    null
                )

                currentStopIndex = 0

                tripStartTime = 0L

                tripStartLat = 0.0

                tripStartLng = 0.0





                // HIDE ANALYTICS
                updateTripAnalyticsVisibility(
                    false
                )



                // RESET UI VALUES
                findViewById<TextView>(
                    R.id.distanceTxt
                ).text = "0.0 km"


                findViewById<TextView>(
                    R.id.durationTxt
                ).text = "0h 0m"


                findViewById<TextView>(
                    R.id.speedTxt
                ).text = "0 km/h"



                firestore.collection(
                    "droppedStudents"
                )

                    .whereEqualTo(
                        "driverId",
                        uid
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
                        uid
                    )

                    .get()

                    .addOnSuccessListener { result ->

                        for (
                        document in result.documents
                        ) {

                            document.reference.delete()
                        }
                    }



// CLEAR TRIP STATUS
                firestore.collection(
                    "tripStatus"
                )

                    .whereEqualTo(
                        "driverId",
                        uid
                    )

                    .get()

                    .addOnSuccessListener { result ->

                        for (
                        document in result.documents
                        ) {

                            document.reference.delete()
                        }
                    }

                return@setOnClickListener
            }



            val nextStop =

                tripStops[
                    currentStopIndex
                ]


            firestore.collection(
                "activeTrips"
            )

                .document(
                    uid
                )

                .set(

                    hashMapOf(

                        "routeId" to routeId,

                        "currentStop" to currentStopIndex,

                        "nextStop" to nextStop,

                        "tripStartTime" to tripStartTime,

                        "tripStartLat" to tripStartLat,

                        "tripStartLng" to tripStartLng,

                        // IMPORTANT
                        "currentLat" to currentDriverLat,

                        "currentLng" to currentDriverLng
                    )
                )
        }
    }


    private fun loadTripAnalytics() {

        val uid =
            auth.currentUser!!.uid


        // ROUTE DETAILS
        firestore.collection(
            "routes"
        )

            .whereEqualTo(
                "driverId",
                uid
            )

            .limit(1)

            .get()

            .addOnSuccessListener { result ->


                if (
                    result.documents.isEmpty()
                ) return@addOnSuccessListener


                val routeDoc =
                    result.documents[0]



                // ROUTE NAME
                val routeName =

                    routeDoc.getString(
                        "routeName"
                    ) ?: ""


                findViewById<TextView>(
                    R.id.routeNameTxt
                ).text =
                    routeName



                // TOTAL STOPS
                val stops =

                    routeDoc.get(
                        "stops"
                    ) as? List<HashMap<String, Any>>
                        ?: emptyList()


                val totalStops =
                    stops.size + 2


                findViewById<TextView>(
                    R.id.stopProgressTxt
                ).text =

                    "${currentStopIndex + 1} / $totalStops"
            }



        // TRIP STATUS
        firestore.collection(
            "tripStatus"
        )

            .whereEqualTo(
                "driverId",
                uid
            )

            .whereEqualTo(
                "stopNumber",
                currentStopIndex
            )

            .limit(1)

            .get()

            .addOnSuccessListener { result ->


                val status =

                    if (
                        result.documents.isEmpty()
                    )

                        "On Time"

                    else

                        result.documents[0]
                            .getString(
                                "status"
                            ) ?: "On Time"



                findViewById<TextView>(
                    R.id.tripStatusTxt
                ).text =
                    status
            }



        // DISTANCE COVERED
        val distanceKm =

            if (

                tripStartLat != 0.0 &&

                tripStartLng != 0.0 &&

                currentDriverLat != 0.0 &&

                currentDriverLng != 0.0

            ) {

                calculateDistance(

                    tripStartLat,

                    tripStartLng,

                    currentDriverLat,

                    currentDriverLng

                ) / 1000

            }

            else {

                0f
            }


        findViewById<TextView>(
            R.id.distanceTxt
        ).text =

            String.format(

                "%.1f km",

                distanceKm
            )



        val durationMillis =

            if (
                tripStartTime > 0
            )

                System.currentTimeMillis() -
                        tripStartTime

            else

                0L


        val hours =
            durationMillis /
                    (1000 * 60 * 60)

        val minutes =
            (
                    durationMillis /
                            (1000 * 60)
                    ) % 60


        findViewById<TextView>(
            R.id.durationTxt
        ).text =

            "${hours}h ${minutes}m"



        // LIVE SPEED
        val speedKmh =

            if (
                driverSpeed > 0
            )

                driverSpeed.toInt()

            else

                0


        findViewById<TextView>(
            R.id.speedTxt
        ).text =

            "$speedKmh km/h"



        // ON BOARD
        firestore.collection(
            "totalSeatBooked"
        )

            .whereEqualTo(
                "driverId",
                uid
            )

            .get()

            .addOnSuccessListener { result ->


                findViewById<TextView>(
                    R.id.onBoardTxt
                ).text =

                    result.size().toString()
            }



        // DROPPED
        // DROPPED STUDENTS LIVE
        firestore.collection(
            "droppedStudents"
        )

            .whereEqualTo(
                "driverId",
                uid
            )

            .addSnapshotListener { value, _ ->


                val count =

                    value?.documents?.size ?: 0


                findViewById<TextView>(
                    R.id.droppedTxt
                ).text =

                    count.toString()
            }
    }


    private fun startDurationUpdater() {

        durationHandler.removeCallbacksAndMessages(
            null
        )


        durationHandler.post(

            object : Runnable {

                override fun run() {

                    if (
                        tripStarted &&
                        tripStartTime > 0
                    ) {

                        val durationMillis =

                            System.currentTimeMillis() -
                                    tripStartTime


                        val hours =
                            durationMillis /
                                    (1000 * 60 * 60)

                        val minutes =
                            (
                                    durationMillis /
                                            (1000 * 60)
                                    ) % 60


                        findViewById<TextView>(
                            R.id.durationTxt
                        ).text =

                            "${hours}h ${minutes}m"
                    }


                    durationHandler.postDelayed(
                        this,
                        1000
                    )
                }
            }
        )
    }


    private fun showNextStop() {

        if (
            tripStops.isEmpty()
        ) return


        val nextStopTxt =
            findViewById<TextView>(
                R.id.nextStopTxt
            )


        val tripBtn =
            findViewById<TextView>(
                R.id.tripBtn
            )


        if (
            currentStopIndex >= tripStops.size
        ) {

            nextStopTxt.text =
                "--"

            return
        }


        nextStopTxt.text =

            tripStops[
                currentStopIndex
            ]

        val etaTxt =
            findViewById<TextView>(
                R.id.etaTxt
            )





        if (
            currentStopIndex == 0
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
    }

    private fun setupEndTripButton() {

        findViewById<TextView>(
            R.id.endTripBtn
        ).setOnClickListener {


            val uid =
                auth.currentUser!!.uid


            firestore.collection(
                "activeTrips"
            )

                .document(
                    uid
                )

                .delete()



            // RESET LOCAL VARIABLES
            tripStarted = false

            durationHandler.removeCallbacksAndMessages(
                null
            )

            currentStopIndex = 0

            tripStartTime = 0L

            tripStartLat = 0.0

            tripStartLng = 0.0



            driverSpeed = 0f



            // RESET ANALYTICS UI
            findViewById<TextView>(
                R.id.distanceTxt
            ).text = "0.0 km"


            findViewById<TextView>(
                R.id.durationTxt
            ).text = "0h 0m"


            findViewById<TextView>(
                R.id.speedTxt
            ).text = "0 km/h"



            // HIDE ANALYTICS CARDS
            updateTripAnalyticsVisibility(
                false
            )



            // CLEAR DROPPED STUDENTS
            firestore.collection(
                "droppedStudents"
            )

                .whereEqualTo(
                    "driverId",
                    uid
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
                    uid
                )

                .get()

                .addOnSuccessListener { result ->

                    for (
                    document in result.documents
                    ) {

                        document.reference.delete()
                    }
                }



// CLEAR TRIP STATUS
            firestore.collection(
                "tripStatus"
            )

                .whereEqualTo(
                    "driverId",
                    uid
                )

                .get()

                .addOnSuccessListener { result ->

                    for (
                    document in result.documents
                    ) {

                        document.reference.delete()
                    }
                }
        }
    }

    private fun setupStatusButton() {

        findViewById<TextView>(
            R.id.updateStatusBtn
        ).setOnClickListener {

            if (
                !tripStarted
            ) {

                android.widget.Toast.makeText(

                    this,

                    "Start trip first",

                    android.widget.Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }


            val intent =

                android.content.Intent(

                    this,

                    TripStatusActivity::class.java
                )


            intent.putExtra(
                "routeId",
                routeId
            )


            intent.putExtra(
                "driverId",
                auth.currentUser!!.uid
            )


            intent.putExtra(
                "currentStopIndex",
                currentStopIndex - 1
            )


            startActivity(
                intent
            )
        }
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

            requestCode == LOCATION_PERMISSION &&

            grantResults.isNotEmpty() &&

            grantResults[0] == PackageManager.PERMISSION_GRANTED

        ) {

            askDriverLocation()
        }
    }

    private fun calculateEta(
        destinationName: String
    ) {

        val etaTxt =
            findViewById<TextView>(
                R.id.etaTxt
            )


        // LIVE DRIVER LOCATION
        val currentLat =
            currentDriverLat


        val currentLng =
            currentDriverLng


        if (

            currentLat == 0.0 ||

            currentLng == 0.0

        ) {

            etaTxt.text =
                "--"

            return
        }



        val geocoder =

            android.location.Geocoder(

                this,

                java.util.Locale.getDefault()
            )



        val address =

            geocoder.getFromLocationName(

                destinationName,

                1
            )



        if (
            address.isNullOrEmpty()
        ) {

            etaTxt.text =
                "--"

            return
        }



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

                runOnUiThread {

                    etaTxt.text =
                        "--"
                }
            }

        }.start()



        // REFRESH EVERY 5 SECONDS
        etaHandler.removeCallbacksAndMessages(
            null
        )


        etaHandler.postDelayed(

            {

                calculateEta(
                    destinationName
                )

            },

            5000
        )
    }

    override fun onDestroy() {

        etaHandler.removeCallbacksAndMessages(
            null
        )

        durationHandler.removeCallbacksAndMessages(
            null
        )

        super.onDestroy()
    }

    private fun calculateDistance(

        startLat: Double,

        startLng: Double,

        endLat: Double,

        endLng: Double

    ): Float {

        val results =
            FloatArray(1)


        android.location.Location.distanceBetween(

            startLat,

            startLng,

            endLat,

            endLng,

            results
        )


        return results[0]
    }
}

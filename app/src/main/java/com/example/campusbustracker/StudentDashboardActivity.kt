package com.example.campusbustracker

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class StudentDashboardActivity :
    AppCompatActivity() {

    private lateinit var firestore:
            FirebaseFirestore

    private lateinit var auth:
            FirebaseAuth


    private lateinit var arrivalValueTxt:
            TextView

    private lateinit var arrivalSubTxt:
            TextView

    private lateinit var busNumberValueTxt:
            TextView

    private lateinit var locationValueTxt:
            TextView

    private lateinit var seatsValueTxt:
            TextView

    private val LOCATION_PERMISSION =
        1001

    private var studentLat = 0.0
    private var studentLng = 0.0


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_student_dashboard
        )

        findViewById<android.widget.ScrollView>(
            R.id.mainScrollView
        ).alpha = 0f

        firestore =
            FirebaseFirestore.getInstance()

        auth =
            FirebaseAuth.getInstance()


        arrivalValueTxt =
            findViewById(
                R.id.arrivalValueTxt
            )

        arrivalSubTxt =
            findViewById(
                R.id.arrivalSubTxt
            )

        busNumberValueTxt =
            findViewById(
                R.id.busNumberValueTxt
            )

        locationValueTxt =
            findViewById(
                R.id.locationValueTxt
            )

        seatsValueTxt =
            findViewById(
                R.id.seatsValueTxt
            )





        findViewById<ImageView>(
            R.id.menuBtn
        ).setOnClickListener {

            Toast.makeText(

                this,

                "Menu clicked",

                Toast.LENGTH_SHORT

            ).show()
        }


        findViewById<ImageView>(
            R.id.notificationBtn
        ).setOnClickListener {

            Toast.makeText(

                this,

                "Notifications",

                Toast.LENGTH_SHORT

            ).show()
        }




        BottomNavigationManager.setup(

            this,

            findViewById(
                R.id.footer
            )
        )
    }

    override fun onResume() {

        super.onResume()

        checkLocationPermission()
    }

    private fun checkLocationPermission() {

        if (

            ContextCompat.checkSelfPermission(

                this,

                Manifest.permission.ACCESS_FINE_LOCATION

            )

            == PackageManager.PERMISSION_GRANTED

        ) {

            loadStudentData()

            return
        }


        ActivityCompat.requestPermissions(

            this,

            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),

            LOCATION_PERMISSION
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

            requestCode == LOCATION_PERMISSION &&

            grantResults.isNotEmpty() &&

            grantResults[0] == PackageManager.PERMISSION_GRANTED

        ) {

            loadStudentData()
        }

        else {

            // USER CLICKED DENY
            if (

                !ActivityCompat.shouldShowRequestPermissionRationale(

                    this,

                    Manifest.permission.ACCESS_FINE_LOCATION
                )

            ) {

                // DON'T ASK AGAIN
                Toast.makeText(

                    this,

                    "Enable location permission from settings",

                    Toast.LENGTH_LONG

                ).show()


                val intent = android.content.Intent(

                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS

                )

                intent.data = android.net.Uri.parse(

                    "package:$packageName"
                )

                startActivity(intent)
            }

            else {

                // ASK AGAIN
                ActivityCompat.requestPermissions(

                    this,

                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),

                    LOCATION_PERMISSION
                )
            }
        }
    }

    private fun loadStudentData() {

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

            .addOnSuccessListener { userDoc ->


                val studentName =

                    userDoc.getString(
                        "name"
                    ) ?: ""


                findViewById<TextView>(
                    R.id.nameTxt
                ).text =

                    "Hello, $studentName! 👋"



                getStudentLiveLocation()
            }
    }



    private fun getStudentLiveLocation() {


        val fusedLocationClient =

            LocationServices.getFusedLocationProviderClient(
                this
            )


        fusedLocationClient.lastLocation

            .addOnSuccessListener { location ->


                if (
                    location != null
                ) {

                    studentLat =
                        location.latitude

                    studentLng =
                        location.longitude


                    loadNearestRoute()
                }

                else {

                    Toast.makeText(

                        this,

                        "Unable to get location",

                        Toast.LENGTH_SHORT

                    ).show()
                }
            }
    }



    private fun loadNearestRoute() {

        firestore.collection(
            "routes"
        )

            .get()

            .addOnSuccessListener { result ->


                val geocoder =

                    android.location.Geocoder(
                        this,
                        Locale.getDefault()
                    )


                var bestRoute:
                        com.google.firebase.firestore.DocumentSnapshot? = null

                var bestStopName = ""

                var bestDistance =
                    Double.MAX_VALUE


                for (
                routeDoc in result.documents
                ) {


                    val allStops =
                        mutableListOf<String>()


                    // START
                    allStops.add(

                        routeDoc.getString(
                            "startLocation"
                        ) ?: ""
                    )


                    // MIDDLE STOPS
                    val middleStops =

                        routeDoc.get(
                            "stops"
                        ) as? List<HashMap<String, Any>>
                            ?: emptyList()


                    for (
                    stop in middleStops
                    ) {

                        allStops.add(

                            stop["name"].toString()
                        )
                    }


                    // END
                    allStops.add(

                        routeDoc.getString(
                            "endLocation"
                        ) ?: ""
                    )



                    for (
                    stopName in allStops
                    ) {

                        try {

                            val address =

                                geocoder.getFromLocationName(
                                    stopName,
                                    1
                                )


                            if (
                                address.isNullOrEmpty()
                            ) continue



                            val stopLat =
                                address[0].latitude


                            val stopLng =
                                address[0].longitude



                            val distance =

                                calculateDistance(

                                    studentLat,

                                    studentLng,

                                    stopLat,

                                    stopLng
                                )



                            if (
                                distance < bestDistance
                            ) {

                                bestDistance =
                                    distance

                                bestRoute =
                                    routeDoc

                                bestStopName =
                                    stopName
                            }

                        }

                        catch (
                            e: Exception
                        ) {
                        }
                    }
                }



                if (
                    bestRoute == null
                ) {

                    Toast.makeText(

                        this,

                        "No routes found",

                        Toast.LENGTH_SHORT

                    ).show()

                    return@addOnSuccessListener
                }



                val routeDocument =
                    bestRoute


                val driverId =

                    routeDocument.getString(
                        "driverId"
                    ) ?: ""


                findViewById<TextView>(
                    R.id.routeTxt
                ).text =

                    routeDocument.getString(
                        "routeName"
                    ) ?: ""


                findViewById<TextView>(
                    R.id.busTxt
                ).text =

                    routeDocument.getString(
                        "routeId"
                    ) ?: ""



                // SAME LIKE ROUTE MAP ETA
                loadETA(
                    bestStopName
                )


                // BUS NUMBER + LOCATION + SEATS
                loadDriverDetails(
                    driverId
                )
            }
    }



    private fun getLocationFromPlaceName(
        placeName: String
    ): android.location.Address? {

        return try {

            val geocoder =

                android.location.Geocoder(
                    this,
                    Locale.ENGLISH
                )


            // First try exact place
            var addresses =

                geocoder.getFromLocationName(
                    placeName,
                    1
                )


            // If plus code fails, add city
            if (
                addresses.isNullOrEmpty()
            ) {

                addresses =

                    geocoder.getFromLocationName(

                        "$placeName, India",

                        1
                    )
            }


            if (
                addresses.isNullOrEmpty()
            ) {

                null
            }

            else {

                addresses[0]
            }

        } catch (
            e: Exception
        ) {

            null
        }
    }



    private fun loadETA(
        stopName: String
    ) {

        arrivalValueTxt.text =
            "12 min"

        arrivalSubTxt.text =
            stopName
    }



    private fun loadDriverDetails(
        driverId: String
    ) {

        firestore.collection(
            "profiles"
        )

            .document(
                driverId
            )

            .get()

            .addOnSuccessListener { profileDoc ->


                val busNumber =

                    profileDoc.getString(
                        "vehicleNumber"
                    ) ?: ""


                val totalSeats =

                    profileDoc.getString(
                        "totalSeats"
                    )

                        ?.trim()

                        ?.toIntOrNull()

                        ?: 0


                val shareLocation =

                    profileDoc.getBoolean(
                        "shareLiveLocation"
                    ) ?: false


                busNumberValueTxt.text =
                    busNumber



                if (
                    shareLocation
                ) {

                    firestore.collection(
                        "activeTrips"
                    )

                        .document(
                            driverId
                        )

                        .get()

                        .addOnSuccessListener { tripDoc ->


                            val currentLocation =

                                tripDoc.getString(
                                    "currentLocation"
                                ) ?: "Not Visible"


                            locationValueTxt.text =
                                currentLocation


                            // ETA visible only if driver location exists
                            if (
                                currentLocation == "Not Visible"
                            ) {

                                arrivalValueTxt.text =
                                    "Not Visible"

                                arrivalSubTxt.text =
                                    ""
                            }

                            else {

                                loadETA(
                                    currentLocation
                                )
                            }


                            // SHOW DASHBOARD NOW
                            findViewById<android.widget.ScrollView>(
                                R.id.mainScrollView
                            ).alpha = 1f
                        }
                }
                else {

                    locationValueTxt.text =
                        "Not Visible"


                    arrivalValueTxt.text =
                        "Not Visible"

                    arrivalSubTxt.text =
                        ""


                    // SHOW DASHBOARD NOW
                    findViewById<android.widget.ScrollView>(
                        R.id.mainScrollView
                    ).alpha = 1f
                }



                firestore.collection(
                    "totalSeatBooked"
                )

                    .whereEqualTo(
                        "driverId",
                        driverId
                    )

                    .get()

                    .addOnSuccessListener { seatResult ->


                        val bookedSeats =

                            seatResult.documents.size


                        val availableSeats =

                            totalSeats -
                                    bookedSeats


                        seatsValueTxt.text =

                            "$availableSeats/$totalSeats"
                    }
            }
    }



    private fun calculateDistance(

        lat1: Double,
        lng1: Double,

        lat2: Double,
        lng2: Double

    ): Double {

        val results =
            FloatArray(1)


        Location.distanceBetween(

            lat1,
            lng1,

            lat2,
            lng2,

            results
        )


        return results[0].toDouble()
    }
}
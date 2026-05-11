package com.example.campusbustracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

import com.google.firebase.firestore.FirebaseFirestore


class RouteDetailsActivity :
    AppCompatActivity() {


    private lateinit var firestore:
            FirebaseFirestore

    private lateinit var stopsContainer:
            LinearLayout
    private var routeDriverId = ""

    private var activeStopIndex = -1

    private var startLocation = ""

    private var endLocation = ""

    private var studentId = ""

    private var joinedBus = false

    private var userRole = ""

    private lateinit var mainContent: View

    private var stops =
        mutableListOf<HashMap<String, Any>>()


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_route_details
        )


        firestore =
            FirebaseFirestore.getInstance()


        stopsContainer =
            findViewById(
                R.id.stopsContainer
            )

        mainContent = findViewById(
            R.id.mainContent
        )

        mainContent.visibility =
            View.GONE


        val uid =

            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser?.uid
                ?: return


        studentId = uid


        firestore.collection(
            "users"
        )

            .document(
                uid
            )

            .get()

            .addOnSuccessListener { document ->


                userRole =

                    document.getString(
                        "role"
                    ) ?: "student"
            }


        val routeId =

            intent.getStringExtra(
                "routeId"
            ) ?: ""


        // BACK
        findViewById<ImageView>(
            R.id.backBtn
        ).setOnClickListener {

            finish()
        }


        // EDIT
        findViewById<CardView>(
            R.id.editBtn
        ).setOnClickListener {

            val intent = Intent(
                this,
                EditRouteActivity::class.java
            )

            intent.putExtra(
                "routeId",
                routeId
            )

            startActivity(intent)
        }




        // TRACK
        findViewById<CardView>(
            R.id.trackBtn
        ).setOnClickListener {

            val locations =
                ArrayList<String>()


            locations.add(
                startLocation
            )


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




            startActivity(

                android.content.Intent(

                    this,

                    RouteMapActivity::class.java

                )

                    .putStringArrayListExtra(

                        "locations",

                        locations
                    )

                    .putExtra(

                        "driverId",

                        routeDriverId
                    )
            )
        }


        findViewById<CardView>(
            R.id.joinBusBtn
        ).setOnClickListener {

            if (
                joinedBus
            ) {

                dropBus()
            }

            else {

                joinBus()
            }
        }


        // DELETE
        findViewById<CardView>(
            R.id.deleteBtn
        ).setOnClickListener {

            val currentRouteId =

                intent.getStringExtra(
                    "routeId"
                ) ?: return@setOnClickListener


            firestore.collection(
                "routes"
            )

                .whereEqualTo(
                    "routeId",
                    currentRouteId
                )

                .get()

                .addOnSuccessListener { result ->


                    if (
                        result.documents.isNotEmpty()
                    ) {

                        result.documents[0]

                            .reference

                            .delete()

                            .addOnSuccessListener {

                                Toast.makeText(

                                    this,

                                    "Route deleted",

                                    Toast.LENGTH_SHORT

                                ).show()

                                finish()
                            }
                    }
                }
        }


        BottomNavigationManager.setup(

            this,

            findViewById(
                R.id.footer
            )
        )



        // IMPORTANT
        loadRoute(
            routeId
        )
    }



    private fun loadRoute(
        routeId: String
    ) {

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

                routeDriverId =

                    document.getString(
                        "driverId"
                    ) ?: ""

                // STUDENT UI
                if (
                    userRole == "student"
                ) {

                    // TOP ROUTE CIRCLE ONLY
                    findViewById<TextView>(
                        R.id.busCircle
                    ).setBackgroundResource(
                        R.drawable.purple_circle_bg
                    )


                    // BACK BUTTON ONLY
                    findViewById<ImageView>(
                        R.id.backBtn
                    ).setImageResource(
                        R.drawable.ic_back_p
                    )


                    // HIDE DRIVER BUTTONS
                    findViewById<CardView>(
                        R.id.editBtn
                    ).visibility =
                        View.GONE


                    findViewById<CardView>(
                        R.id.deleteBtn
                    ).visibility =
                        View.GONE

                    findViewById<CardView>(
                        R.id.joinBusBtn
                    ).visibility =
                        View.VISIBLE


                    // IMPORTANT
                    checkJoinStatus()
                }

                val currentUserId =

                    com.google.firebase.auth.FirebaseAuth
                        .getInstance()
                        .currentUser?.uid
                        ?: ""


                val editBtn =
                    findViewById<CardView>(
                        R.id.editBtn
                    )


                val deleteBtn =
                    findViewById<CardView>(
                        R.id.deleteBtn
                    )


// ONLY ROUTE CREATOR DRIVER
                if (
                    userRole == "driver" &&
                    currentUserId == routeDriverId
                ) {

                    editBtn.visibility =
                        View.VISIBLE

                    deleteBtn.visibility =
                        View.VISIBLE
                }


// OTHER DRIVERS
                else {

                    editBtn.visibility =
                        View.GONE

                    deleteBtn.visibility =
                        View.GONE
                }

                if (
                    activeStopIndex == -1
                ) {

                    listenTripProgress()
                }

                // ROUTE ID + ROUTE NAME
                val routeIdText =

                    document.getString(
                        "routeId"
                    ) ?: ""



                val routeName =

                    document.getString(
                        "routeName"
                    ) ?: ""



                findViewById<TextView>(
                    R.id.routeIdTxt
                ).text = routeIdText



                findViewById<TextView>(
                    R.id.routeNameTxt
                ).text =

                    routeName


                startLocation =

                    document.getString(
                        "startLocation"
                    ) ?: ""



                endLocation =

                    document.getString(
                        "endLocation"
                    ) ?: ""



                val firebaseStops =

                    document.get(
                        "stops"
                    ) as? List<HashMap<String, Any>>
                        ?: emptyList()



                stops.clear()

                stops.addAll(
                    firebaseStops
                )



                stopsContainer.removeAllViews()


                // COMPLETE STOPS LIST
                val allStops =
                    mutableListOf<HashMap<String, Any>>()



// START LOCATION
                val startStop =
                    hashMapOf<String, Any>()

                startStop["name"] =

                    document.getString(
                        "startLocation"
                    ) ?: ""

                startStop["time"] =

                    document.getString(
                        "departureTime"
                    ) ?: ""

                allStops.add(
                    startStop
                )



// MIDDLE STOPS
                allStops.addAll(
                    stops
                )



// END LOCATION
                val endStop =
                    hashMapOf<String, Any>()

                endStop["name"] =

                    document.getString(
                        "endLocation"
                    ) ?: ""

                endStop["time"] =

                    document.getString(
                        "arrivalTime"
                    ) ?: ""

                allStops.add(
                    endStop
                )




                for (
                (index, stop) in allStops.withIndex()
                ) {


                    val stopView =

                        layoutInflater.inflate(

                            R.layout.item_stop_route,

                            stopsContainer,

                            false
                        )



                    val stopNumber =

                        stopView.findViewById<TextView>(
                            R.id.stopNumber
                        )



                    val stopName =

                        stopView.findViewById<TextView>(
                            R.id.stopNameTxt
                        )



                    val stopTime =

                        stopView.findViewById<TextView>(
                            R.id.stopTimeTxt
                        )



                    val stopStatus =

                        stopView.findViewById<TextView>(
                            R.id.stopStatusTxt
                        )



                    val topLine =

                        stopView.findViewById<View>(
                            R.id.topLine
                        )



                    val bottomLine =

                        stopView.findViewById<View>(
                            R.id.bottomLine
                        )



                    val stopArrow =

                        stopView.findViewById<TextView>(
                            R.id.stopArrow
                        )



                    // NUMBER
                    stopNumber.text =

                        (index + 1).toString()


                    // NUMBER
                    stopNumber.text =
                        (index + 1).toString()


// COMPLETED STOPS


// CURRENT STOP
                    if (
                        index == activeStopIndex
                    ) {

                        stopNumber.setTextColor(
                            Color.WHITE
                        )

                        val greenBg =
                            android.graphics.drawable.GradientDrawable()

                        greenBg.shape =
                            android.graphics.drawable.GradientDrawable.OVAL

                        greenBg.setColor(
                            Color.parseColor(
                                "#16A34A"
                            )
                        )

                        greenBg.setStroke(
                            2,
                            Color.parseColor(
                                "#16A34A"
                            )
                        )

                        stopNumber.background =
                            greenBg
                    }


                    val greenLine =

                        Color.parseColor(
                            "#16A34A"
                        )

                    val defaultLine =

                        Color.parseColor(
                            "#D1D5DB"
                        )


// PASSED + CURRENT STOP = GREEN
                    if (
                        activeStopIndex != -1 &&
                        index <= activeStopIndex
                    ) {

                        topLine.setBackgroundColor(
                            greenLine
                        )

                        bottomLine.setBackgroundColor(
                            greenLine
                        )

                        stopArrow.setTextColor(
                            greenLine
                        )
                    }

// UPCOMING STOPS = DEFAULT COLOR
                    else {

                        topLine.setBackgroundColor(
                            defaultLine
                        )

                        bottomLine.setBackgroundColor(
                            defaultLine
                        )

                        stopArrow.setTextColor(
                            defaultLine
                        )
                    }


                    // LOCATION
                    stopName.text =

                        stop["name"].toString()



                    // TIME
                    stopTime.text =

                        stop["time"].toString()



                    // FIRST = START
                    if (
                        index == 0
                    ) {

                        topLine.visibility =
                            View.INVISIBLE


                        stopStatus.text =
                            "Start"


                        stopView.setPadding(

                            0,

                            12,

                            0,

                            0
                        )
                    }



                    // LAST = END
                    if (
                        index == allStops.size - 1
                    ) {

                        bottomLine.visibility =
                            View.INVISIBLE

                        stopArrow.visibility =
                            View.INVISIBLE

                        stopStatus.text =
                            "End"


                        stopNumber.setTextColor(
                            Color.parseColor(
                                "#5B21B6"
                            )
                        )
                    }



                    stopsContainer.addView(
                        stopView
                    )

                    mainContent.visibility =
                        View.VISIBLE
                }
            }

    }

    private fun listenTripProgress() {

        if (
            routeDriverId.isEmpty()
        ) return


        firestore.collection(
            "activeTrips"
        )

            .document(
                routeDriverId
            )

            .addSnapshotListener { document, _ ->


                if (
                    document == null
                ) return@addSnapshotListener



                activeStopIndex =

                    document.getLong(
                        "currentStop"
                    )?.toInt() ?: -1



                val currentRouteId =

                    intent.getStringExtra(
                        "routeId"
                    ) ?: ""


                loadRoute(
                    currentRouteId
                )
            }
    }

    private fun checkJoinStatus() {

        firestore.collection(
            "totalSeatBooked"
        )

            .whereEqualTo(
                "driverId",
                routeDriverId
            )

            .whereEqualTo(
                "studentId",
                studentId
            )

            .get()

            .addOnSuccessListener { result ->

                joinedBus =
                    result.documents.isNotEmpty()


                updateJoinButton()
            }
    }



    private fun joinBus() {

        firestore.collection(
            "profiles"
        )

            .document(
                routeDriverId
            )

            .get()

            .addOnSuccessListener { driverDoc ->


                val totalSeats =

                    driverDoc.getString(
                        "totalSeats"
                    )?.toInt() ?: 0


                firestore.collection(
                    "totalSeatBooked"
                )

                    .whereEqualTo(
                        "driverId",
                        routeDriverId
                    )

                    .get()

                    .addOnSuccessListener { seatResult ->


                        val bookedCount =

                            seatResult.documents.size


                        if (
                            bookedCount >= totalSeats
                        ) {

                            Toast.makeText(

                                this,

                                "Seats are full, join another bus",

                                Toast.LENGTH_SHORT

                            ).show()

                            return@addOnSuccessListener
                        }


                        val data =
                            hashMapOf<String, Any>()

                        data["driverId"] =
                            routeDriverId

                        data["studentId"] =
                            studentId


                        firestore.collection(
                            "totalSeatBooked"
                        )

                            .add(
                                data
                            )

                            .addOnSuccessListener {

                                joinedBus =
                                    true

                                updateJoinButton()
                            }
                    }
            }
    }



    private fun dropBus() {

        val routeId =

            intent.getStringExtra(
                "routeId"
            ) ?: return


        firestore.collection(
            "routes"
        )

            .whereEqualTo(
                "routeId",
                routeId
            )

            .get()

            .addOnSuccessListener { routeResult ->


                if (
                    routeResult.documents.isEmpty()
                ) {

                    Toast.makeText(

                        this,

                        "Route not found",

                        Toast.LENGTH_SHORT

                    ).show()

                    return@addOnSuccessListener
                }


                val driverId =

                    routeResult.documents[0]

                        .getString(
                            "driverId"
                        ) ?: ""


                if (
                    driverId.isEmpty()
                ) {

                    Toast.makeText(

                        this,

                        "Driver not found",

                        Toast.LENGTH_SHORT

                    ).show()

                    return@addOnSuccessListener
                }


                // DELETE FROM totalSeatBooked
                firestore.collection(
                    "totalSeatBooked"
                )

                    .whereEqualTo(
                        "studentId",
                        studentId
                    )

                    .whereEqualTo(
                        "driverId",
                        driverId
                    )

                    .get()

                    .addOnSuccessListener { result ->


                        for (
                        document in result.documents
                        ) {

                            document.reference.delete()
                        }


                        // SAVE TO droppedStudents
                        val dropData =
                            hashMapOf<String, Any>()

                        dropData["driverId"] =
                            driverId

                        dropData["studentId"] =
                            studentId

                        dropData["timestamp"] =
                            System.currentTimeMillis()


                        firestore.collection(
                            "droppedStudents"
                        )

                            .add(
                                dropData
                            )

                            .addOnSuccessListener {

                                joinedBus =
                                    false

                                updateJoinButton()

                                Toast.makeText(

                                    this,

                                    "Dropped Successfully",

                                    Toast.LENGTH_SHORT

                                ).show()
                            }
                    }
            }
    }



    private fun updateJoinButton() {

        val joinBtn =
            findViewById<CardView>(
                R.id.joinBusBtn
            )


        val joinTxt =
            findViewById<TextView>(
                R.id.joinBusTxt
            )


        if (
            joinedBus
        ) {

            joinBtn.setCardBackgroundColor(
                Color.parseColor(
                    "#EF4444"
                )
            )

            joinTxt.text =
                "🗑   Drop Bus"
        }

        else {

            joinBtn.setCardBackgroundColor(
                Color.parseColor(
                    "#16A34A"
                )
            )

            joinTxt.text =
                "🚌   Join Bus"
        }
    }


    override fun onResume() {

        super.onResume()

        val routeId =

            intent.getStringExtra(
                "routeId"
            ) ?: ""


        loadRoute(routeId)
    }
}
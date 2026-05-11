package com.example.campusbustracker

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RoutesActivity :
    AppCompatActivity() {


    private lateinit var auth:
            FirebaseAuth

    private lateinit var firestore:
            FirebaseFirestore

    private lateinit var routesContainer:
            LinearLayout


    private var userRole =
        "student"



    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_routes
        )


        auth =
            FirebaseAuth.getInstance()

        firestore =
            FirebaseFirestore.getInstance()


        routesContainer =
            findViewById(
                R.id.routesContainer
            )


        val rootLayout =
            findViewById<ConstraintLayout>(
                R.id.rootLayout
            )


        val backBtn =
            findViewById<ImageView>(
                R.id.backBtn
            )


        val headerLayout =
            findViewById<RelativeLayout>(
                R.id.headerLayout
            )


        val addRouteCard =
            findViewById<CardView>(
                R.id.addRouteCard
            )


        rootLayout.alpha = 0f


        // BACK
        backBtn.setOnClickListener {

            finish()
        }


        // USER ROLE
        loadUserRole(

            headerLayout,

            addRouteCard,

            backBtn,

            rootLayout
        )





        BottomNavigationManager.setup(

            this,

            findViewById(
                R.id.footer
            )
        )
    }



    private fun loadUserRole(

        headerLayout: RelativeLayout,

        addRouteCard: CardView,

        backBtn: ImageView,

        rootLayout: ConstraintLayout

    ) {

        val uid =
            auth.currentUser?.uid
                ?: return


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



                // STUDENT
                if (
                    userRole == "student"
                ) {

                    addRouteCard.visibility =
                        View.GONE
                }



                // DRIVER
                else if (
                    userRole == "driver"
                ) {

                    headerLayout.setBackgroundColor(
                        Color.parseColor("#16A34A")
                    )


                    addRouteCard.setCardBackgroundColor(
                        Color.parseColor("#22C55E")
                    )


                    backBtn.setImageResource(
                        R.drawable.ic_back_g
                    )


                    addRouteCard.setOnClickListener {

                        startActivity(

                            Intent(

                                this,

                                AddRouteActivity::class.java
                            )
                        )
                    }
                }


                rootLayout.alpha =
                    1f
            }


            .addOnFailureListener {

                rootLayout.alpha =
                    1f
            }
    }



    private fun loadRoutes() {

        routesContainer.removeAllViews()


        firestore.collection(
            "routes"
        )

            .get()

            .addOnSuccessListener { result ->


                for (
                (index, document) in result.documents.withIndex()
                ) {


                    val routeView =

                        layoutInflater.inflate(

                            R.layout.item_route,

                            routesContainer,

                            false
                        )



                    val routeId =

                        routeView.findViewById<TextView>(
                            R.id.routeId
                        )



                    val routeTitle =

                        routeView.findViewById<TextView>(
                            R.id.routeTitle
                        )



                    val routePath =

                        routeView.findViewById<TextView>(
                            R.id.routePath
                        )



                    val routeInfo =

                        routeView.findViewById<TextView>(
                            R.id.routeInfo
                        )



                    val routeIdText =

                        document.getString(
                            "routeId"
                        ) ?: ""



                    val routeName =

                        document.getString(
                            "routeName"
                        ) ?: ""



                    val startLocation =

                        document.getString(
                            "startLocation"
                        ) ?: ""



                    val endLocation =

                        document.getString(
                            "endLocation"
                        ) ?: ""



                    val stops =

                        document.get(
                            "stops"
                        ) as? List<*>



                    val totalStops =

                        stops?.size ?: 0



                    routeId.text =

                        (index + 1).toString()



                    routeTitle.text =
                        routeName



                    routePath.text =

                        "$startLocation → $endLocation"



                    // REAL DISTANCE + TIME
                    val departureTime =

                        document.getString(
                            "departureTime"
                        ) ?: ""


                    val arrivalTime =

                        document.getString(
                            "arrivalTime"
                        ) ?: ""



                    getDistanceAndDuration(

                        startLocation,

                        endLocation,

                        routeInfo,

                        totalStops,

                        departureTime,

                        arrivalTime
                    )



                    // DRIVER GREEN

                    if (
                        userRole == "driver"
                    ) {

                        val greenCircle =
                            GradientDrawable()

                        greenCircle.shape =
                            GradientDrawable.OVAL

                        greenCircle.setColor(
                            Color.parseColor("#22C55E")
                        )

                        routeId.background =
                            greenCircle
                    }



                    routeView.setOnClickListener {

                        startActivity(

                            android.content.Intent(

                                this,

                                RouteDetailsActivity::class.java

                            ).putExtra(

                                "routeId",

                                routeIdText
                            )
                        )
                    }



                    routesContainer.addView(
                        routeView
                    )
                }
            }
    }

    private fun getDistanceAndDuration(

        startLocation: String,

        endLocation: String,

        routeInfo: TextView,

        totalStops: Int,

        departureTime: String,

        arrivalTime: String

    ) {

        val apiKey =
            getString(
                R.string.google_maps_key
            )


        val url =

            "https://maps.googleapis.com/maps/api/directions/json?" +

                    "origin=$startLocation&" +

                    "destination=$endLocation&" +

                    "key=$apiKey"



        Thread {

            try {

                val response =

                    java.net.URL(url)
                        .readText()


                val json =

                    org.json.JSONObject(
                        response
                    )


                val routes =

                    json.getJSONArray(
                        "routes"
                    )


                if (
                    routes.length() > 0
                ) {

                    val leg =

                        routes.getJSONObject(0)

                            .getJSONArray(
                                "legs"
                            )

                            .getJSONObject(0)



                    val distance =

                        leg.getJSONObject(
                            "distance"
                        )

                            .getString(
                                "text"
                            )



                    // TIME FROM DATABASE
                    val duration =

                        calculateDuration(

                            departureTime,

                            arrivalTime
                        )



                    runOnUiThread {

                        routeInfo.text =

                            "$totalStops Stops     $distance     $duration"
                    }
                }

            }

            catch (
                e: Exception
            ) {

                runOnUiThread {

                    routeInfo.text =

                        "$totalStops Stops"
                }
            }

        }.start()
    }


    private fun calculateDuration(

        startTime: String,

        endTime: String

    ): String {

        try {

            val startParts =
                startTime.split(":")

            val endParts =
                endTime.split(":")


            val startMinutes =

                startParts[0].toInt() * 60 +

                        startParts[1].toInt()



            val endMinutes =

                endParts[0].toInt() * 60 +

                        endParts[1].toInt()



            val totalMinutes =

                endMinutes -
                        startMinutes


            return "$totalMinutes mins"

        }

        catch (
            e: Exception
        ) {

            return "0 mins"
        }
    }


    override fun onResume() {

        super.onResume()

        loadRoutes()
    }
}
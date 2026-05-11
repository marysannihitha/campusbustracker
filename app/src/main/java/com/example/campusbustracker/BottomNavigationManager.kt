package com.example.campusbustracker


import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object BottomNavigationManager {

    fun setup(
        activity: Activity,
        rootView: View
    ) {

        val homeBtn =
            rootView.findViewById<ImageView>(
                R.id.homeBtn
            )

        val mapBtn =
            rootView.findViewById<ImageView>(
                R.id.mapBtn
            )

        val routesBtn =
            rootView.findViewById<ImageView>(
                R.id.routesBtn
            )

        val alertsBtn =
            rootView.findViewById<ImageView>(
                R.id.alertsBtn
            )

        val profileBtn =
            rootView.findViewById<ImageView>(
                R.id.profileBtn
            )


        // HOME
        homeBtn.setOnClickListener {

            val uid =
                FirebaseAuth.getInstance()
                    .currentUser?.uid
                    ?: return@setOnClickListener


            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()

                .addOnSuccessListener { document ->

                    val role =
                        document.getString(
                            "role"
                        ) ?: ""


                    if (role == "driver") {

                        activity.startActivity(
                            Intent(
                                activity,
                                DriverDashboardActivity::class.java
                            )
                        )

                    } else {

                        activity.startActivity(
                            Intent(
                                activity,
                                StudentDashboardActivity::class.java
                            )
                        )
                    }
                }
        }


        // MAP
        mapBtn.setOnClickListener {

            activity.startActivity(
                Intent(
                    activity,
                    MapActivity::class.java
                )
            )
        }


        // ROUTES
        routesBtn.setOnClickListener {

            activity.startActivity(
                Intent(
                    activity,
                    RoutesActivity::class.java
                )
            )
        }


//        // ALERTS
//        alertsBtn.setOnClickListener {
//
//            activity.startActivity(
//                Intent(
//                    activity,
//                    AlertsActivity::class.java
//                )
//            )
//        }


        // PROFILE
        profileBtn.setOnClickListener {

            activity.startActivity(
                Intent(
                    activity,
                    ProfileActivity::class.java
                )
            )
        }

    }
}
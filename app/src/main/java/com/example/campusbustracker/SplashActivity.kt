package com.example.campusbustracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        auth =
            FirebaseAuth.getInstance()

        firestore =
            FirebaseFirestore.getInstance()


        val currentUser =
            auth.currentUser


        // Not logged in
        if (currentUser == null) {

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()

            return
        }


        // Already logged in
        firestore.collection(
            "users"
        )

            .document(
                currentUser.uid
            )

            .get()

            .addOnSuccessListener { document ->

                val role =
                    document.getString(
                        "role"
                    ) ?: ""


                if (
                    role == "student"
                ) {

                    startActivity(
                        Intent(
                            this,
                            StudentDashboardActivity::class.java
                        )
                    )

                }

                else if (
                    role == "driver"
                ) {

                    startActivity(
                        Intent(
                            this,
                            DriverDashboardActivity::class.java
                        )
                    )

                }

                else {

                    startActivity(
                        Intent(
                            this,
                            LoginActivity::class.java
                        )
                    )
                }

                finish()
            }

            .addOnFailureListener {

                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )

                finish()
            }
    }
}
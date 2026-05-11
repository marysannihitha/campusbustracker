package com.example.campusbustracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_login
        )

        auth =
            FirebaseAuth.getInstance()

        firestore =
            FirebaseFirestore.getInstance()


        val emailEt =
            findViewById<EditText>(
                R.id.emailEt
            )

        val passwordEt =
            findViewById<EditText>(
                R.id.passwordEt
            )

        val loginBtn =
            findViewById<Button>(
                R.id.loginBtn
            )

        val registerBtn =
            findViewById<TextView>(
                R.id.registerBtn
            )

        val forgotBtn =
            findViewById<TextView>(
                R.id.forgotPasswordBtn
            )


        loginBtn.setOnClickListener {

            val email =
                emailEt.text
                    .toString()
                    .trim()

            val password =
                passwordEt.text
                    .toString()
                    .trim()


            if (
                !Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {

                emailEt.error =
                    "Enter valid email"

                return@setOnClickListener
            }


            if (
                password.length < 6
            ) {

                passwordEt.error =
                    "Minimum 6 characters"

                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(

                email,
                password

            ).addOnSuccessListener {

                val userId =
                    auth.currentUser!!.uid


                firestore.collection(
                    "users"
                )

                    .document(
                        userId
                    )

                    .get()

                    .addOnSuccessListener { document ->

                        val role =
                            document.getString(
                                "role"
                            ) ?: ""


                        Toast.makeText(

                            this,

                            "Login Success",

                            Toast.LENGTH_SHORT

                        ).show()


                        // later you can open separate dashboards
                        if (
                            role == "student"
                        ) {

                            startActivity(
                                Intent(
                                    this,
                                    StudentDashboardActivity::class.java
                                )
                            )

                            finish()

                        } else if (
                            role == "driver"
                        ) {

                            startActivity(
                                Intent(
                                    this,
                                    DriverDashboardActivity::class.java
                                )
                            )

                            finish()
                        }

                        finish()
                    }

            }.addOnFailureListener {

                Toast.makeText(

                    this,

                    it.message,

                    Toast.LENGTH_LONG

                ).show()
            }
        }


        registerBtn.setOnClickListener {

            startActivity(

                Intent(
                    this,
                    RoleSelectionActivity::class.java
                )
            )
        }


        forgotBtn.setOnClickListener {

            val email =
                emailEt.text
                    .toString()
                    .trim()


            if (
                !Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {

                emailEt.error =
                    "Enter email first"

                return@setOnClickListener
            }


            auth.sendPasswordResetEmail(
                email
            )

                .addOnSuccessListener {

                    Toast.makeText(

                        this,

                        "Reset email sent",

                        Toast.LENGTH_SHORT

                    ).show()
                }

                .addOnFailureListener {

                    Toast.makeText(

                        this,

                        it.message,

                        Toast.LENGTH_LONG

                    ).show()
                }
        }
    }
}
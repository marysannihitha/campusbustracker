package com.example.campusbustracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DriverRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_driver_register
        )

        auth =
            FirebaseAuth.getInstance()

        firestore =
            FirebaseFirestore.getInstance()


        val backBtn =
            findViewById<ImageView>(
                R.id.backBtn
            )

        val registerBtn =
            findViewById<Button>(
                R.id.registerBtn
            )

        val loginBtn =
            findViewById<TextView>(
                R.id.loginBtn
            )

        val termsCheck =
            findViewById<CheckBox>(
                R.id.termsCheck
            )


        val nameEt =
            findViewById<EditText>(
                R.id.nameEt
            )

        val emailEt =
            findViewById<EditText>(
                R.id.emailEt
            )

        val phoneEt =
            findViewById<EditText>(
                R.id.phoneEt
            )

        val licenseEt =
            findViewById<EditText>(
                R.id.licenseEt
            )

        val busEt =
            findViewById<EditText>(
                R.id.busEt
            )

        val passwordEt =
            findViewById<EditText>(
                R.id.passwordEt
            )

        val confirmPasswordEt =
            findViewById<EditText>(
                R.id.confirmPasswordEt
            )


        onBackPressedDispatcher
            .addCallback(

                this,

                object :
                    OnBackPressedCallback(
                        true
                    ) {

                    override fun
                            handleOnBackPressed() {

                        finish()
                    }
                }
            )


        backBtn.setOnClickListener {

            finish()
        }


        loginBtn.setOnClickListener {

            startActivity(

                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }


        registerBtn.setOnClickListener {

            val name =
                nameEt.text
                    .toString()
                    .trim()

            val email =
                emailEt.text
                    .toString()
                    .trim()

            val phone =
                phoneEt.text
                    .toString()
                    .trim()

            val license =
                licenseEt.text
                    .toString()
                    .trim()

            val busNumber =
                busEt.text
                    .toString()
                    .trim()

            val password =
                passwordEt.text
                    .toString()
                    .trim()

            val confirmPassword =
                confirmPasswordEt.text
                    .toString()
                    .trim()


            if (name.isEmpty()) {

                nameEt.error =
                    "Enter full name"

                return@setOnClickListener
            }


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
                phone.length < 10
            ) {

                phoneEt.error =
                    "Enter valid phone"

                return@setOnClickListener
            }


            if (
                license.isEmpty()
            ) {

                licenseEt.error =
                    "Enter license number"

                return@setOnClickListener
            }


            if (
                busNumber.isEmpty()
            ) {

                busEt.error =
                    "Enter bus number"

                return@setOnClickListener
            }


            if (
                password.length < 6
            ) {

                passwordEt.error =
                    "Minimum 6 characters"

                return@setOnClickListener
            }


            if (
                password !=
                confirmPassword
            ) {

                confirmPasswordEt.error =
                    "Passwords do not match"

                return@setOnClickListener
            }


            if (
                !termsCheck.isChecked
            ) {

                Toast.makeText(

                    this,

                    "Accept terms first",

                    Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }


            auth.createUserWithEmailAndPassword(

                email,
                password

            ).addOnSuccessListener {

                val userId =
                    auth.currentUser!!.uid


                val driverData =
                    hashMapOf(

                        "uid" to userId,

                        "name" to name,

                        "email" to email,

                        "phone" to phone,

                        "licenseNumber" to license,

                        "busNumber" to busNumber,

                        "role" to "driver"
                    )


                firestore.collection(
                    "users"
                )

                    .document(
                        userId
                    )

                    .set(
                        driverData
                    )

                    .addOnSuccessListener {

                        Toast.makeText(

                            this,

                            "Driver Registered Successfully",

                            Toast.LENGTH_SHORT

                        ).show()


                        startActivity(

                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )

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
    }
}
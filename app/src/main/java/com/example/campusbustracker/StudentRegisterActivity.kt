package com.example.campusbustracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_student_register
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

        val passwordEt =
            findViewById<EditText>(
                R.id.passwordEt
            )

        val confirmPasswordEt =
            findViewById<EditText>(
                R.id.confirmPasswordEt
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
                nameEt.text.toString().trim()

            val email =
                emailEt.text.toString().trim()

            val phone =
                phoneEt.text.toString().trim()

            val password =
                passwordEt.text.toString().trim()

            val confirmPassword =
                confirmPasswordEt.text.toString().trim()


            // NAME VALIDATION
            if (name.isEmpty()) {

                nameEt.error =
                    "Enter full name"

                nameEt.requestFocus()

                return@setOnClickListener
            }


            // EMAIL VALIDATION
            if (email.isEmpty()) {

                emailEt.error =
                    "Enter email"

                emailEt.requestFocus()

                return@setOnClickListener
            }

            if (
                !Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {

                emailEt.error =
                    "Invalid email"

                emailEt.requestFocus()

                return@setOnClickListener
            }


            // PHONE VALIDATION
            if (phone.isEmpty()) {

                phoneEt.error =
                    "Enter phone number"

                phoneEt.requestFocus()

                return@setOnClickListener
            }

            if (
                phone.length != 10
            ) {

                phoneEt.error =
                    "Enter valid phone number"

                phoneEt.requestFocus()

                return@setOnClickListener
            }


            // PASSWORD VALIDATION
            if (password.isEmpty()) {

                passwordEt.error =
                    "Enter password"

                passwordEt.requestFocus()

                return@setOnClickListener
            }

            if (
                password.length < 6
            ) {

                passwordEt.error =
                    "Minimum 6 characters"

                passwordEt.requestFocus()

                return@setOnClickListener
            }


            // CONFIRM PASSWORD
            if (
                password !=
                confirmPassword
            ) {

                confirmPasswordEt.error =
                    "Passwords do not match"

                confirmPasswordEt.requestFocus()

                return@setOnClickListener
            }


            // TERMS VALIDATION
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


            registerStudent(
                name,
                email,
                phone,
                password
            )
        }
    }


    private fun registerStudent(

        name: String,

        email: String,

        phone: String,

        password: String

    ) {

        auth.createUserWithEmailAndPassword(
            email,
            password
        )

            .addOnSuccessListener {

                val userId =
                    auth.currentUser!!.uid


                val studentData =
                    hashMapOf(

                        "name" to name,

                        "email" to email,

                        "phone" to phone,

                        "role" to "student"
                    )


                firestore.collection(
                    "users"
                )

                    .document(
                        userId
                    )

                    .set(
                        studentData
                    )

                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Student Registered Successfully",
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

                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            "Database Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            .addOnFailureListener { e ->

                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
package com.example.campusbustracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity

import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_profile
        )

        auth =
            FirebaseAuth.getInstance()

        firestore =
            FirebaseFirestore.getInstance()


        val profileImage =
            findViewById<ImageView>(
                R.id.profileImage
            )

        val nameTxt =
            findViewById<TextView>(
                R.id.nameTxt
            )

        val idTxt =
            findViewById<TextView>(
                R.id.idTxt
            )

        val phoneTxt =
            findViewById<TextView>(
                R.id.phoneTxt
            )

        val emailTxt =
            findViewById<TextView>(
                R.id.emailTxt
            )

        val roleTxt =
            findViewById<TextView>(
                R.id.roleTxt
            )

        val dobTxt =
            findViewById<TextView>(
                R.id.dobTxt
            )

        val addressTxt =
            findViewById<TextView>(
                R.id.addressTxt
            )

        val vehicleNumberTxt =
            findViewById<TextView>(
                R.id.vehicleNumberTxt
            )

        val vehicleModelTxt =
            findViewById<TextView>(
                R.id.vehicleModelTxt
            )

        val vehicleColorTxt =
            findViewById<TextView>(
                R.id.vehicleColorTxt
            )

        val manufactureYearTxt =
            findViewById<TextView>(
                R.id.manufactureYearTxt
            )

        val insuranceTxt =
            findViewById<TextView>(
                R.id.insuranceTxt
            )

        val rcTxt =
            findViewById<TextView>(
                R.id.rcTxt
            )

        val totalSeatsTxt =
            findViewById<TextView>(
                R.id.totalSeatsTxt
            )

        val vehicleLayout =
            findViewById<LinearLayout>(
                R.id.vehicleLayout
            )

        val editBtn =
            findViewById<ImageView>(
                R.id.editBtn
            )

        val logoutBtn =
            findViewById<Button>(
                R.id.logoutBtn
            )


        loadProfileData(
            profileImage,
            nameTxt,
            idTxt,
            phoneTxt,
            emailTxt,
            roleTxt,
            dobTxt,
            addressTxt,
            vehicleNumberTxt,
            vehicleModelTxt,
            vehicleColorTxt,
            manufactureYearTxt,
            insuranceTxt,
            rcTxt,
            totalSeatsTxt,

            vehicleLayout
        )


        editBtn.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    EditProfileActivity::class.java
                )
            )
        }


        logoutBtn.setOnClickListener {

            auth.signOut()

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finishAffinity()
        }


        BottomNavigationManager.setup(
            this,
            findViewById(R.id.footer)
        )
    }



    private fun loadProfileData(

        profileImage: ImageView,

        nameTxt: TextView,

        idTxt: TextView,

        phoneTxt: TextView,

        emailTxt: TextView,

        roleTxt: TextView,

        dobTxt: TextView,

        addressTxt: TextView,

        vehicleNumberTxt: TextView,
        vehicleModelTxt: TextView,
        vehicleColorTxt: TextView,
        manufactureYearTxt: TextView,
        insuranceTxt: TextView,
        rcTxt: TextView,
        totalSeatsTxt: TextView,
        vehicleLayout: LinearLayout

    ) {

        val uid =
            auth.currentUser?.uid
                ?: return


        firestore.collection("users")
            .document(uid)
            .get()

            .addOnSuccessListener { userDoc ->

                val name =
                    userDoc.getString("name") ?: ""

                val email =
                    userDoc.getString("email") ?: ""

                val role =
                    userDoc.getString("role") ?: ""

                val imageUrl =
                    userDoc.getString("profileImage") ?: ""


                nameTxt.text = name
                emailTxt.text = "Email: $email"
                roleTxt.text = "Role: $role"


                if (imageUrl.isNotEmpty()) {

                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(profileImage)
                }


                firestore.collection("profiles")
                    .document(uid)
                    .get()

                    .addOnSuccessListener { profileDoc ->

                        val userId =
                            profileDoc.getString("userId") ?: ""

                        val phone =
                            profileDoc.getString("phone") ?: ""

                        val dob =
                            profileDoc.getString("dob") ?: ""

                        val address =
                            profileDoc.getString("address") ?: ""


                        idTxt.text =
                            if (role == "driver")
                                "Driver ID: $userId"
                            else
                                "Student ID: $userId"


                        phoneTxt.text =
                            "Phone: $phone"

                        dobTxt.text =
                            "DOB: $dob"

                        addressTxt.text =
                            "Address: $address"


                        if (role == "driver") {

                            vehicleLayout.visibility =
                                View.VISIBLE


                            val vehicleNumber =
                                profileDoc.getString(
                                    "vehicleNumber"
                                ) ?: ""

                            val vehicleModel =
                                profileDoc.getString(
                                    "vehicleModel"
                                ) ?: ""

                            val vehicleColor =
                                profileDoc.getString(
                                    "vehicleColor"
                                ) ?: ""

                            val manufactureYear =
                                profileDoc.getString(
                                    "manufactureYear"
                                ) ?: ""

                            val insuranceDate =
                                profileDoc.getString(
                                    "insuranceDate"
                                ) ?: ""

                            val rcDate =
                                profileDoc.getString(
                                    "rcDate"
                                ) ?: ""

                            val totalSeats =
                                profileDoc.getString("totalSeats") ?: ""


                            vehicleNumberTxt.text =
                                "Vehicle Number: $vehicleNumber"

                            vehicleModelTxt.text =
                                "Model: $vehicleModel"

                            vehicleColorTxt.text =
                                "Color: $vehicleColor"

                            manufactureYearTxt.text =
                                "Year: $manufactureYear"

                            insuranceTxt.text =
                                "Insurance: $insuranceDate"

                            rcTxt.text =
                                "RC Valid Till: $rcDate"

                            totalSeatsTxt.text =
                                "Total Seats: $totalSeats"

                        } else {

                            vehicleLayout.visibility =
                                View.GONE
                        }
                    }
            }
    }
}
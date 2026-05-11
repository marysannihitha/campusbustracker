package com.example.campusbustracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import com.bumptech.glide.Glide

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private lateinit var profileImage: ImageView
    private lateinit var cameraBtn: ImageView

    private lateinit var nameEt: EditText
    private lateinit var idEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var phoneEt: EditText
    private lateinit var roleEt: EditText
    private lateinit var dobEt: EditText
    private lateinit var addressEt: EditText

    private lateinit var vehicleNumberEt: EditText
    private lateinit var vehicleModelEt: EditText
    private lateinit var vehicleColorEt: EditText
    private lateinit var manufactureYearEt: EditText
    private lateinit var insuranceDateEt: EditText
    private lateinit var rcDateEt: EditText

    private lateinit var vehicleLayout: LinearLayout

    private lateinit var saveBtn: TextView
    private lateinit var backBtn: ImageView

    private lateinit var totalSeatsEt:
            EditText

    private lateinit var logoutBtn: Button

    private var userRole = ""

    private var imageUri: Uri? = null


    private val imagePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (
                result.resultCode == Activity.RESULT_OK
            ) {

                imageUri =
                    result.data?.data

                profileImage.setImageURI(
                    imageUri
                )

                uploadProfileImage()
            }
        }


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_edit_profile
        )

        val profileImage =
            findViewById<ImageView>(
                R.id.profileImage
            )

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid


        if (uid != null) {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()

                .addOnSuccessListener { document ->

                    val imageUrl =
                        document.getString(
                            "profileImage"
                        )

                    if (!imageUrl.isNullOrEmpty()) {

                        Glide.with(this)
                            .load(imageUrl)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(
                                com.bumptech.glide.load.engine.DiskCacheStrategy.NONE
                            )
                            .circleCrop()
                            .into(profileImage)
                    }
                }
        }

        auth =
            FirebaseAuth.getInstance()



        storageRef =
            FirebaseStorage.getInstance()
                .reference

        firestore =
            FirebaseFirestore
                .getInstance()

        initViews()

        setupDatePickers()

        loadUserData()


        backBtn.setOnClickListener {
            finish()
        }


        saveBtn.setOnClickListener {

            Toast.makeText(
                this,
                "Saving...",
                Toast.LENGTH_SHORT
            ).show()

            saveUserData()
        }


        cameraBtn.setOnClickListener {

            val intent =
                Intent(
                    Intent.ACTION_PICK
                )

            intent.type =
                "image/*"

            imagePickerLauncher.launch(
                intent
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

        setupFooter()
    }



    private fun initViews() {

        profileImage =
            findViewById(
                R.id.profileImage
            )

        cameraBtn =
            findViewById(
                R.id.cameraBtn
            )

        nameEt =
            findViewById(
                R.id.nameEt
            )

        idEt =
            findViewById(
                R.id.idEt
            )

        emailEt =
            findViewById(
                R.id.emailEt
            )

        phoneEt =
            findViewById(
                R.id.phoneEt
            )

        roleEt =
            findViewById(
                R.id.roleEt
            )

        dobEt =
            findViewById(
                R.id.dobEt
            )

        addressEt =
            findViewById(
                R.id.addressEt
            )

        vehicleNumberEt =
            findViewById(
                R.id.vehicleNumberEt
            )

        vehicleModelEt =
            findViewById(
                R.id.vehicleModelEt
            )

        vehicleColorEt =
            findViewById(
                R.id.vehicleColorEt
            )

        manufactureYearEt =
            findViewById(
                R.id.manufactureYearEt
            )

        insuranceDateEt =
            findViewById(
                R.id.insuranceDateEt
            )

        rcDateEt =
            findViewById(
                R.id.rcDateEt
            )

        totalSeatsEt =
            findViewById(
                R.id.totalSeatsEt
            )

        vehicleLayout =
            findViewById(
                R.id.vehicleLayout
            )

        saveBtn =
            findViewById(
                R.id.saveBtn
            )

        backBtn =
            findViewById(
                R.id.backBtn
            )

        logoutBtn =
            findViewById(
                R.id.logoutBtn
            )
    }

    private fun setupDatePickers() {

        insuranceDateEt.setOnClickListener {
            showDatePicker(insuranceDateEt)
        }

        rcDateEt.setOnClickListener {
            showDatePicker(rcDateEt)
        }

        dobEt.setOnClickListener {
            showDatePicker(dobEt)
        }
    }


    private fun showDatePicker(editText: EditText) {

        val calendar = java.util.Calendar.getInstance()

        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val picker = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                val date =
                    "$selectedDay/${selectedMonth + 1}/$selectedYear"

                editText.setText(date)

            },
            year,
            month,
            day
        )

        picker.show()
    }

    private fun loadUserData() {

        val uid =
            auth.currentUser?.uid ?: return


        // USERS COLLECTION
        firestore.collection("users")
            .document(uid)
            .get()

            .addOnSuccessListener { userDoc ->

                userRole =
                    userDoc.getString("role")
                        ?: ""

                nameEt.setText(
                    userDoc.getString("name")
                )

                emailEt.setText(
                    userDoc.getString("email")
                )

                roleEt.setText(
                    userRole
                )


                val imageUrl =
                    userDoc.getString(
                        "profileImage"
                    ) ?: ""

                if (imageUrl.isNotEmpty()) {

                    Glide.with(this@EditProfileActivity)
                        .load(imageUrl)
                        .skipMemoryCache(true)
                        .circleCrop()
                        .into(profileImage)
                }


                // PROFILE COLLECTION
                firestore.collection("profiles")
                    .document(uid)
                    .get()

                    .addOnSuccessListener { profileDoc ->

                        idEt.setText(
                            profileDoc.getString(
                                "userId"
                            )
                        )

                        phoneEt.setText(
                            profileDoc.getString(
                                "phone"
                            )
                        )

                        dobEt.setText(
                            profileDoc.getString(
                                "dob"
                            )
                        )

                        addressEt.setText(
                            profileDoc.getString(
                                "address"
                            )
                        )


                        if (userRole == "driver") {

                            vehicleLayout.visibility =
                                View.VISIBLE

                            vehicleNumberEt.setText(
                                profileDoc.getString(
                                    "vehicleNumber"
                                )
                            )

                            vehicleModelEt.setText(
                                profileDoc.getString(
                                    "vehicleModel"
                                )
                            )

                            vehicleColorEt.setText(
                                profileDoc.getString(
                                    "vehicleColor"
                                )
                            )

                            manufactureYearEt.setText(
                                profileDoc.getString(
                                    "manufactureYear"
                                )
                            )

                            insuranceDateEt.setText(
                                profileDoc.getString(
                                    "insuranceDate"
                                )
                            )

                            rcDateEt.setText(
                                profileDoc.getString(
                                    "rcDate"
                                )
                            )

                            totalSeatsEt.setText(
                                profileDoc.getString(
                                    "totalSeats"
                                )
                            )

                        } else {

                            vehicleLayout.visibility =
                                View.GONE
                        }
                    }
            }
    }



    private fun uploadProfileImage() {

        val uid =
            auth.currentUser?.uid ?: return

        if (imageUri == null) return


        val fileName =
            System.currentTimeMillis().toString() + ".jpg"


        val imageRef =
            storageRef.child(
                "profile_images/$uid/$fileName"
            )


        imageRef.putFile(imageUri!!)
            .addOnSuccessListener {

                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->

                        firestore.collection("users")
                            .document(uid)
                            .update(
                                "profileImage",
                                uri.toString()
                            )
                            .addOnSuccessListener {

                                // show latest instantly
                                Glide.with(this)
                                    .load(uri.toString())
                                    .circleCrop()
                                    .into(profileImage)

                                Toast.makeText(
                                    this,
                                    "Image Updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }
    }


    private fun saveUserData() {

        val uid =
            auth.currentUser?.uid ?: return


        // USERS COLLECTION
        val userData =
            hashMapOf<String, Any?>()

        userData["name"] =
            nameEt.text.toString()

        userData["email"] =
            emailEt.text.toString()

        userData["role"] =
            userRole


        // PROFILE COLLECTION
        val profileData =
            hashMapOf<String, Any?>()

        profileData["userId"] =
            idEt.text.toString()

        profileData["phone"] =
            phoneEt.text.toString()

        profileData["dob"] =
            dobEt.text.toString()

        profileData["address"] =
            addressEt.text.toString()


        if (userRole == "driver") {

            profileData["vehicleNumber"] =
                vehicleNumberEt.text.toString()

            profileData["vehicleModel"] =
                vehicleModelEt.text.toString()

            profileData["vehicleColor"] =
                vehicleColorEt.text.toString()

            profileData["manufactureYear"] =
                manufactureYearEt.text.toString()

            profileData["insuranceDate"] =
                insuranceDateEt.text.toString()

            profileData["rcDate"] =
                rcDateEt.text.toString()

            profileData["totalSeats"] =
                totalSeatsEt.text.toString()

        } else {

            profileData["vehicleNumber"] = null
            profileData["vehicleModel"] = null
            profileData["vehicleColor"] = null
            profileData["manufactureYear"] = null
            profileData["insuranceDate"] = null
            profileData["rcDate"] = null
        }


        firestore.collection("users")
            .document(uid)
            .set(userData)

            .addOnSuccessListener {

                firestore.collection("profiles")
                    .document(uid)
                    .set(profileData)

                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Saved Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this,
                                ProfileActivity::class.java
                            )
                        )

                        finish()
                    }
            }
    }



    private fun setupFooter() {

        val footerLayout =
            findViewById<LinearLayout>(
                R.id.footerLayout
            )

        if (footerLayout != null) {

            BottomNavigationManager.setup(
                this,
                footerLayout
            )
        }
    }
}
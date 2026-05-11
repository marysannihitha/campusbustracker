package com.example.campusbustracker

import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TripStatusActivity :
    AppCompatActivity() {

    private lateinit var firestore:
            FirebaseFirestore

    private var routeId = ""

    private var driverId = ""

    private var currentStop = 0


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_trip_status
        )


        firestore =
            FirebaseFirestore.getInstance()


        routeId =

            intent.getStringExtra(
                "routeId"
            ) ?: ""


        driverId =

            intent.getStringExtra(
                "driverId"
            ) ?: ""


        // BACK
        findViewById<ImageView>(
            R.id.backBtn
        ).setOnClickListener {

            finish()
        }


        // GET CURRENT STOP
        getCurrentStop()


        // SAVE STATUS
        findViewById<MaterialButton>(
            R.id.updateBtn
        ).setOnClickListener {

            saveTripStatus()
        }
    }


    private fun getCurrentStop() {

        if (
            driverId.isEmpty()
        ) return


        firestore.collection(
            "activeTrips"
        )

            .document(
                driverId
            )

            .get()

            .addOnSuccessListener { document ->

                if (
                    document.exists()
                ) {

                    currentStop =

                        document.getLong(
                            "currentStop"
                        )?.toInt() ?: 0
                }
            }
    }


    private fun saveTripStatus() {

        val statusGroup =

            findViewById<RadioGroup>(
                R.id.statusGroup
            )


        val selectedId =

            statusGroup.checkedRadioButtonId


        if (
            selectedId == -1
        ) {

            Toast.makeText(

                this,

                "Select trip status",

                Toast.LENGTH_SHORT

            ).show()

            return
        }


        val selectedStatus =

            findViewById<android.widget.RadioButton>(
                selectedId
            ).text.toString()


        val note =

            findViewById<android.widget.EditText>(
                R.id.noteEdt
            ).text.toString()



        val data =
            hashMapOf<String, Any>()

        if (
            routeId.isNotEmpty()
        ) {

            data["routeId"] =
                routeId
        }

        data["driverId"] =
            driverId

        data["stopNumber"] =
            currentStop

        data["status"] =
            selectedStatus

        data["note"] =
            note

        data["timestamp"] =
            System.currentTimeMillis()



        firestore.collection(
            "tripStatus"
        )

            .whereEqualTo(
                "driverId",
                driverId
            )

            .whereEqualTo(
                "stopNumber",
                currentStop
            )

            .get()

            .addOnSuccessListener { result ->


                // UPDATE EXISTING
                if (
                    result.documents.isNotEmpty()
                ) {

                    result.documents[0]

                        .reference

                        .update(
                            data
                        )

                        .addOnSuccessListener {

                            Toast.makeText(

                                this,

                                "Status updated",

                                Toast.LENGTH_SHORT

                            ).show()

                            setResult(
                                RESULT_OK
                            )

                            finish()
                        }
                }


                // CREATE NEW
                else {

                    firestore.collection(
                        "tripStatus"
                    )

                        .add(
                            data
                        )

                        .addOnSuccessListener {

                            Toast.makeText(

                                this,

                                "Status updated",

                                Toast.LENGTH_SHORT

                            ).show()

                            setResult(
                                RESULT_OK
                            )

                            finish()
                        }
                }

            }

            .addOnFailureListener {

                Toast.makeText(

                    this,

                    "Failed to update",

                    Toast.LENGTH_SHORT

                ).show()
            }
    }
}
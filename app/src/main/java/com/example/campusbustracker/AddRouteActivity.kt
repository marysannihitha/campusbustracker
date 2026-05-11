package com.example.campusbustracker

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.api.model.Place

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.util.Calendar
import java.util.Locale


class AddRouteActivity :
    AppCompatActivity() {


    private lateinit var firestore:
            FirebaseFirestore

    private lateinit var auth:
            FirebaseAuth


    private lateinit var stopContainer:
            LinearLayout

    private lateinit var busNumberEt:
            EditText

    private lateinit var routeNameEt:
            EditText

    private lateinit var startEt:
            EditText

    private lateinit var endEt:
            EditText

    private lateinit var departureEt:
            EditText

    private lateinit var arrivalEt:
            EditText

    private lateinit var routeIdTxt:
            TextView


    private var stopCount = 1

    private var selectedEditText:
            EditText? = null


    private var generatedRouteId =
        ""


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_add_route
        )


        firestore =
            FirebaseFirestore.getInstance()

        auth =
            FirebaseAuth.getInstance()


        if (
            !Places.isInitialized()
        ) {

            Places.initialize(

                applicationContext,

                getString(
                    R.string.google_maps_key
                )
            )
        }


        stopContainer =
            findViewById(
                R.id.stopContainer
            )

        routeIdTxt =
            findViewById<TextView>(
                R.id.routeIdTxt
            )

        routeNameEt =
            findViewById(
                R.id.routeNameEt
            )

        busNumberEt =
            findViewById(
                R.id.busNumberEt
            )

        startEt =
            findViewById<EditText>(
                R.id.startEt
            )

        endEt =
            findViewById<EditText>(
                R.id.endEt
            )

        departureEt =
            findViewById<EditText>(
                R.id.departureEt
            )

        arrivalEt =
            findViewById<EditText>(
                R.id.arrivalEt
            )


        generateRouteId()

        loadBusNumber()


        setupPlacePicker(
            startEt
        )

        setupPlacePicker(
            endEt
        )


        setupTimePicker(
            departureEt
        )

        setupTimePicker(
            arrivalEt
        )


        findViewById<TextView>(
            R.id.addStopBtn
        ).setOnClickListener {

            addNewStop()
        }


        findViewById<TextView>(
            R.id.addRouteBtn
        ).setOnClickListener {

            saveRoute()
        }


        addNewStop()
    }



    private fun generateRouteId() {

        firestore.collection(
            "routes"
        )

            .get()

            .addOnSuccessListener {

                val count =
                    it.size() + 1


                generatedRouteId =

                    "RTE-" +

                            String.format(
                                "%03d",
                                count
                            )


                routeIdTxt.text =
                    generatedRouteId
            }
    }



    private fun loadBusNumber() {

        val uid =
            auth.currentUser?.uid
                ?: return


        firestore.collection(
            "profiles"
        )
            .document(uid)
            .get()

            .addOnSuccessListener { document ->


                val busNumber =

                    document.getString(
                        "vehicleNumber"
                    ) ?: ""


                busNumberEt.setText(
                    busNumber
                )


                busNumberEt.isEnabled =
                    false
            }
    }



    private fun setupPlacePicker(
        editText: EditText
    ) {

        editText.isFocusable =
            false


        editText.setOnClickListener {

            selectedEditText =
                editText


            val fields =
                listOf(

                    Place.Field.NAME,

                    Place.Field.ADDRESS
                )


            val intent =
                Autocomplete.IntentBuilder(

                    AutocompleteActivityMode.OVERLAY,

                    fields

                ).build(
                    this
                )


            startActivityForResult(
                intent,
                1001
            )
        }
    }



    private fun setupTimePicker(
        editText: EditText
    ) {

        editText.inputType = 0

        editText.keyListener = null

        editText.isFocusable = false

        editText.isCursorVisible = false


        editText.setOnClickListener {

            showTimePicker(
                editText
            )
        }
    }



    private fun addNewStop() {

        val stopView =
            layoutInflater.inflate(

                R.layout.item_stop,

                stopContainer,

                false
            )


        val numberTxt =
            stopView.findViewById<TextView>(
                R.id.stopNumber
            )


        val stopEt =
            stopView.findViewById<EditText>(
                R.id.stopEt
            )


        val timeTxt =
            stopView.findViewById<TextView>(
                R.id.stopTimeTxt
            )


        numberTxt.text =
            stopCount.toString()


        setupPlacePicker(
            stopEt
        )


        timeTxt.setOnClickListener {

            showTimePicker(
                timeTxt
            )
        }


        // SPACE BETWEEN STOPS
        val params =
            LinearLayout.LayoutParams(

                LinearLayout.LayoutParams.MATCH_PARENT,

                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.topMargin =
            0


        stopView.layoutParams =
            params


        stopContainer.addView(
            stopView
        )


        stopCount++
    }



    private fun showTimePicker(
        textView: TextView
    ) {

        val calendar =
            Calendar.getInstance()


        TimePickerDialog(

            this,

            { _, hour, minute ->

                val time =
                    String.format(

                        Locale.getDefault(),

                        "%02d:%02d",

                        hour,

                        minute
                    )

                textView.text =
                    time
            },

            calendar.get(
                Calendar.HOUR_OF_DAY
            ),

            calendar.get(
                Calendar.MINUTE
            ),

            false

        ).show()
    }



    override fun onActivityResult(

        requestCode: Int,

        resultCode: Int,

        data: Intent?

    ) {

        super.onActivityResult(

            requestCode,

            resultCode,

            data
        )


        if (
            requestCode == 1001
        ) {

            if (
                resultCode == RESULT_OK &&
                data != null
            ) {

                val place =

                    Autocomplete
                        .getPlaceFromIntent(
                            data
                        )


                selectedEditText?.setText(

                    place.address
                )
            }
        }
    }



    private fun saveRoute() {

        val driverId =
            auth.currentUser?.uid
                ?: return


        // CHECK IF DRIVER ALREADY HAS A ROUTE
        firestore.collection(
            "routes"
        )

            .whereEqualTo(
                "driverId",
                driverId
            )

            .get()

            .addOnSuccessListener { existingRoutes ->


                if (
                    existingRoutes.documents.isNotEmpty()
                ) {

                    Toast.makeText(

                        this,

                        "You can add only one route",

                        Toast.LENGTH_SHORT

                    ).show()

                    return@addOnSuccessListener
                }



                // YOUR EXISTING CODE
                val stops =
                    mutableListOf<HashMap<String, String>>()


                for (
                i in 0 until stopContainer.childCount
                ) {

                    val stopView =
                        stopContainer.getChildAt(
                            i
                        )


                    val stopName =

                        stopView.findViewById<EditText>(
                            R.id.stopEt
                        ).text.toString()


                    val stopTime =

                        stopView.findViewById<TextView>(
                            R.id.stopTimeTxt
                        ).text.toString()


                    stops.add(

                        hashMapOf(

                            "name" to
                                    stopName,

                            "time" to
                                    stopTime
                        )
                    )
                }



                val routeData =
                    hashMapOf(

                        "routeId" to
                                generatedRouteId,

                        "driverId" to
                                driverId,

                        "routeName" to
                                routeNameEt.text.toString(),

                        "startLocation" to
                                startEt.text.toString(),

                        "endLocation" to
                                endEt.text.toString(),

                        "departureTime" to
                                departureEt.text.toString(),

                        "arrivalTime" to
                                arrivalEt.text.toString(),

                        "busNumber" to
                                busNumberEt.text.toString(),

                        "stops" to
                                stops,

                        "createdAt" to
                                System.currentTimeMillis()
                    )



                firestore.collection(
                    "routes"
                )

                    .add(
                        routeData
                    )

                    .addOnSuccessListener {

                        Toast.makeText(

                            this,

                            "Route Added",

                            Toast.LENGTH_SHORT

                        ).show()

                        finish()
                    }
            }
    }
}
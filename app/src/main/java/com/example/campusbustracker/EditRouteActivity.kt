package com.example.campusbustracker

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

import com.google.firebase.firestore.FirebaseFirestore

import java.util.*

class EditRouteActivity :
    AppCompatActivity() {

    private lateinit var firestore:
            FirebaseFirestore

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

    private lateinit var busNumberEt:
            EditText

    private lateinit var stopContainer:
            LinearLayout

    private var firestoreDocumentId =
        ""

    private var selectedEditText:
            EditText? = null


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_edit_route
        )


        firestore =
            FirebaseFirestore.getInstance()


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


        routeNameEt =
            findViewById(
                R.id.routeNameEt
            )

        startEt =
            findViewById(
                R.id.startEt
            )

        endEt =
            findViewById(
                R.id.endEt
            )

        departureEt =
            findViewById(
                R.id.departureEt
            )

        arrivalEt =
            findViewById(
                R.id.arrivalEt
            )

        busNumberEt =
            findViewById(
                R.id.busNumberEt
            )

        stopContainer =
            findViewById(
                R.id.stopContainer
            )


        // LOCATION PICKERS
        setupPlacePicker(
            startEt
        )

        setupPlacePicker(
            endEt
        )


        // TIME PICKERS
        setupTimePicker(
            departureEt
        )

        setupTimePicker(
            arrivalEt
        )


        findViewById<TextView>(
            R.id.addStopBtn
        ).setOnClickListener {

            addStop(
                "",
                ""
            )
        }


        findViewById<Button>(
            R.id.addRouteBtn
        ).setOnClickListener {

            updateRoute()
        }


        findViewById<ImageView>(
            R.id.backBtn
        ).setOnClickListener {

            finish()
        }


        val routeId =

            intent.getStringExtra(
                "routeId"
            ) ?: ""


        loadRoute(
            routeId
        )
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
                        ?: place.name
                )
            }
        }
    }



    private fun setupPlacePicker(
        editText: EditText
    ) {

        editText.inputType = 0

        editText.keyListener = null

        editText.isFocusable = false

        editText.isCursorVisible = false


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



    private fun loadRoute(
        routeId: String
    ) {

        firestore.collection(
            "routes"
        )

            .whereEqualTo(
                "routeId",
                routeId
            )

            .get()

            .addOnSuccessListener { result ->

                if (
                    result.documents.isEmpty()
                ) return@addOnSuccessListener


                val document =
                    result.documents[0]


                firestoreDocumentId =
                    document.id


                routeNameEt.setText(

                    document.getString(
                        "routeName"
                    )
                )


                startEt.setText(

                    document.getString(
                        "startLocation"
                    )
                )


                endEt.setText(

                    document.getString(
                        "endLocation"
                    )
                )


                departureEt.setText(

                    document.getString(
                        "departureTime"
                    )
                )


                arrivalEt.setText(

                    document.getString(
                        "arrivalTime"
                    )
                )


                busNumberEt.setText(

                    document.getString(
                        "busNumber"
                    )
                )


                stopContainer.removeAllViews()


                val stops =

                    document.get(
                        "stops"
                    ) as? List<HashMap<String, Any>>

                        ?: emptyList()


                for (
                stop in stops
                ) {

                    addStop(

                        stop["name"].toString(),

                        stop["time"].toString()
                    )
                }
            }
    }



    private fun addStop(

        stopName: String,

        stopTime: String

    ) {

        val stopView =

            layoutInflater.inflate(

                R.layout.edit_item_stop,

                stopContainer,

                false
            )


        val stopEt =
            stopView.findViewById<EditText>(
                R.id.stopEt
            )


        val stopTimeTxt =
            stopView.findViewById<TextView>(
                R.id.stopTimeTxt
            )


        val stopNumber =
            stopView.findViewById<TextView>(
                R.id.stopNumber
            )


        val deleteBtn =
            stopView.findViewById<ImageView>(
                R.id.deleteStopBtn
            )


        stopNumber.text =

            (
                    stopContainer.childCount + 1
                    ).toString()


        stopEt.setText(
            stopName
        )


        stopTimeTxt.text =
            stopTime


        // PLACE PICKER FOR STOP
        setupPlacePicker(
            stopEt
        )


        // TIME PICKER
        stopTimeTxt.setOnClickListener {

            showTimePickerForText(
                stopTimeTxt
            )
        }


        deleteBtn.setOnClickListener {

            stopContainer.removeView(
                stopView
            )

            refreshStopNumbers()
        }


        stopContainer.addView(
            stopView
        )
    }



    private fun refreshStopNumbers() {

        for (
        i in 0 until stopContainer.childCount
        ) {

            val view =
                stopContainer.getChildAt(i)


            val numberTxt =

                view.findViewById<TextView>(
                    R.id.stopNumber
                )


            numberTxt.text =
                (i + 1).toString()
        }
    }



    private fun updateRoute() {

        val stopsList =
            mutableListOf<HashMap<String, String>>()


        for (
        i in 0 until stopContainer.childCount
        ) {

            val view =
                stopContainer.getChildAt(i)


            val stopName =

                view.findViewById<EditText>(
                    R.id.stopEt
                )

                    .text
                    .toString()


            val stopTime =

                view.findViewById<TextView>(
                    R.id.stopTimeTxt
                )

                    .text
                    .toString()


            if (
                stopName.isNotEmpty()
            ) {

                val map =
                    hashMapOf<String, String>()


                map["name"] =
                    stopName


                map["time"] =
                    stopTime


                stopsList.add(
                    map
                )
            }
        }


        val updatedData =
            hashMapOf<String, Any>()


        updatedData["routeName"] =
            routeNameEt.text.toString()

        updatedData["startLocation"] =
            startEt.text.toString()

        updatedData["endLocation"] =
            endEt.text.toString()

        updatedData["departureTime"] =
            departureEt.text.toString()

        updatedData["arrivalTime"] =
            arrivalEt.text.toString()

        updatedData["busNumber"] =
            busNumberEt.text.toString()

        updatedData["stops"] =
            stopsList


        firestore.collection(
            "routes"
        )

            .document(
                firestoreDocumentId
            )

            .update(
                updatedData
            )

            .addOnSuccessListener {

                Toast.makeText(

                    this,

                    "Route Updated",

                    Toast.LENGTH_SHORT

                ).show()

                finish()
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

            val calendar =
                Calendar.getInstance()

            val hour =
                calendar.get(
                    Calendar.HOUR_OF_DAY
                )

            val minute =
                calendar.get(
                    Calendar.MINUTE
                )


            TimePickerDialog(

                this,

                { _, selectedHour, selectedMinute ->

                    val formattedTime =

                        String.format(

                            Locale.getDefault(),

                            "%02d:%02d",

                            selectedHour,

                            selectedMinute
                        )


                    editText.setText(
                        formattedTime
                    )
                },

                hour,

                minute,

                false

            ).show()
        }
    }



    private fun showTimePickerForText(
        textView: TextView
    ) {

        val calendar =
            Calendar.getInstance()

        val hour =
            calendar.get(
                Calendar.HOUR_OF_DAY
            )

        val minute =
            calendar.get(
                Calendar.MINUTE
            )


        TimePickerDialog(

            this,

            { _, selectedHour, selectedMinute ->

                val formattedTime =

                    String.format(

                        Locale.getDefault(),

                        "%02d:%02d",

                        selectedHour,

                        selectedMinute
                    )


                textView.text =
                    formattedTime
            },

            hour,

            minute,

            false

        ).show()
    }
}
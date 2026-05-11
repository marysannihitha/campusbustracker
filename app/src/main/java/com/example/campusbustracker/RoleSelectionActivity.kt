package com.example.campusbustracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class RoleSelectionActivity : AppCompatActivity() {

    private var selectedRole = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_role_selection
        )

        val backBtn =
            findViewById<ImageView>(R.id.backBtn)

        val studentCard =
            findViewById<LinearLayout>(R.id.studentCard)

        val driverCard =
            findViewById<LinearLayout>(R.id.driverCard)

        val continueBtn =
            findViewById<Button>(R.id.continueBtn)

        val loginBtn =
            findViewById<TextView>(R.id.loginBtn)


        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )

        studentCard.setOnClickListener {
            selectedRole = "student"
        }

        driverCard.setOnClickListener {
            selectedRole = "driver"
        }

        continueBtn.setOnClickListener {

            if (selectedRole == "student") {

                startActivity(
                    Intent(
                        this,
                        StudentRegisterActivity::class.java
                    )
                )

            } else {

                startActivity(
                    Intent(
                        this,
                        DriverRegisterActivity::class.java
                    )
                )
            }
        }


        backBtn.setOnClickListener {
            finish()
        }


        // FIXED
        loginBtn.setOnClickListener {

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
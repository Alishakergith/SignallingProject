
package com.example.signallingproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val nameEditText =
            findViewById<EditText>(R.id.nameEditText)
        val addressEditText =
            findViewById<EditText>(R.id.addressEditText)
        val phoneEditText =
            findViewById<EditText>(R.id.phoneEditText)

        val saveButton =
            findViewById<Button>(R.id.saveButton)
        val logoutButton =
            findViewById<Button>(R.id.logoutButton)

        val user = auth.currentUser

        if (user == null) {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
            finish()
            return
        }

        val userId = user.uid

        // Connect to Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)

        // Load existing profile
        database.get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {
                    nameEditText.setText(
                        snapshot.child("name")
                            .getValue(String::class.java) ?: ""
                    )

                    addressEditText.setText(
                        snapshot.child("address")
                            .getValue(String::class.java) ?: ""
                    )

                    phoneEditText.setText(
                        snapshot.child("phone")
                            .getValue(String::class.java) ?: ""
                    )
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        // Save profile to Realtime Database
        saveButton.setOnClickListener {

            val name =
                nameEditText.text.toString().trim()
            val address =
                addressEditText.text.toString().trim()
            val phone =
                phoneEditText.text.toString().trim()

            if (name.isEmpty() ||
                address.isEmpty() ||
                phone.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val profile = hashMapOf(
                "name" to name,
                "address" to address,
                "phone" to phone,
                "email" to (user.email ?: "")
            )

            database.setValue(profile)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Profile saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // Firebase logout
        logoutButton.setOnClickListener {

            auth.signOut()

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}

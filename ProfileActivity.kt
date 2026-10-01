
package com.example.signallingproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val name = findViewById<EditText>(R.id.nameEditText)
        val phone = findViewById<EditText>(R.id.phoneEditText)
        val save = findViewById<Button>(R.id.saveProfileButton)

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "Please log in first",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        val profileRef = database
            .getReference("users")
            .child(user.uid)

        // Retrieve existing profile
        profileRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    name.setText(
                        snapshot.child("name")
                            .getValue(String::class.java) ?: ""
                    )
                    phone.setText(
                        snapshot.child("phone")
                            .getValue(String::class.java) ?: ""
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to load profile",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Add or edit profile
        save.setOnClickListener {
            val fullName = name.text.toString().trim()
            val phoneNumber = phone.text.toString().trim()

            if (fullName.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(
                    this,
                    "Fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val updates = mapOf<String, Any>(
                "name" to fullName,
                "phone" to phoneNumber
            )

            save.isEnabled = false

            profileRef.updateChildren(updates)
                .addOnSuccessListener {
                    save.isEnabled = true
                    Toast.makeText(
                        this,
                        "Profile saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { error ->
                    save.isEnabled = true
                    Toast.makeText(
                        this,
                        "Failed to save: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}

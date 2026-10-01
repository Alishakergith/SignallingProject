
package com.example.signallingproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val emailInput =
            findViewById<EditText>(R.id.emailEditText)
        val passwordInput =
            findViewById<EditText>(R.id.passwordEditText)
        val createButton =
            findViewById<Button>(R.id.createAccountButton)

        createButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            createButton.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        if (user == null) {
                            createButton.isEnabled = true
                            return@addOnCompleteListener
                        }

                        // Create user record immediately
                        val profile = mapOf(
                            "email" to email,
                            "name" to "",
                            "phone" to ""
                        )

                        database.getReference("users")
                            .child(user.uid)
                            .setValue(profile)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Account created successfully",
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
                            .addOnFailureListener { error ->
                                createButton.isEnabled = true
                                Toast.makeText(
                                    this,
                                    "Account created, but profile save failed: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        createButton.isEnabled = true
                        Toast.makeText(
                            this,
                            task.exception?.message ?: "Sign up failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}

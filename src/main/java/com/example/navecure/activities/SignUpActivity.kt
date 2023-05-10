package com.example.navecure.activities


import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.example.navecure.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase




class SignUpActivity : AppCompatActivity() {
    lateinit var btnSignup: Button
    lateinit var tvLoginHere: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etRegEmail = findViewById<EditText>(R.id.etRegEmail)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        btnSignup = findViewById(R.id.btnSignup)
        tvLoginHere = findViewById(R.id.tvLoginHere)

        auth = Firebase.auth

        btnSignup.setOnClickListener {
            if (TextUtils.isEmpty(etRegEmail!!.text.toString())) {
                Toast.makeText(getApplicationContext(),
                    "Please enter email!!",
                    Toast.LENGTH_LONG)
                    .show();
            }
            else if (TextUtils.isEmpty(etRegPassword!!.text.toString())) {
                Toast.makeText(getApplicationContext(),
                    "Please enter password!!",
                    Toast.LENGTH_LONG)
                    .show();

            }
            else
                createUser(etRegEmail!!.text.toString(), etRegPassword!!.text.toString())
        }
        tvLoginHere.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))

        }

    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, inform the user
                    Toast.makeText(
                        this@SignUpActivity,
                        "User registered successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this@SignUpActivity, HomePageActivity::class.java))

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        this@SignUpActivity,
                        "Registration Error: " + task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
    }


}
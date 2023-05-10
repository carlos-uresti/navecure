package com.example.navecure.activities
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.navecure.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var btnLogin: Button//variable for login button in login screen
    lateinit var tvSignupHere: TextView//link to signup

    lateinit var auth: FirebaseAuth//this will get the context from our firebase database

    //this function executes when the login activity is created on the screen
    //it serves mostly to bind the logic in this file to the items in its
    //corresponding xml file i.e. binding a button variable in this file to
    //the actual button that will be appear on the screen for user to interact with
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //binds login activity with its corresponding xml file in layout directory (activity_login)
        setContentView(R.layout.activity_login)

        title = ""

        //input from email EditText in login screen
        val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)

        //input from password EditText in login screen
        val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)

        //binds login button to the Button in xml file
        btnLogin = findViewById(R.id.btnLogin)

        //binds login button to the Button in xml file
        tvSignupHere = findViewById(R.id.tvSignUpHere)

        //calls authentication method from Firebase
        //it will used to validate user credentials
        //which are provided by user as inputs from screen
        auth = Firebase.auth

        //Creates a listener and lambda function to execute
        //when user clicks the login button in the login screen
        btnLogin.setOnClickListener {

            //call to loginUser() function below with email and password input given by user
            loginUser(etLoginEmail!!.text.toString(), etLoginPassword!!.text.toString())
        }

        //this is a listener for the link to sign up at the bottom of login screen, executes when clicked
        tvSignupHere.setOnClickListener {

            //starts the sign up activity defined in SignUpActivity class file
            startActivity(Intent(this, SignUpActivity::class.java))

        }
    }
    //executes after activity is successfully created
    public override fun onStart() {
        super.onStart()



        //extras!!.putString(key, value)
        // Check if user is signed in (non-null) when app starts, if so and update UI accordingly.
        val currentUser = auth.currentUser
        //if user is signed in already, this will start the home page activity
        if(currentUser != null){
            startActivity(Intent(this@LoginActivity, HomePageActivity::class.java))
        }
    }

    //business logic for user login
    private fun loginUser(email: String, password: String) {
        //check user credentials against Firebase context
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //user logged in successfully, toast and start home page activity
                Toast.makeText(this@LoginActivity,"User logged in successfully",Toast.LENGTH_LONG).show()
                startActivity(Intent(this@LoginActivity, HomePageActivity::class.java))
            } else {
                //login failed, let user know in toast
                Toast.makeText(this@LoginActivity,"Log in Error: " + task.exception!!.message,Toast.LENGTH_LONG).show()

            }
        }
    }


}
package com.example.navecure.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.navecure.R


//this class serves as the holder for the login activity only
//as it will be the first place the user is directed to when the app starts
//treated similar to main() file in C++, this is where the program is looking to start
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //starts login activity
        startActivity(Intent(this, LoginActivity::class.java))

    }

}
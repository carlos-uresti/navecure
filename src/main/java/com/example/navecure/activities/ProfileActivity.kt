package com.example.navecure.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.navecure.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.profile.*


class ProfileActivity: AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
//variables to be used for shared preferences
    public val SHARED_PREFS = "sharedPrefs";
    val vfname = "fname"
    val vlname = "lname"
    val vphone = "phone"
    val vemail = "email"
    val vaddress = "address"
    val vcity = "city"
    val vzip = "zipcode"
    val vstate = "state"
    val vmake = "make"
    val vmodel = "model"
    val vyear = "year"
    val vcolor = "color"
    val vplate= "license"
    val vename = "vename"
    val velname = "velname"
    val verelate = "relation"
    val vephone = "vephone"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        //attempt to load the saved data to the profile
        loadData()
        //submit button listener
        submit.setOnClickListener{
            //if any edittext data is empty
            if (fname.text.toString() ==  ""
            || lname.text.toString() ==  "" || editTextPhone.text.toString() ==  "" || address.text.toString() ==  ""
            || city.text.toString() ==  "" || zip.text.toString() ==  ""
            || state.text.toString() ==  "" || make.text.toString() ==  ""
            || model.text.toString() ==  "" || year.text.toString() ==  ""
            || color.text.toString() ==  "" || plate.text.toString() ==  ""
            || eFname.text.toString() ==  "" || eLname.text.toString() ==  ""
            || relation.text.toString() ==  "" || ePhone.text.toString() ==  "")
            {
                //ask the user to fill it out completely
                Toast.makeText(this, "Please fill everything out with your own information.", Toast.LENGTH_SHORT).show()
            }
            else
            {
                //if the phone numbers are not numbers
                if(editTextPhone.text.toString().toLongOrNull() == null || ePhone.text.toString().toLongOrNull() == null)
                {
                    Toast.makeText(this, "Phone Numbers need to only be numbers", Toast.LENGTH_SHORT).show()
                }
                else if(year.text.toString().toLongOrNull() == null || zip.text.toString().toLongOrNull() == null)
                {
                    //if the year or zip isnt a number
                    Toast.makeText(this, "Year and Zip need to only be numbers", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    //if no problems, save data
                    saveData()
                }
            }

        }
        //bottom navigation bar functionality
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> {
                    //simply end this activity to return back to the home activity
                    this.finish()

                }

                R.id.log_out -> {
                    val logout_intent = Intent(this, SignUpActivity::class.java)
                    //clear shared preferences
                    var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                    sharedPreferences.edit().clear().commit()
                    var auth = Firebase.auth
                    auth.signOut()
                    //end this activity and return back to login screen
                    startActivity(logout_intent)
                    this.finish()
                }

            }
            true
        }
    }
//used to save the user inputted data to sharedpreferences
    fun saveData() {
        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        //handles each edittext value
        editor.putString(vfname, fname.text.toString())
        editor.putString(vlname, lname.text.toString())
        editor.putString(vphone, editTextPhone.text.toString())
        editor.putString(vaddress, address.text.toString())
        editor.putString(vcity, city.text.toString())
        editor.putString(vzip, zip.text.toString())
        editor.putString(vstate, state.text.toString())
        editor.putString(vmake, make.text.toString())
        editor.putString(vmodel, model.text.toString())
        editor.putString(vyear, year.text.toString())
        editor.putString(vcolor, color.text.toString())
        editor.putString(vplate, plate.text.toString())
        editor.putString(vename, eFname.text.toString())
        editor.putString(velname, eLname.text.toString())
        editor.putString(verelate, relation.text.toString())
        editor.putString(vephone , ePhone.text.toString())
        //apply those changes to the sharedpreferences
        editor.apply()

        Toast.makeText(this, "Data Saved!", Toast.LENGTH_SHORT).show()
    }
    //function to load previously saved data
    fun loadData() {
        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        //set the edittext text value to be the saved sharedpref value
        //if there is no saved value, put an empty string
        fname.setText(sharedPreferences.getString(vfname, ""))
        lname.setText(sharedPreferences.getString(vlname, ""))
        editTextPhone.setText(sharedPreferences.getString(vphone, ""))
        address.setText(sharedPreferences.getString(vaddress, ""))
        city.setText(sharedPreferences.getString(vcity, ""))
        zip.setText(sharedPreferences.getString(vzip, ""))
        state.setText(sharedPreferences.getString(vstate, ""))
        make.setText(sharedPreferences.getString(vmake, ""))
        model.setText(sharedPreferences.getString(vmodel, ""))
        year.setText(sharedPreferences.getString(vyear, ""))
        color.setText(sharedPreferences.getString(vcolor, ""))
        plate.setText(sharedPreferences.getString(vplate, ""))
        eFname.setText(sharedPreferences.getString(vename, ""))
        eLname.setText(sharedPreferences.getString(velname, ""))
        relation.setText(sharedPreferences.getString(verelate, ""))
        ePhone.setText(sharedPreferences.getString(vephone, ""))
        //if there isnt saved previous data
        if (fname.text.toString() ==  ""
                || lname.text.toString() ==  "" || editTextPhone.text.toString() ==  ""
                || address.text.toString() ==  ""
                || city.text.toString() ==  "" || zip.text.toString() ==  ""
                || state.text.toString() ==  "" || make.text.toString() ==  ""
                || model.text.toString() ==  "" || year.text.toString() ==  ""
                || color.text.toString() ==  "" || plate.text.toString() ==  ""
                || eFname.text.toString() ==  "" || eLname.text.toString() ==  ""
                || relation.text.toString() ==  "" || ePhone.text.toString() ==  "")
        {
            //inform the user
            Toast.makeText(this, "No previous data found", Toast.LENGTH_SHORT).show()
        }
        else
        {
            //or confirm that the users previous data has been loaded
            Toast.makeText(this, "Previous Data Loaded!", Toast.LENGTH_SHORT).show()
        }
    }

}
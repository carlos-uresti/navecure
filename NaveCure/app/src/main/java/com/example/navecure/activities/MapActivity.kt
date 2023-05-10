package com.example.navecure.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.navecure.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.navecure.databinding.ActivityMapBinding
import com.google.android.gms.maps.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //binds logic in this file to its corresponding xml file, res/layout/activity_map.xml
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Fragment defined in res/layout/activity_maps.xml
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        //declare the type of map you want to use
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        //initiate a variable with the coordinates for UTA using LatLng() method from google.maps.model
        val universityOfTexasAtArlington = LatLng(32.7328, -97.1132)

        //place a marker in map using UTA coordinates declared above and provide a title
        googleMap.addMarker(
            MarkerOptions().position(universityOfTexasAtArlington).title("College of Engineering UTA")
        )

        //newLatLngZoom() method takes coordinates and zoom level as parameters
        //this will make it so that when map activity starts, the map will start zoomed in on this location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(universityOfTexasAtArlington, 17.5F))


    }

    //creates a hamburger menu on top right of home page activity
    //the visual menu items are defined in res/menu/main_menu.xml
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater : MenuInflater = menuInflater
        with(inflater) {
            inflate(R.menu.main_menu,menu)
        }
        return true
    }


    //logic for each menu item created should be defined here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        //check item id to gather which item was selected by user
        if(item.itemId == R.id.home)
        {
            //anything can be executed when user selects something from menu
            //in our case, we are simply jumping from activity to activity
            startActivity(Intent(this, HomePageActivity::class.java))

        }
        if (item.itemId  == R.id.log_out)
        {
            Firebase.auth.signOut()//log out user

            //launch login activity once user has logged out
            startActivity(Intent(this, LoginActivity::class.java))

        }
        return super.onOptionsItemSelected(item)
    }

}
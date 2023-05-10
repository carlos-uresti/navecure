package com.example.navecure.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.navecure.R
//import com.example.navecure.databinding.ActivityHomePageBinding
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.profile.*
import java.util.*
import kotlin.properties.Delegates

// HomePage Activity implements SensorEventListener, LocationListener, and RecognitionListener
//Their functions will be listed below for the class
class HomePageActivity : AppCompatActivity(),  SensorEventListener, LocationListener, RecognitionListener{

    //Variable declaration section
    private lateinit var msensorManager: SensorManager
    private var maccelerometer: Sensor? = null
    var mMediaPlayer: MediaPlayer? = null

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var blueToothDevices : ArrayList<String> ? = null;
    private var blueToothConnection:NavecureBlueTooth? = null;

    private var played = 0
    private var last = "0"
    private var begin = 0 ; private var end = 0 ; private var found = 0 ; private var notfound = 0
    private var mTTS:TextToSpeech? = null
    private var alert = 0
    private var alert2 = 0
    private var notfound2 = 0
    private var output: String = ""


    private var speech = 0; private var sensor = 0
    private var isAccelAvail: Boolean = false
    private var resume = true;
    private var emergency = false
    private var notFirst = false
    private var currentX: Float = 0.0F; private var currentY: Float = 0.0F; private var currentZ: Float = 0.0F
    private var lastX: Float = 0.0F; private var lastY: Float = 0.0F; private var lastZ: Float = 0.0F;
    private var xDif = 0.0f; private var yDif = 0.0f; private var zDif = 0.0f;
    private lateinit var bottomNavigationView: BottomNavigationView
    private var REQUEST_RECORD_PERMISSION = 100;


    lateinit var speechRec: SpeechRecognizer
    lateinit var recognizerIntent: Intent;
    private var LOG_TAG = "VoiceRecognitionActivity";

    val SHARED_PREFS = "sharedPrefs";
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

    var longitude: Double = 0.0
    var latitude: Double = 0.0
    //executes when activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        //A majority of the beginning of this onCreate function is the setup that makes the app functionality possible

        //passes bundle around classes, Bundle is where pass items between classes
        super.onCreate(savedInstanceState)

        //set the tts functionality of the phone
        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                mTTS!!.language = Locale.US
            }
        })
        //Set up speech recognition--------------------------------------------------------------------------------------
        speechRec = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        speechRec.setRecognitionListener(this);
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //The intent
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault()); // The default language of the device
        //^^^^^^setting the language ensures that it will only recognize words from that language

        //handles an orientation change of the device--------------------------------------------------------------------
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //Request record audio permissions-------------------------------------------------------------------
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                102
            )
        }
        //Request location/GPS permissions-------------------------------------------------------------------
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                52
            )
        }
        //Request calling and sms permissions ------------------------------------------------------------
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                101
            )
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS),
                103
            )
        }
        //------------------------------------------------------------------------------------------------
        //------------------------------------------------------------------------------------------------
        //bind activity to xml file
        setContentView(R.layout.activity_home_page)

        //Create location request, on each callback, convert it to mph and display it via tv_loc.text
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setSmallestDisplacement(0F);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setWaitForAccurateLocation(true);
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    var tempInt = (location.speed * 2.2369f).toInt() //conversion to MPH
                    tv_loc.text = tempInt.toString() //Display to homescreen
                }
            }
        }
        //Create location client to request location updates---------------------------------------------
        mLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

        //Create a sensor manager to handle the accelerometer sensor-------------------------------------
        msensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        maccelerometer = msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if(msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
        {
            maccelerometer = msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            isAccelAvail = true
        }
        else
        {
            isAccelAvail = false
        }
        //Setup Bluetooth Connection---------------------------------------------------------------------
        blueToothConnection = NavecureBlueTooth(this)
        blueToothConnection?.onBluetooth()

        blueToothDeviceList.setOnItemClickListener { _, _, i, _ -> //Clicking the bluetooth list

            if (blueToothDevices?.isNotEmpty()!!){

                blueToothConnection?.connect(blueToothDevices!![i].subSequence(blueToothDevices!![i].length-17,
                    blueToothDevices!![i].length).toString())

                blueToothConnection?.onDataLoadFinish(object:NavecureBlueTooth.ConnectedBluetooth{
                    override fun onConnectionState(state: NavecureBlueTooth.Connected) {
                        when(state){

                            NavecureBlueTooth.Connected.True->{
                                Toast.makeText(applicationContext,"Connection successful",Toast.LENGTH_SHORT).show()
                                blueToothDeviceList?.visibility = View.GONE
                                disconnectButton?.visibility = View.VISIBLE
                                pingConnectionView?.visibility   = View.VISIBLE

                            }

                            NavecureBlueTooth.Connected.Pending->{
                                Toast.makeText(applicationContext,"Connecting",Toast.LENGTH_SHORT).show()
                                textReceived()

                            }

                            NavecureBlueTooth.Connected.False->{
                                Toast.makeText(applicationContext,"Failed to connect",Toast.LENGTH_SHORT).show()
                            }

                            NavecureBlueTooth.Connected.Disconnected->{
                                Toast.makeText(applicationContext,"Bluetooth connection lost",Toast.LENGTH_SHORT).show()
                                disconnectButton?.visibility = View.INVISIBLE
                                blueToothDeviceList?.visibility = View.VISIBLE
                            }

                        }
                    }
                })
            }
        }
        //If pressed, disconnect the bluetooth connection
        disconnectButton?.setOnClickListener {
            blueToothConnection?.onClose()
        }

        //Set up bottom bottom navigation bar, and functionality-----------------------------------------
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
//                R.id.home -> WE DONT NEED TO SET UP ANYTHING BC WE ARE ALREADY ON HOME

                R.id.profile-> {
                    //navigate to profile section if pressed
                    val profile_intent = Intent(this, ProfileActivity::class.java)
                    startActivity(profile_intent)

                }
                R.id.log_out -> {
                    //log out of application if pressed
                    val logout_intent = Intent(this, SignUpActivity::class.java)
                    //clear shared preferences
                    //this clears out the saved profile information
                    var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                    sharedPreferences.edit().clear().commit()
                    var auth = Firebase.auth
                    auth.signOut()
                    //navigate back to login page, using logout activity
                    startActivity(logout_intent)
                    this.finish() //this clears out the current activity
                }

            }
            true
        }
        // This section checks current shared preferences------------------------------------------------------
        var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val number = sharedPreferences.getString(vephone, "")
        val fn = sharedPreferences.getString(vfname, "")
        val ln = sharedPreferences.getString(vlname, "")
        val cm = sharedPreferences.getString(vmake, "")
        val cmdl = sharedPreferences.getString(vmodel, "")
        val col = sharedPreferences.getString(vcolor, "")
        val yr = sharedPreferences.getString(vyear, "")
        val efn = sharedPreferences.getString(vename, "")
        val eln = sharedPreferences.getString(vlname, "")
        val epn = sharedPreferences.getString(vephone, "")
        //If there are no current sharedpreferences, it will ask the user to save some
        if (number == "" || fn == "" || ln == "" || cm == "" ||
            cmdl == "" || col == "" || yr == "" || efn == "" ||
            eln == "" || epn == "")
        {
            Toast.makeText(this, "Please make sure to fill out profile information", Toast.LENGTH_SHORT).show()
        }
    }
    //If the accelerometer sensor detects a change in value-------------------------------------------------------------------
    override fun onSensorChanged(event: SensorEvent)
    {
        // Get the initial x,y,z values
        currentX = event.values[0]
        currentY = event.values[1]
        currentZ = event.values[2]
        //for the second time around
        if(notFirst)
        {
            //compare x,y,z values to previous values via their difference
            xDif = Math.abs(lastX-currentX)
            yDif = Math.abs(lastY-currentY)
            zDif = Math.abs(lastZ-currentZ)
            //If there is a major change in 2 directions, it will count as a major sudden movement
            if((xDif > 15f && yDif > 15f || xDif > 15f && zDif > 15f || yDif > 15f && zDif > 15f) && sensor == 0)
            {
                sensor = 1 //sensor boolean value for
                playBreakSound()
                //display and ask user
                returnedText.text = "Do you need any assistance?"
                mTTS!!.speak("I sensed some movement, do you need any assistance?", TextToSpeech.QUEUE_FLUSH, null,"")
                //sleep allows the tts time to speak
                Thread.sleep(3_000)
                alert2 = 1
                askSpeechInput()
            }
        }
        //current x,y,z become last x,y,z for next time
        lastX = currentX
        lastY = currentY
        lastZ = currentZ
        notFirst = true
    }
    //------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------
    override fun onAccuracyChanged(sensor: Sensor, i:Int)
    {
    }
    override fun onResume()
    {
        super.onResume()
        if(isAccelAvail)
            msensorManager.registerListener(this, maccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        if(isAccelAvail)
            msensorManager.unregisterListener(this)
    }
//Handles the request of user speech-------------------------------------------------------------------
    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_SHORT).show()
        } else
        {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_PERMISSION)

        }
    }
//Media player / sound effect portion-------------------------------------------------------------------
//Stops any current sound if the media player is already playing
//Media player needs to reset before setting a new source
    private fun playAlertSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.bell)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else
        {
            if(mMediaPlayer!!.isPlaying())
                mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
            mMediaPlayer = MediaPlayer.create(this, R.raw.bell)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        }
}
    private fun playSuccessSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.success)
            mMediaPlayer!!.start()
        }
        else
        {
            if(mMediaPlayer!!.isPlaying())
                mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
            mMediaPlayer = MediaPlayer.create(this, R.raw.success)
            mMediaPlayer!!.start()
        }
    }
    private fun playStartupSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.chime)
            mMediaPlayer!!.start()
        }
        else
        {
            if(mMediaPlayer!!.isPlaying())
                mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
            mMediaPlayer = MediaPlayer.create(this, R.raw.chime)
            mMediaPlayer!!.start()
        }
    }
    private fun playBreakSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.ring)
            mMediaPlayer!!.start()
        }
        else
        {
            if(mMediaPlayer!!.isPlaying())
                mMediaPlayer!!.stop()
            mMediaPlayer!!.reset()
            mMediaPlayer = MediaPlayer.create(this, R.raw.ring)
            mMediaPlayer!!.start()
        }
    }
//Text Received refers to any bluetooth signals that have been received from the arduino
    private fun textReceived() {

        blueToothConnection?.onLoadReceived(object:NavecureBlueTooth.ReceivedData{
            override fun onReceive(receivedText: String) {
                //The bluetooth connection seems to be receiving more values than we intended
                //We filter out the extraneous values to just focus on the 3, 4, 5, signals sent from the camera
                // 3 = Setup camera to monitor
                // 4 = driver found
                // 5 = driver isnt found
                if(receivedText.contains("3") || receivedText.contains("4") || receivedText.contains("5"))
                {
                    //Get current time each time a 5 is received and the driver still isnt found
                    if(receivedText.contains("5") && found == 0)
                        end = System.currentTimeMillis().toInt()
                    //only focus on new signals, we dont need to focus on repeat signals
                    if(receivedText != last)
                    {
                        notfound = 0 //boolean for driver not being found
                        notfound2 = 0//boolean for driver not being found for a while
                        alert = 0
                        last = receivedText
                        if(receivedText.contains("3"))
                        {
                            playStartupSound()
                        }
                        else if(receivedText.contains("4"))
                        {

                            found = 1 //boolean for if the driver has been found
                            begin = 0
                            end = 0
                            //handles playing the jingle and changing the driver icon
                            //only want to play this icon once
                            if(played == 0)
                            {
                                playSuccessSound()
                                played = 1//plays success
                                alert = 0
                                alert2 = 0
                                driverstatus.setImageResource(R.drawable.found)
                                driverText.text = "Driver Found!"
                            }

                        }
                        else if(receivedText.contains("5"))
                        {
                            //start initial timer that the driver hasnt been found
                            begin = System.currentTimeMillis().toInt()
                            found = 0
                        }

                    }
                    //driver hasnt been found for 4 seconds (this value can be changed)
                    else if(end - begin > 4000 && receivedText.contains("5"))
                    {
                        if (notfound == 0)
                        {
                            notfound = 1

                            if (alert == 0)
                            {
                                played = 0
                                alert = 1
                                driverstatus.setImageResource(R.drawable.lost)
                                driverText.text = "Driver Not Found! Please pay attention to the road"
                                playAlertSound()//plays bell sound
                            }

                        }
                        //driver hasnt been found for 13 seconds (this value can be changed and adjusted)
                        if (end - begin > 13000 && notfound2 == 0)
                        {
                            notfound2 = 1
//                            terminalText.text = "Do you need any assistance?"
//                            message = terminalText.text.toString()
                            if(alert2 == 0)
                            {
                                playBreakSound()
                                //Ask driver if they need assistance
                                returnedText.text = "Do you need any assistance?"
                                mTTS!!.speak("Do you need any assistance?", TextToSpeech.QUEUE_FLUSH, null,"")
                                Thread.sleep(2_000)
                                alert2 = 1
                                askSpeechInput()
                            }
                        }
                    }
                }
            }
        }
        )
    }
//Request permission function--------------------------------------------------------------------------
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //Record audio
        if (requestCode == REQUEST_RECORD_PERMISSION)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speechRec.startListening(recognizerIntent);
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
            return
        }
        //Bluetooth
        if (blueToothConnection?.onPermissions(requestCode, grantResults) == true){
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            blueToothConnection?.onInitialize()
        }
        else
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }


    override fun onStart() {

        if (!blueToothConnection?.onState()!!){
            blueToothConnection?.onInitialize()
        }else{

            blueToothDevices = blueToothConnection?.deviceList()
            val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1,
                blueToothDevices!!
            )
            blueToothDeviceList?.adapter  = adapter

        }
        super.onStart()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (mTTS != null) {
            mTTS!!.stop()
            mTTS!!.shutdown()
        }
        super.onDestroy()
    }

    fun onRationaleAccepted(requestCode: Int) {
    }

    fun onRationaleDenied(requestCode: Int) {
    }

    //set longitude and latitude on every location change
    override fun onLocationChanged(loc: Location) {
        longitude = loc.longitude;
        latitude = loc.latitude;
    }
//logs ready for speech
    override fun onReadyForSpeech(p0: Bundle?) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }
//logs beginning of speech

    override fun onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");

    }

    override fun onRmsChanged(rmsdB: Float) {

    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }
//logs end of speech
    override fun onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
    }
//if there was an error during recording (USED TO HANDLE NO RESPONSE)
    override fun onError(errorCode: Int) {
        val errorMessage: String = getErrorText(errorCode)
    //Ignores client side error, due to a bug
        if (errorMessage != "Client side error") {
            Log.d(LOG_TAG, "FAILED $errorMessage")
            returnedText.text = errorMessage
            //If no emergency detected, ask to respond or else will send help automatically
            //this also sets emergency to be true
            if (!emergency)
            {

                Thread.sleep(1_000)
                returnedText.text = "There was no response, please respond or I will seek help automatically"
                mTTS!!.speak(
                    "There was no response, please respond or I will seek help automatically",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )

                Thread.sleep(5_000)
                emergency = true
                askSpeechInput()
            }
            //still no response
            else
            {
                Thread.sleep(1_000)
                returnedText.text = "Okay, I'm sending out a distress signal now"
                mTTS!!.speak(
                    "Okay, I'm sending out a distress signal now",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                Thread.sleep(4_000)
                //get the saved sharedpreferences
                //this gets the profile information
                var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

                val number = sharedPreferences.getString(vephone, "")
                val fn = sharedPreferences.getString(vfname, "")
                val ln = sharedPreferences.getString(vlname, "")
                val cm = sharedPreferences.getString(vmake, "")
                val cmdl = sharedPreferences.getString(vmodel, "")
                val col = sharedPreferences.getString(vcolor, "")
                val yr = sharedPreferences.getString(vyear, "")
                val efn = sharedPreferences.getString(vename, "")
                val eln = sharedPreferences.getString(vlname, "")
                val epn = sharedPreferences.getString(vephone, "")
                val rel = sharedPreferences.getString(verelate, "")
                val plate = sharedPreferences.getString(vplate,"")
                //create message string to be sent to the police
                var messageToSend = "AUTOMATIC EMERGENCY RESPONSE\nMy driver ($fn $ln) seems to need help. They are located at $longitude longitude" +
                        " and $latitude latitude. They should be in a $col $yr $cm $cmdl with a plate that reads $plate \nThey have an emergency contact" +
                        "named $efn $eln, it should be their $rel. Their number is $epn. Please send help as soon as possible!"
                var smsmanager = SmsManager.getDefault()
                //WE SEND IT TO THE EMERGENCY CONTACT FOR TESTING PURPOSES
                smsmanager.sendTextMessage(number, null, messageToSend, null, null)
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                returnedText.text = "Sent, help should be on the way shortly"
                mTTS!!.speak(
                    "Sent, help should be on the way shortly",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                Thread.sleep(3_000)
                sensor = 0
            }
        }
    }
//This handles the on results of the User Speech Call
    override fun onResults(results: Bundle?) {
        Log.i(LOG_TAG, "onResults")
        //get their speech as a string array lsit
        val matches: ArrayList<String> = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
        output = matches!!.get(0)
        Log.d("message: ", output)

            resume = false
            emergency = false
            //first line of questioning
            if (speech == 0)
            {
                speech = 1
                //if their speech has yes or yea in it
                if (output!!.contains("yes") or output!!.contains("yeah") or
                    output!!.contains("Yes") or output!!.contains("Yeah"))
                {
                    Thread.sleep(1_000)
                    returnedText.text = "Okay, would you like me to contact the police or your emergency contact?"
                    mTTS!!.speak("Okay, would you like me to contact the police or your emergency contact?", TextToSpeech.QUEUE_FLUSH, null, "")
                    Thread.sleep(4_000)
                    askSpeechInput()
                }
                //if they said no nah, stop, or cancel
                else if (output!!.contains("no") or output!!.contains("nah") or output!!.contains("stop") or output!!.contains("cancel")
                    or output!!.contains("No") or output!!.contains("Nah") or output!!.contains("Stop") or output!!.contains("Cancel"))
                {
                    Thread.sleep(1_000)
                    returnedText.text = "Alright, please be safe and pay attention to the road"
                    mTTS!!.speak("Alright, please be safe and pay attention to the road", TextToSpeech.QUEUE_FLUSH, null, "")
                    Thread.sleep(2_000)
                    Log.d("message: ", "you said no")
                    speech = 0
                    sensor = 0
                }
                //this handles if their speech didnt actually answer the question
                else
                {
                    Thread.sleep(1_000)
                    returnedText.text = "I didn't understand, do you need assistance?"
                    mTTS!!.speak("I didn't understand, do you need assistance?", TextToSpeech.QUEUE_FLUSH, null, "")
                    Thread.sleep(5_000)
                    speech = 0
                    askSpeechInput()
                }
            }
            //this is the second line of questioning, signaled by the speech boolean variable
            else
            {
                //if they want to call the police
                if (output!!.contains("police") or output!!.contains("ambulance") or output!!.contains("911") or
                    output!!.contains("Police") or output!!.contains("Ambulance"))
                {
                    Thread.sleep(1_000)
                    returnedText.text = "Okay, messaging the police now"
                    mTTS!!.speak("Okay, calling the police now", TextToSpeech.QUEUE_FLUSH, null, "")
                    Thread.sleep(2_000)

                    speech = 0
                    sensor = 0

                }
                //if they want to call their emergency contact
                else if (output!!.contains("emergency contact") or output!!.contains("family") or output!!.contains("guardian") or
                    output!!.contains("Emergency contact") or output!!.contains("Family") or output!!.contains("Guardian"))
                {
                    Thread.sleep(1_000)
                    returnedText.text = "Okay, calling your emergency contact now"
                    mTTS!!.speak("Okay, calling your emergency contact now", TextToSpeech.QUEUE_FLUSH, null, "")
                    Thread.sleep(2_000)
                    //gets their profile information via sharedpreferences
                    var sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

                    val number = sharedPreferences.getString(vephone, "")
                    val intent = Intent(Intent.ACTION_CALL)
                    //start intent to call
                    intent.data = Uri.parse("tel:$number")
                    startActivity(intent)
                    speech = 0
                    sensor = 0
                }
                //if they would like to cancel at this point
                else if (output!!.contains("cancel") or output!!.contains("Cancel"))
                {
                    Thread.sleep(1_000)
                    returnedText.text = "Alright, please be safe and pay attention to the road"
                    mTTS!!.speak("Alright, please be safe and pay attention to the road", TextToSpeech.QUEUE_FLUSH, null, "")
                    Thread.sleep(2_000)
                    speech = 0
                    sensor = 0
                }
                //if their response did not answer the question
                else
                {
                    Thread.sleep(1_000)
                    returnedText.text = "I didn't understand, police or emergency contact? Or say cancel"
                    mTTS!!.speak("I didn't understand, police or emergency contact? Or say cancel", TextToSpeech.QUEUE_FLUSH, null, "")
                    Thread.sleep(5_000)
                    speech = 1
                    askSpeechInput()
                }

            }
    }


    override fun onPartialResults(results: Bundle?) {
        Log.i(LOG_TAG, "onResults")
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        Log.i(LOG_TAG, "onEvent");
    }
    //get the type of error and return that as a string message
    private fun getErrorText(errorCode: Int): String {
        val message: String
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> message = "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> message = "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> message = "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> message = "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> message = "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "No speech input"
            else -> message = "Didn't understand, please try again."
        }
        return message
    }

}


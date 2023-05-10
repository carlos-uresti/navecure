package com.example.navecure.activities
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import java.util.*
import java.io.*
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.collections.ArrayList
import android.annotation.SuppressLint

class NavecureBlueTooth(val context: Context) : Serializable{

    //states for bt connection
    enum class Connected
    {
        False, Pending, True, Disconnected
    }

    //bt adapter
    private val blueToothAdapter: BluetoothAdapter = (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

    //bt socket
    private var blueToothSocket: BluetoothSocket? = null

    //uuid to identify android device
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    //will store data stream from arduino board
    private var inputStream: InputStream? = null

    //array for permissions
    private var requiredPermissions = listOf<String>()
    private var grantedPermissions = false


    //initialize bluetooth permission request
    @SuppressLint("MissingPermission")
    fun onInitialize() {
        (context as Activity).startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    //if granted permissions cannot be verified ask again
    fun onBluetooth(){
        if (!grantedPermissions){
            onVerify()
        }else{
            onInitialize()
        }

    }

    //verify bt permission by sdk version
    private fun onVerify() {
        requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH)
        } else {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        }

        val permissions = requiredPermissions.filter { permission ->
            context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
        if (permissions.isEmpty()) {
            onInitialize()
        } else {
            (context as Activity).requestPermissions(permissions.toTypedArray(),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }
    //bt requested code per android
    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 9999

    }
    //enable adapter
    fun onState() = blueToothAdapter.isEnabled

    //check bluetooth permissions
    fun onPermissions(requestCode: Int, grantResults: IntArray):Boolean {
        return when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
                    grantedPermissions = true
                    onInitialize()
                    return true
                } else {
                    grantedPermissions = true
                    (context as Activity).requestPermissions(
                        requiredPermissions.toTypedArray(),
                        BLUETOOTH_PERMISSION_REQUEST_CODE
                    )
                    return false
                }
            }
            else -> false
        }

    }


    //device list
    @SuppressLint("MissingPermission")
    fun deviceList():ArrayList<String>{
        val pairedDevices = blueToothAdapter.bondedDevices
        val deviceList = ArrayList<String>()
        for (i in pairedDevices){
            deviceList.add(i.name+"\n"+i.address)
        }
        return deviceList
    }

    //bluetooth connected interface
    interface ConnectedBluetooth{
        fun onConnectionState(state: Connected)
    }
    //end of data received
    private var connectionEnd: ConnectedBluetooth? = null

    //load data after connection ends
    fun onDataLoadFinish(data: ConnectedBluetooth) {
        this.connectionEnd = data
    }

    //update after loading
    private fun onUpdateState(state:Connected) {
        connectionEnd!!.onConnectionState(state)
    }

    //open socket for bluetooth device address, update state accordingly
    @SuppressLint("MissingPermission")
    fun connect(address:String){
        val deviceAddress = address.subSequence(address.length-17,address.length).toString()
        val device = blueToothAdapter.getRemoteDevice(deviceAddress)
        blueToothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
        //cancel bluetooth device discovery
        blueToothAdapter.cancelDiscovery()
        thread(start = true){
            onUpdateConnection(Connected.Pending)
            Connected.Pending
            try {
                blueToothSocket?.connect()
                Connected.True
                inputStream()
                onUpdateConnection(Connected.True)
            }catch(e:Exception){
                onUpdateConnection(Connected.False)
            }
        }
    }

    //data received interface
    interface ReceivedData{
        fun onReceive(receivedData: String)
    }
    //received data
    private var receivedData: ReceivedData? = null

    //load data after it is received
    fun onLoadReceived(data: ReceivedData) {
        this.receivedData = data
    }

    //update after message received
    private fun onUpdateReceived(message:String) {
        (context as Activity).runOnUiThread {
            receivedData?.onReceive(message)
        }
    }

    //create an input stream from the bluetooth socket connection
    private fun inputStream() {
        var inputData: InputStream? = null
        inputData = blueToothSocket?.inputStream
        inputStream = inputData
        onBluetoothReceive()
    }

    //handle data received through bluetooth connection
    private fun onBluetoothReceive() {
        thread(start = true){
            while (true){
                val input = BufferedReader(InputStreamReader(inputStream))
                val receivedText = input.readLine()
                onUpdateReceived(receivedText)
            }
        }
    }

    //handle connection update
    private fun onUpdateConnection(connection:Connected){
        (context as Activity).runOnUiThread {
            onUpdateState(connection)
        }

    }

    //handle closing connection
    fun onClose(){
        if (blueToothSocket!=null){
            blueToothSocket?.close()
            onUpdateConnection(Connected.Disconnected)
            blueToothSocket = null
        }
    }
}
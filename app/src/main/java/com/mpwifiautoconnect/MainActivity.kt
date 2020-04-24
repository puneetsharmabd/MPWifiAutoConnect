package com.mpwifiautoconnect

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.wifi.WifiInfo
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE_ACCESS_LOCATION = 123

    var resultList = ArrayList<ScanResult>()
    lateinit var wifiManager: WifiManager

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            resultList = wifiManager.scanResults as ArrayList<ScanResult>
            Log.d("TESTING", "onReceive Called"+resultList.size)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                10)
            return
        }
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        startScanning()
    }

    fun startScanning() {
        registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        Handler().postDelayed({
            stopScanning()
        }, 10000)
    }

    fun stopScanning() {
        unregisterReceiver(broadcastReceiver)
        Log.d("TESTING", resultList.size.toString())
        Log.d("TESTING", "stopScanning called")
        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val list = wifiManager.configuredNetworks
        Log.d("TESTING", "configuredNetworks"+list.size.toString())
        if (mWifi.isConnected)
        {
            val info = wifiManager.connectionInfo
            val ssid = info.ssid
            Log.d("TESTING", ""+ssid)
            for ((index,id) in resultList.withIndex())
            {
                if (!ssid.equals(id.SSID))
                {
                    wifiManager.removeNetwork(list.get(index).networkId)
                    wifiManager.disableNetwork(list.get(index).networkId)
                }
                else
                {
                    wifiManager.enableNetwork(list.get(index).networkId,true)
                    wifiManager.saveConfiguration()

                }
            }
        }

        startScanning()

    }

    override fun onResume() {
        super.onResume()
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("++", "Displaying permission rationale to provide additional context.")
            startLocationPermissionRequest()
        } else {
            Log.i("++", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            10)
    }
}

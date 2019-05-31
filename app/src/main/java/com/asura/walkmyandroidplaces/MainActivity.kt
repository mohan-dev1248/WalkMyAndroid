package com.asura.walkmyandroidplaces

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnTaskCompleted {

    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var mLastLocation: Location
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getLocation()

    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this
                , arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                , REQUEST_LOCATION_PERMISSION
            )
        } else {
            Log.i(TAG, "Location Permission is already there")
            mFusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    mLastLocation = it
                    locationTextView.text = getString(R.string.location_text,
                        mLastLocation.latitude,
                        mLastLocation.longitude,
                        mLastLocation.time)
                    FetchAddressTask(this).execute(it)
                } else {
                    locationTextView.text = getString(R.string.no_location)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    getLocation()
                } else {
                    Toast.makeText(
                        this,
                        R.string.location_permission_denied,
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

    override fun onTaskCompleted(result: String) {
        addressTextView.text = getString(
            R.string.address_text,
            result,
            System.currentTimeMillis()
        )
    }
}

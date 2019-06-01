package com.asura.walkmyandroidplaces

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnTaskCompleted {

    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var mRotateAnime: AnimatorSet

    private lateinit var mLastLocation: Location
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var mTrackingLocation = false

    private var mDistance = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRotateAnime = AnimatorInflater.loadAnimator(
            this,
            R.animator.rotate
        ) as AnimatorSet

        mRotateAnime.setTarget(androidImageView)

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
                    locationTextView.text = getString(
                        R.string.location_text,
                        mLastLocation.latitude,
                        mLastLocation.longitude,
                        mLastLocation.time,
                        System.currentTimeMillis()
                    )
                } else {
                    locationTextView.text = getString(R.string.no_location)
                }
            }
        }
    }

    private fun startTrackingLocation() {
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

            mTrackingLocation = true

            trackerButtonTextView.text = getString(R.string.press_the_button_to_stop_tracking)
            trackerButton.text = getString(R.string.stop_tracker_button)

            val locationCallback = CLocationCallback(
                this,
                this,
                mTrackingLocation
            )
            mFusedLocationProviderClient.requestLocationUpdates(
                getLocationRequest(),
                locationCallback,
                null
            )
            mRotateAnime.start()
        }
    }

    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun stopTrackingLocation() {
        mTrackingLocation = false
        mDistance = 0F
        trackerButtonTextView.text = getString(R.string.press_the_button_to_start_tracking)
        trackerButton.text = getString(R.string.start_tracker_button)

        mRotateAnime.end()
    }

    fun toggleTracking(view: View) {
        if (mTrackingLocation) stopTrackingLocation() else startTrackingLocation()
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
                    ).show()
                }
            }
        }
    }

    override fun onTaskCompleted(resultPair: Pair<Location?,Pair<String,Float>>) {
        if (mTrackingLocation) {
            mDistance += resultPair.second.second
            addressTextView.text = getString(
                R.string.address_text,
                resultPair.second.first,
                System.currentTimeMillis(),
                mDistance
            )

            locationTextView.text = getString(
                R.string.location_text,
                resultPair.first?.latitude,
                resultPair.first?.longitude,
                resultPair.first?.time,
                System.currentTimeMillis()
            )
        }
    }

    override fun onPause() {
        if (mTrackingLocation) {
            stopTrackingLocation()
            mTrackingLocation = true
        }
        super.onPause()
    }

    override fun onResume() {
        if (mTrackingLocation) {
            startTrackingLocation()
        }
        super.onResume()
    }
}
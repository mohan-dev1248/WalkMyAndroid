package com.asura.walkmyandroidplaces

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference
import java.util.*

interface OnTaskCompleted{
    fun onTaskCompleted(resultPair: Pair<Location?,Pair<String, Float>>)
}

class FetchAddressTask(private val notifier: OnTaskCompleted) :
    AsyncTask<Location, Unit, Pair<Location?,Pair<String, Float>>>() {

    companion object {
        private const val TAG = "FetchAddressTask"
        private var previousLocation: Location? = null
    }

    //Currently the OnTaskCompleted's notifier will be a Context instance
    //so getting them in the same parameter and converting it into second one later
    private val ctx: WeakReference<Context> = WeakReference(notifier as Context)

    override fun doInBackground(vararg param: Location?): Pair<Location?,Pair<String, Float>> {
        val geoCoder = Geocoder(ctx.get(), Locale.getDefault())
        val location = param[0]
        var addresses: List<Address>? = null
        var resultMessage = ""

        try {
            addresses = geoCoder.getFromLocation(
                location!!.latitude,
                location!!.longitude,
                1
            )
        } catch (e: IOException) {
            resultMessage = ctx.get()!!.getString(R.string.service_not_available)
            Log.e(TAG, resultMessage, e)
            return Pair(location,Pair(resultMessage,0F))
        } catch (e: IllegalArgumentException) {
            resultMessage = ctx.get()!!.getString(R.string.invalid_lat_long_used)
            Log.e(TAG, resultMessage, e)
            return Pair(location,Pair(resultMessage,0F))
        }

        if (addresses == null || addresses.isEmpty()) {
            resultMessage = ctx.get()!!.getString(R.string.address_not_found)
            Log.i(TAG, resultMessage)
            return Pair(location,Pair(resultMessage,0F))
        }

        val address = addresses.get(0)
        var addressParts = mutableListOf<String>()
        for(i in 0..address.maxAddressLineIndex){
            addressParts.add(address.getAddressLine(i))
        }

        resultMessage = addressParts.joinToString (separator = "\n")

        Log.i(TAG,"Before updating the distance")
        var distance = 0F
        if(previousLocation!=null){
            Log.i(TAG,"Updating the distance")
            distance = previousLocation!!.distanceTo(location)
        }
        previousLocation = location

        return Pair(location,Pair(resultMessage,distance))
    }

    override fun onPostExecute(resultPair: Pair<Location?,Pair<String, Float>>) {
        notifier.onTaskCompleted(resultPair)
    }
}
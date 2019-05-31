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
    fun onTaskCompleted(result: String)
}

class FetchAddressTask(private val notifier: OnTaskCompleted) : AsyncTask<Location, Unit, String>() {

    companion object {
        private const val TAG = "FetchAddressTask"
    }

    private val ctx: WeakReference<Context> = WeakReference(notifier as Context)

    override fun doInBackground(vararg param: Location?): String {
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
            return resultMessage
        } catch (e: IllegalArgumentException) {
            resultMessage = ctx.get()!!.getString(R.string.invalid_lat_long_used)
            Log.e(TAG, resultMessage, e)
            return resultMessage
        }

        if (addresses == null || addresses.isEmpty()) {
            resultMessage = ctx.get()!!.getString(R.string.address_not_found)
            Log.i(TAG, resultMessage)
            return resultMessage
        }

        val address = addresses.get(0)
        var addressParts = mutableListOf<String>()
        for(i in 0..address.maxAddressLineIndex){
            addressParts.add(address.getAddressLine(i))
        }

        resultMessage = addressParts.joinToString (separator = "\n")
        return resultMessage
    }

    override fun onPostExecute(result: String) {
        notifier.onTaskCompleted(result)
    }
}
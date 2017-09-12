package com.example.android.shushme

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.PlaceBuffer

/**
 * Created by pmvb on 17-09-11.
 */
class Geofencing(context: Context, googleApiClient: GoogleApiClient) : ResultCallback<Status> {

    companion object {
        @JvmField val TAG = Geofencing::class.java.simpleName
        @JvmStatic
        private val GEOFENCE_TIMEOUT: Long = 24 * 60 * 60 * 1000 // 1 Hour in milliseconds
        @JvmStatic
        private val GEOFENCE_RADIUS = 50.toFloat()
        @JvmField
        val RC_GEOFENCE_BROADCAST = 4000
    }

    private var mContext = context
    private var mGoogleApiClient = googleApiClient
    private var mGeofencePendingIntent: PendingIntent? = null
    private var mGeofenceList = mutableListOf<Geofence>()

    fun updateGeofencesList(placeBuffer: PlaceBuffer) {
        val geofenceBuilder = Geofence.Builder()
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .setExpirationDuration(GEOFENCE_TIMEOUT)
        val geofenceList = placeBuffer.map { place ->
            geofenceBuilder
                    .setRequestId(place.id)
                    .setCircularRegion(
                            place.latLng.latitude,
                            place.latLng.longitude,
                            GEOFENCE_RADIUS
                    )
                    .build()
        }
        mGeofenceList = geofenceList.toMutableList()
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeofenceList)
                .build()
    }

    private fun getGeofencingIntent(): PendingIntent {
        if (mGeofencePendingIntent == null) {
            val broadcastIntent = Intent(mContext, GeofenceBroadcastReceiver::class.java)
            mGeofencePendingIntent = PendingIntent.getBroadcast(
                    mContext, RC_GEOFENCE_BROADCAST, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return mGeofencePendingIntent!!
    }

    fun registerAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected) {
            return
        }
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencingIntent()
        ).setResultCallback(this)
    }

    fun unregisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected) {
            return
        }
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencingIntent()
        ).setResultCallback(this)
    }

    override fun onResult(status: Status) {
        if (status.isCanceled) {
            Log.e(TAG, "Geofencing request canceled")
        } else if (status.isInterrupted) {
            Log.e(TAG, "Geofencing request interrupted")
        }
    }
}
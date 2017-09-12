package com.example.android.shushme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by pmvb on 17-09-11.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        val TAG = GeofenceBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context?, data: Intent?) {
        Log.d(TAG, "Broadcast received")
    }
}
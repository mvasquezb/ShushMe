package com.example.android.shushme

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * Created by pmvb on 17-09-11.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        val TAG = GeofenceBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context?, data: Intent?) {
        Log.d(TAG, "Broadcast received")
        val geofencingEvent = GeofencingEvent.fromIntent(data)
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error code: ${geofencingEvent.errorCode}")
            return
        }
        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                setRingerMode(context, AudioManager.RINGER_MODE_SILENT)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                setRingerMode(context, AudioManager.RINGER_MODE_NORMAL)
            }
            else -> {
                Log.e(TAG, "Unknown transition: ${geofencingEvent.geofenceTransition}")
                return
            }
        }
        sendNotification(context, geofencingEvent.geofenceTransition)
    }

    private fun sendNotification(context: Context?, geofenceTransition: Int) {
        if (context == null) {
            return
        }
        val notificationIntent = Intent(context, MainActivity::class.java)
        val notificationPendingIntent = TaskStackBuilder.create(context)
                .addParentStack(MainActivity::class.java)
                .addNextIntent(notificationIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.touch_to_relaunch))
                .setContentIntent(notificationPendingIntent)
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                context.resources,
                                R.drawable.ic_volume_off_white_24dp
                        ))
                        .setContentTitle(context.getString(R.string.silent_mode_activated))
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                context.resources,
                                R.drawable.ic_volume_up_white_24dp
                        ))
                        .setContentTitle(context.getString(R.string.back_to_normal))
            }
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(0, builder.build())
    }

    private fun setRingerMode(context: Context?, ringerMode: Int) {
        if (context == null) {
            return
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT < 24 || (
                android.os.Build.VERSION.SDK_INT >= 24 && nm.isNotificationPolicyAccessGranted)) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = ringerMode
        }
    }
}
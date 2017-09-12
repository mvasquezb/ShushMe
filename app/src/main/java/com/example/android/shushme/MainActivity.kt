package com.example.android.shushme

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.app.Activity
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.android.shushme.provider.PlaceContract
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker

class MainActivity :
        AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    companion object {
        // Constants
        val TAG = MainActivity::class.java.simpleName
        val PERMISSION_REQUEST_FINE_LOCATION = 2923
        val PLACE_PICKER_REQUEST = 35352
    }

    // Member variables
    private lateinit var mAdapter: PlaceListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mGoogleClient: GoogleApiClient
    private lateinit var mGeofencing: Geofencing
    private var mGeofencesEnabled: Boolean = false

    /**
     * Called when the activity is starting

     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the recycler view
        mRecyclerView = findViewById(R.id.places_list_recycler_view) as RecyclerView
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = PlaceListAdapter(this)
        mRecyclerView.adapter = mAdapter

        // Setup initial geofence preference
        val geofencesSwitch = findViewById(R.id.enable_switch) as Switch
        mGeofencesEnabled = getPreferences(Context.MODE_PRIVATE).getBoolean(
                resources.getString(R.string.pref_enable_geofences_key),
                resources.getBoolean(R.bool.pref_enable_geofences_default)
        )

        geofencesSwitch.isChecked = mGeofencesEnabled
        geofencesSwitch.setOnCheckedChangeListener { button, checked ->
            if (checked) {
                mGeofencing.registerAllGeofences()
            } else {
                mGeofencing.unregisterAllGeofences()
            }
            Log.e(TAG, "Setting geofences preference")
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(
                    getString(R.string.pref_enable_geofences_key),
                    checked
            ).apply()
            Log.e(TAG, "Done setting geofences preference")
        }

        // Setup privacy link
        val privacyLink = findViewById(R.id.privacy_link) as TextView
        privacyLink.movementMethod = LinkMovementMethod.getInstance()

        mGoogleClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build()

        mGeofencing = Geofencing(context = this, googleApiClient = mGoogleClient)
    }

    override fun onResume() {
        super.onResume()
        val locationPermissionCheckbox = findViewById(R.id.location_permission_checkbox) as CheckBox
        val locationPermissionStatus = ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (locationPermissionStatus != PackageManager.PERMISSION_GRANTED) {
            locationPermissionCheckbox.isChecked = false
        } else {
            locationPermissionCheckbox.isChecked = true
            locationPermissionCheckbox.isEnabled = false
        }
        val ringerPermissions = findViewById(R.id.ringer_permission_checkbox) as CheckBox
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 24 &&
                !notificationManager.isNotificationPolicyAccessGranted) {
            ringerPermissions.isChecked = false
        } else {
            ringerPermissions.isChecked = true
            ringerPermissions.isEnabled = false
        }
    }

    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "API Client Connnection Successful")
        refreshPlacesData()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(TAG, "API Client Connection Suspended")
    }


    override fun onConnectionSuspended(cause: Int) {
        Log.e(TAG, "API Client Connection Failed")
    }

    /**
     * Refresh places data from server
     */
    fun refreshPlacesData() {
        val data = contentResolver.query(
                PlaceContract.PlaceEntry.CONTENT_URI,
                null, null, null, null
        )
        if (data == null || data.count == 0) {
            return
        }
        val placeIds = mutableListOf<String>()
        while (data.moveToNext()) {
            placeIds.add(data.getString(
                    data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)
            ))
        }
        val placesResult = Places.GeoDataApi.getPlaceById(mGoogleClient, *placeIds.toTypedArray())
        placesResult.setResultCallback { placeBuffer ->
            mAdapter.swapPlaces(placeBuffer)
            mGeofencing.updateGeofencesList(placeBuffer)
            if (mGeofencesEnabled) {
                mGeofencing.registerAllGeofences()
            }
        }
    }

    /**
     * Location permission checkbox click callback
     */
    fun onLocationPermissionClicked(view: View) {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION
        )
    }

    /**
     * Ringer permission checkbox click callback
     */
    fun onRingerPermissionClicked(view: View) {
        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
    }

    /**
     * Add location button click callback
     */
    fun onAddPlaceButtonClicked(view: View) {
        val locationPermissionStatus = ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (locationPermissionStatus != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                    this,
                    getString(R.string.location_permissions_required),
                    Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val pickerIntent = PlacePicker.IntentBuilder().build(this)
            startActivityForResult(pickerIntent, PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            Log.e(TAG, "GooglePlayServices not available: ${e.message}")
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.e(TAG, "GooglePlayServices not available: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "PlacePicker exception: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PLACE_PICKER_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = PlacePicker.getPlace(this, data)
                    if (place == null) {
                        Log.i(TAG, "No place selected")
                        return
                    }
                    // Get place information
                    val placeName = place.name
                    val placeAddress = place.address
                    val placeId = place.id
                    Log.i(TAG, "Place name: $placeName. Place Address: $placeAddress")

                    // Insert place information into db
                    val values = ContentValues()
                    values.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeId)
                    contentResolver.insert(PlaceContract.PlaceEntry.CONTENT_URI, values)

                    // Update place list
                    refreshPlacesData()
                }
            }
        }
    }
}

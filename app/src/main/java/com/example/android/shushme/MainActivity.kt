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

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places

class MainActivity :
        AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    companion object {
        // Constants
        val TAG = MainActivity::class.java.simpleName
        val PERMISSION_REQUEST_FINE_LOCATION = 2923
    }

    // Member variables
    private lateinit var mAdapter: PlaceListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mGoogleClient: GoogleApiClient

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

        mGoogleClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build()
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
    }

    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "API Client Connnection Successful")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(TAG, "API Client Connection Suspended")
    }


    override fun onConnectionSuspended(cause: Int) {
        Log.e(TAG, "API Client Connection Failed")
    }

    fun onLocationPermissionClicked(view: View) {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION
        )
    }

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
        } else {
            Toast.makeText(
                    this,
                    getString(R.string.location_permissions_granted),
                    Toast.LENGTH_SHORT
            ).show()
        }
    }
}

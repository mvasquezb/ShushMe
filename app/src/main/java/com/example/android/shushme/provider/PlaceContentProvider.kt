package com.example.android.shushme.provider

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

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

import com.example.android.shushme.provider.PlaceContract.PlaceEntry


class PlaceContentProvider : ContentProvider() {

    // Member variable for a PlaceDbHelper that's initialized in the onCreate() method
    private lateinit var mPlaceDbHelper: PlaceDbHelper

    override fun onCreate(): Boolean {
        mPlaceDbHelper = PlaceDbHelper(context)
        return true
    }

    /***
     * Handles requests to insert a single new row of data

     * @param uri
     * *
     * @param values
     * *
     * @return
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = mPlaceDbHelper.writableDatabase

        // Write URI matching code to identify the match for the places directory
        val match = sUriMatcher.match(uri)
        val returnUri: Uri // URI to be returned
        when (match) {
            PLACES -> {
                // Insert new values into the database
                val id = db.insert(PlaceEntry.TABLE_NAME, null, values)
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(PlaceContract.PlaceEntry.CONTENT_URI, id)
                } else {
                    throw android.database.SQLException("Failed to insert row into " + uri)
                }
            }
        // Default case throws an UnsupportedOperationException
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        context!!.contentResolver.notifyChange(uri, null)

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri
    }

    /***
     * Handles requests for data by URI

     * @param uri
     * *
     * @param projection
     * *
     * @param selection
     * *
     * @param selectionArgs
     * *
     * @param sortOrder
     * *
     * @return
     */
    override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
    ): Cursor? {

        // Get access to underlying database (read-only for query)
        val db = mPlaceDbHelper.readableDatabase

        // Write URI match code and set a variable to return a Cursor
        val match = sUriMatcher.match(uri)
        val retCursor: Cursor

        when (match) {
        // Query for the places directory
            PLACES -> retCursor = db.query(PlaceEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs, null, null,
                    sortOrder)
        // Default exception
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(context!!.contentResolver, uri)

        // Return the desired Cursor
        return retCursor
    }

    /***
     * Deletes a single row of data

     * @param uri
     * *
     * @param selection
     * *
     * @param selectionArgs
     * *
     * @return number of rows affected
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // Get access to the database and write URI matching code to recognize a single item
        val db = mPlaceDbHelper.writableDatabase
        val match = sUriMatcher.match(uri)
        // Keep track of the number of deleted places
        val placesDeleted: Int // starts as 0
        when (match) {
        // Handle the single item case, recognized by the ID included in the URI path
            PLACE_WITH_ID -> {
                // Get the place ID from the URI path
                val id = uri.pathSegments[1]
                // Use selections/selectionArgs to filter for this ID
                placesDeleted = db.delete(PlaceEntry.TABLE_NAME, "_id=?", arrayOf(id))
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
        // Notify the resolver of a change and return the number of items deleted
        if (placesDeleted != 0) {
            // A place (or more) was deleted, set notification
            context!!.contentResolver.notifyChange(uri, null)
        }
        // Return the number of places deleted
        return placesDeleted
    }

    /***
     * Updates a single row of data

     * @param uri
     * *
     * @param selection
     * *
     * @param selectionArgs
     * *
     * @return number of rows affected
     */
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        // Get access to underlying database
        val db = mPlaceDbHelper.writableDatabase
        val match = sUriMatcher.match(uri)
        // Keep track of the number of updated places
        val placesUpdated: Int

        when (match) {
            PLACE_WITH_ID -> {
                // Get the place ID from the URI path
                val id = uri.pathSegments[1]
                // Use selections/selectionArgs to filter for this ID
                placesUpdated = db.update(PlaceEntry.TABLE_NAME, values, "_id=?", arrayOf(id))
            }
        // Default exception
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        // Notify the resolver of a change and return the number of items updated
        if (placesUpdated != 0) {
            // A place (or more) was updated, set notification
            context!!.contentResolver.notifyChange(uri, null)
        }
        // Return the number of places deleted
        return placesUpdated
    }


    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    companion object {

        // Define final integer constants for the directory of places and a single item.
        // It's convention to use 100, 200, 300, etc for directories,
        // and related ints (101, 102, ..) for items in that directory.
        val PLACES = 100
        val PLACE_WITH_ID = 101

        // Declare a static variable for the Uri matcher that you construct
        private val sUriMatcher = buildUriMatcher()
        private val TAG = PlaceContentProvider::class.java.name

        // Define a static buildUriMatcher method that associates URI's with their int match
        fun buildUriMatcher(): UriMatcher {
            // Initialize a UriMatcher
            val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            // Add URI matches
            uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES, PLACES)
            uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES + "/#", PLACE_WITH_ID)
            return uriMatcher
        }
    }
}

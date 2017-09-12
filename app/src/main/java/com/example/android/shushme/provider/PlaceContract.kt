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

import android.net.Uri
import android.provider.BaseColumns

object PlaceContract {

    // The authority, which is how your code knows which Content Provider to access
    val AUTHORITY = "com.example.android.shushme"

    // The base content URI = "content://" + <authority>
    val BASE_CONTENT_URI = Uri.parse("content://$AUTHORITY")

    // Define the possible paths for accessing data in this contract
    // This is the path for the "places" directory
    val PATH_PLACES = "places"

    class PlaceEntry : BaseColumns {
        companion object {

            // TaskEntry content URI = base content URI + path
            val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build()

            val TABLE_NAME = "places"
            val COLUMN_PLACE_ID = "placeID"
        }
    }
}

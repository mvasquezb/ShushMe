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

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PlaceListAdapter(context: Context)
    : RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder>() {

    private val mContext = context

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item

     * @param parent   The ViewGroup into which the new View will be added
     * *
     * @param viewType The view type of the new View
     * *
     * @return A new PlaceViewHolder that holds a View with the item_place_card layout
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        // Get the RecyclerView item layout
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.item_place_card, parent, false)
        return PlaceViewHolder(view)
    }

    /**
     * Binds the data from a particular position in the cursor to the corresponding view holder

     * @param holder   The PlaceViewHolder instance corresponding to the required position
     * *
     * @param position The current position that needs to be loaded with data
     */
    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {

    }


    /**
     * Returns the number of items in the cursor

     * @return Number of items in the cursor, or 0 if null
     */
    override fun getItemCount(): Int {
        return 0
    }

    /**
     * PlaceViewHolder class for the recycler view item
     */
    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameTextView: TextView = itemView.findViewById(R.id.name_text_view) as TextView
        var addressTextView: TextView = itemView.findViewById(R.id.address_text_view) as TextView

    }
}

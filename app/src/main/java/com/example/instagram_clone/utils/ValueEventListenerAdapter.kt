package com.example.instagram_clone.utils

import android.content.ContentValues
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ValueEventListenerAdapter(val handler: (DataSnapshot) -> Unit) : ValueEventListener {
    override fun onDataChange(data: DataSnapshot) {
        handler(data)
    }

    override fun onCancelled(error: DatabaseError) {
        Log.e(ContentValues.TAG, "onCancelled: ", error.toException())
    }
}
package com.example.instagram_clone.ui

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.instagram_clone.R
import com.bumptech.glide.Glide

fun Context.showToast(text:String,duration:Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, text,duration).show()
}

fun coordinateBtnAndInputs(btn: Button, vararg inputs: EditText) {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled = inputs.all { it.text.isNotEmpty() }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.isNotEmpty() }
}

fun Editable.toStringOrNull(): String?{
    val str = toString()
    return if (str.isEmpty()) null else str
}

fun ImageView.loadUserPhoto(photoUrl: String?){
    if (!(context as Activity).isDestroyed){
        Glide.with(this).load(photoUrl).fallback(R.drawable.person).into(this)
    }
}




package com.example.instagram_clone.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.instagram_clone.ui.showToast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(private val activity: Activity){
    val REQUEST_CODE = 1
    var imageUri: Uri? = null
    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager!!) != null) {
            val imageFile = createImageFile()
            imageUri = FileProvider.getUriForFile(
                activity,
                "com.example.instagram_clone.fileprovider",
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            activity.startActivityForResult(intent, REQUEST_CODE)
        } else {
            activity.showToast("Try again")
        }

    }


    private fun createImageFile(): File {
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
    }
}
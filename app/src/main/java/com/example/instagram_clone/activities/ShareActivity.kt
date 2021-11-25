package com.example.instagram_clone.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.instagram_clone.R
import com.example.instagram_clone.models.User
import com.example.instagram_clone.utils.CameraHelper
import com.example.instagram_clone.utils.FirebaseHelper
import com.google.firebase.database.ServerValue
import com.example.instagram_clone.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_share.*
import java.util.*

class ShareActivity : BaseActivity(2) {
    private lateinit var mCamera: CameraHelper
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        setupBottomNavigation()
        mFirebase = FirebaseHelper(this)
        mCamera = CameraHelper(this)
        mCamera.takeCameraPicture()
        share_text.setOnClickListener { share()
            share_text.visibility = View.INVISIBLE
        }
        back_image.setOnClickListener{ finish()}
        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter{
            mUser = it.getValue(User::class.java)!!
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCamera.REQUEST_CODE && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                Glide.with(this).load(mCamera.imageUri).centerCrop().into(post_image)
            } else {
                finish()
            }
        }
    }


    private fun share() {
        val imageUri = mCamera.imageUri
        if (imageUri != null) {
            val uid = mFirebase.auth.currentUser!!.uid
            mFirebase.storage.child("users").child(uid).child("images")
                .child(imageUri.lastPathSegment!!).putFile(imageUri).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val imageDownloadUrl = it.result.downloadUrl!!.toString()
                        mFirebase.database.child("images").child(uid).push()
                            .setValue(imageDownloadUrl)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    mFirebase.database.child("feed-posts").child(uid).push()
                                        .setValue(mkFeedPost(uid, imageDownloadUrl)).addOnCompleteListener {
                                            if (it.isSuccessful){
                                                startActivity(Intent(this, ProfileActivity::class.java))
                                                finish()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        this,
                                        it.exception!!.message!!,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, it.exception!!.message!!, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun showToast(s:String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun mkFeedPost(
        uid: String,
        imageDownloadUrl: String
    ) = FeedPost(
        uid = uid,
        username = mUser.username,
        image = imageDownloadUrl,
        caption = caption_input.text.toString(),
        photo = mUser.photo
    )
}

data class FeedPost(
    val uid: String = "",
    val username: String = "",
    val photo: String? = null,
    val image: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val caption: String = "",
    val comments: List<Comment> = emptyList(),
    val timestamp:Any = ServerValue.TIMESTAMP
){
    fun timestampDate():Date = Date(timestamp as Long)
}


data class Comment(val uid:String,val username:String,val text:String)
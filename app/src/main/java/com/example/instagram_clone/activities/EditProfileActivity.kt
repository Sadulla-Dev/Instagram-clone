package com.example.instagram_clone.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagram_clone.R
import com.example.instagram_clone.models.User
import com.example.instagram_clone.ui.loadUserPhoto
import com.example.instagram_clone.ui.showToast
import com.example.instagram_clone.ui.toStringOrNull
import com.example.instagram_clone.utils.CameraHelper
import com.example.instagram_clone.utils.FirebaseHelper
import com.example.instagram_clone.views.PasswordDialog
import com.example.instagram_clone.utils.ValueEventListenerAdapter
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_edit_profile.*


class EditProfileActivity : AppCompatActivity(),PasswordDialog.Listener {
    private lateinit var mUsers: User
    private lateinit var mPadingUser: User
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCamera: CameraHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        mAuth = FirebaseAuth.getInstance()
        mFirebase = FirebaseHelper(this)

        mCamera = CameraHelper(this)

        back_image.setOnClickListener {
            mAuth.signOut()
        }
        mAuth.addAuthStateListener {
            if (it.currentUser == null){
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        change_photo_text.setOnClickListener { mCamera.takeCameraPicture()}
        save_image.setOnClickListener { updateProfile() }

        mFirebase.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUsers = it.getValue(User::class.java)!!
                name_input.setText(mUsers.name)
                username_input.setText(mUsers.username)
                email_input.setText(mUsers.email)
                website_input.setText(mUsers.website)
                bio_input.setText(mUsers.bio)
                phone_input.setText(mUsers.phone?.toString())
                profile_image.loadUserPhoto(mUsers.photo)
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCamera.REQUEST_CODE && resultCode == RESULT_OK) {
            mFirebase.uploadUserPhoto(mCamera.imageUri!!) {
                val photoUrl = it.downloadUrl.toString()
                mFirebase.updateUserPhoto(photoUrl) {
                    mUsers = mUsers.copy(photo = photoUrl)
                    profile_image.loadUserPhoto(mUsers.photo)
                    showToast("photo saved successfully")
                }
            }
        }
    }





    private fun updateProfile() {
        mPadingUser = readInput()
        val error = validate(mPadingUser)
        if (error == null) {
            if (mPadingUser.email == mUsers.email) {
                updateUser(mPadingUser)
            } else {
                PasswordDialog().show(supportFragmentManager,"passrod_dialog")
            }
        } else {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun readInput(): User {
        return User(
            name = name_input.text.toString(),
            username = username_input.text.toString(),
            email = email_input.text.toString(),
            bio = bio_input.text.toStringOrNull(),
            website = website_input.text.toStringOrNull(),
            phone = phone_input.text.toString().toLongOrNull()
        )
    }



    private fun updateUser(user: User) {
        val updatesMap = mutableMapOf<String, Any?>()
        if (user.name != mUsers.name) updatesMap["name"] = user.name
        if (user.email != mUsers.email) updatesMap["email"] = user.email
        if (user.username != mUsers.username) updatesMap["username"] = user.username
        if (user.website != mUsers.website) updatesMap["website"] = user.website
        if (user.bio != mUsers.bio) updatesMap["bio"] = user.bio
        if (user.phone != mUsers.phone) updatesMap["phone"] = user.phone
        mFirebase.updateUser(updatesMap){
            showToast("Profile saved")
            finish()
        }
    }

    private fun validate(user: User): String? = when {
        user.name.isEmpty() -> "Please enter name"
        user.username.isEmpty() -> "Please enter userName"
        user.email.isEmpty() -> "Please enter email"
        else -> null
    }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUsers.email, password)
            mFirebase.reauthenticate(credential) {
                mFirebase.updateEmail(mPadingUser.email) {
                    updateUser(mPadingUser)
                }
            }
        } else {
            showToast("You should enter your password!!")
        }
    }

}
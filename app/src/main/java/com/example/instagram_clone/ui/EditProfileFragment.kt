package com.example.instagram_clone.ui



import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.instagram_clone.R
import com.example.instagram_clone.activities.LoginActivity
import com.example.instagram_clone.models.User
import com.example.instagram_clone.utils.CameraHelper
import com.example.instagram_clone.utils.FirebaseHelper
import com.example.instagram_clone.utils.ValueEventListenerAdapter
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.password_dialog.view.*


class EditProfileFragment : Fragment() {
    private lateinit var mUsers: User
    private lateinit var mPadingUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var cameraHelper: CameraHelper
    private val TAG = "EditProfileActivity"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mFirebaseHelper = FirebaseHelper(requireActivity())
        cameraHelper = CameraHelper(requireActivity())

        back_image.setOnClickListener {
            mAuth.signOut()
        }
        mAuth.addAuthStateListener {
            if (it.currentUser == null){
                requireActivity().run{
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
        change_photo_text.setOnClickListener { cameraHelper.takeCameraPicture()}
        save_image.setOnClickListener {
            updateProfile()
            Navigation.findNavController(view)
                .navigate(R.id.action_editProfileFragment_to_profileFragment)
        }

        mFirebaseHelper.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUsers = it.getValue(User::class.java)!!
                name_input.setText(mUsers.name, TextView.BufferType.EDITABLE)
                username_input.setText(mUsers.username, TextView.BufferType.EDITABLE)
                email_input.setText(mUsers.email, TextView.BufferType.EDITABLE)
                website_input.setText(mUsers.website, TextView.BufferType.EDITABLE)
                bio_input.setText(mUsers.bio, TextView.BufferType.EDITABLE)
                phone_input.setText(mUsers.phone?.toString(), TextView.BufferType.EDITABLE)
                Glide.with(requireActivity()).load(mUsers.photo).fallback(R.drawable.person)
                    .into(profile_image)
            })
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == cameraHelper.REQUEST_CODE && resultCode == RESULT_OK) {
            mFirebaseHelper.uploadUserPhoto(cameraHelper.imageUri!!) {
                val photoUrl = it.downloadUrl.toString()
                mFirebaseHelper.updateUserPhoto(photoUrl) {
                    mUsers = mUsers.copy(photo = photoUrl)
                    profile_image.loadUserPhoto(mUsers.photo)
                    requireActivity().showToast("photo saved successfully")
                }
            }
        }
    }



    private fun addPasswordConfirm(password: String){
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUsers.email, password)
            mFirebaseHelper.reauthenticate(credential) {
                mFirebaseHelper.updateEmail(mPadingUser.email) {
                    updateUser(mPadingUser)
                }
            }
        } else {
            requireActivity().showToast("You should enter your password!!")
        }
    }

    private fun updateProfile() {
        mPadingUser = readInput()
        val error = validate(mPadingUser)
        if (error == null) {
            if (mPadingUser.email == mUsers.email) {
                updateUser(mPadingUser)
            } else {
                dialog()
            }
        } else {
            Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
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
        mFirebaseHelper.updateUser(updatesMap){
            requireActivity().showToast("Profile saved")
        }
    }


    private fun validate(user: User): String? = when {
            user.name.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter userName"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }

    private fun dialog() {
        val mDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.password_dialog,null)
        val builder = AlertDialog.Builder(requireActivity()).setView(mDialog).setTitle(R.string.please_enter_password)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newPassword:String = mDialog.password_input.text.toString()
                addPasswordConfirm(newPassword)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
        builder.show()
    }

}

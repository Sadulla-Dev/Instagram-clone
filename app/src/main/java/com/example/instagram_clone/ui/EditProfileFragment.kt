package com.example.instagram_clone.ui



import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.instagram_clone.R
import com.example.instagram_clone.models.Users
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.password_dialog.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : Fragment() {
    private lateinit var mUsers: Users
    private lateinit var mPadingUser: Users
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var mImageUri: Uri
    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private val TAG = "EditProfileActivity"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference
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
        change_photo_text.setOnClickListener { takeCameraPicture()}
        save_image.setOnClickListener {
            updateProfile()
            Navigation.findNavController(view)
                .navigate(R.id.action_editProfileFragment_to_profileFragment)
        }
        val user = mAuth.currentUser
        mDatabase.child("users").child(user!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUsers = it.getValue(Users::class.java)!!
                name_input.setText(mUsers.name, TextView.BufferType.EDITABLE)
                username_input.setText(mUsers.username, TextView.BufferType.EDITABLE)
                email_input.setText(mUsers.email, TextView.BufferType.EDITABLE)
                website_input.setText(mUsers.website, TextView.BufferType.EDITABLE)
                bio_input.setText(mUsers.bio, TextView.BufferType.EDITABLE)
                phone_input.setText(mUsers.phone?.toString(), TextView.BufferType.EDITABLE)
                Glide.with(requireActivity()).load(mUsers.photo).fallback(R.drawable.person).into(profile_image)
            })
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val uid = mAuth.currentUser!!.uid
            mStorage.uploadUserPhoto(uid, mImageUri) {
                val photoUrl = it.downloadUrl.toString()
                mDatabase.updateUserPhoto(uid, photoUrl) {
                    mUsers = mUsers.copy(photo = photoUrl)
                    Glide.with(requireActivity()).load(mUsers.photo).fallback(R.drawable.person)
                        .into(profile_image)
                    requireActivity().showToast("photo saved successfully")
                }
            }
        }
    }
    private fun StorageReference.uploadUserPhoto(uid: String,photo:Uri,onSuccess: (UploadTask.TaskSnapshot ) -> Unit){
        child("users/$uid/photo").putFile(mImageUri).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(it.result)
            } else {
                requireActivity().showToast(it.exception!!.message!!)
            }
        }
    }
    private fun DatabaseReference.updateUserPhoto(uid: String,photoUrl:String,onSuccess: () -> Unit){
        child("users/$uid/photo").setValue(photoUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    requireActivity().showToast(it.exception!!.message!!)
                }
            }
    }

    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity?.packageManager!!) != null) {
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(
                requireActivity(),
                "com.example.instagram_clone.fileprovider",
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            requireActivity().showToast("Try again")
        }

    }


    private fun createImageFile(): File {
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )

    }


    private fun addPasswordConfirm(password: String){
        if (password.isNotEmpty()){
            val credential = EmailAuthProvider.getCredential(mUsers.email,password)
            mAuth.currentUser!!.reauthenticate(credential) {
                mAuth.currentUser!!.updateEmail(mPadingUser.email) {
                    updateUser(mPadingUser)
                }
            }
        }else{
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

    private fun readInput(): Users {
        return Users(
            name = name_input.text.toString(),
            username = username_input.text.toString(),
            email = email_input.text.toString(),
            bio = bio_input.text.toStringOrNull(),
            website = website_input.text.toStringOrNull(),
            phone = phone_input.text.toString().toLongOrNull()
        )
    }



    private fun updateUser(user: Users) {
        val updatesMap = mutableMapOf<String, Any?>()
        if (user.name != mUsers.name) updatesMap["name"] = user.name
        if (user.email != mUsers.email) updatesMap["email"] = user.email
        if (user.username != mUsers.username) updatesMap["username"] = user.username
        if (user.website != mUsers.website) updatesMap["website"] = user.website
        if (user.bio != mUsers.bio) updatesMap["bio"] = user.bio
        if (user.phone != mUsers.phone) updatesMap["phone"] = user.phone
        mDatabase.updateUser(mAuth.currentUser!!.uid,updatesMap){
            requireActivity().showToast("Profile saved")
        }
    }

    private fun DatabaseReference.updateUser(uid:String,updates:Map<String,Any?>,onSuccess: () -> Unit){
        child("users").child(uid).updateChildren(updates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    requireActivity().showToast(it.exception!!.message!!)
                }
            }
    }

    private fun validate(user: Users): String? = when {
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

    private fun FirebaseUser.updateEmail(email:String, onSuccess: () -> Unit){
        updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful){
                onSuccess()
            }else{
                requireActivity().showToast(it.exception!!.message!!)
            }
        }
    }

    private fun FirebaseUser.reauthenticate(credential: AuthCredential, onSuccess: () -> Unit){
        reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful){
                onSuccess()
            }else{
                requireActivity().showToast(it.exception!!.message!!)
            }
        }
    }

}

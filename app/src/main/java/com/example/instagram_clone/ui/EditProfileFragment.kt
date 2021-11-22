package com.example.instagram_clone.ui



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
import androidx.navigation.fragment.findNavController
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
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.password_dialog.view.*


class EditProfileFragment : Fragment() {
    private lateinit var mUsers: Users
    private lateinit var mPadingUser: Users
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private val TAG = "EditProfileActivity"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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



        save_image.setOnClickListener {
            updateProfile()
            Navigation.findNavController(view)
                .navigate(R.id.action_editProfileFragment_to_profileFragment)
        }

        val user = mAuth.currentUser
        mDatabase.child("users").child(user!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUsers = it.getValue(Users::class.java)!!
                name_input.setText(mUsers!!.name, TextView.BufferType.EDITABLE)
                username_input.setText(mUsers!!.username, TextView.BufferType.EDITABLE)
                email_input.setText(mUsers!!.email, TextView.BufferType.EDITABLE)
                website_input.setText(mUsers!!.website, TextView.BufferType.EDITABLE)
                bio_input.setText(mUsers!!.bio, TextView.BufferType.EDITABLE)
                phone_input.setText(mUsers!!.phone.toString(), TextView.BufferType.EDITABLE)
            })
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
            website = website_input.text.toString(),
            bio = bio_input.text.toString(),
            email = email_input.text.toString(),
            phone = phone_input.text.toString().toLong()
        )
    }




    private fun updateUser(user: Users) {
        val updatesMap = mutableMapOf<String, Any>()
        if (user.name != mUsers.name) updatesMap["name"] = user.name
        if (user.email != mUsers.email) updatesMap["email"] = user.email
        if (user.username != mUsers.username) updatesMap["username"] = user.username
        if (user.website != mUsers.website) updatesMap["website"] = user.website!!
        if (user.bio != mUsers.bio) updatesMap["bio"] = user.bio!!
        if (user.phone != mUsers.phone) updatesMap["phone"] = user.phone!!

        mDatabase.updateUser(mAuth.currentUser!!.uid,updatesMap){
            requireActivity().showToast("Profile saved")
        }
    }

    private fun DatabaseReference.updateUser(uid:String,updates:Map<String,Any>,onSuccess: () -> Unit){
        child("users").child(mAuth.currentUser!!.uid).updateChildren(updates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    requireActivity().showToast(it.exception!!.message!!)
                }
            }
    }

    private fun validate(user: Users): String? =
        when {
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

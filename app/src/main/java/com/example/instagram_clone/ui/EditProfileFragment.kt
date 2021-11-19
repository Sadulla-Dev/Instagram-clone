package com.example.instagram_clone.ui



import android.app.AlertDialog
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.password_dialog.view.*


class EditProfileFragment : Fragment() {
    private lateinit var mUsers: Users
    private lateinit var mPadingUser: Users
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
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
        back_image.setOnClickListener { Navigation.findNavController(view).navigate(R.id.action_editProfileFragment_to_profileFragment) }


        save_image.setOnClickListener {
            updateProfile()
            Navigation.findNavController(view).navigate(R.id.action_editProfileFragment_to_profileFragment)
        }
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(user!!.uid).addListenerForSingleValueEvent(ValueEventListenerAdapter{
            mUsers = it.getValue(Users::class.java)!!
            name_input.setText(mUsers!!.name, TextView.BufferType.EDITABLE)
            username_input.setText(mUsers!!.username, TextView.BufferType.EDITABLE)
            email_input.setText(mUsers!!.email, TextView.BufferType.EDITABLE)
            website_input.setText(mUsers!!.website, TextView.BufferType.EDITABLE)
            bio_input.setText(mUsers!!.bio, TextView.BufferType.EDITABLE)
            phone_input.setText(mUsers!!.phone.toString(), TextView.BufferType.EDITABLE)
        })
    }

    private fun updateProfile() {
        mPadingUser = readInput()
        val error = validate(mPadingUser)
        if (error == null){
            if (mPadingUser.email == mUsers.email){
                updateUser(mPadingUser)
            }else{
                dialog()
            }
        }else{
            Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun readInput(): Users {
        val phoneStr = phone_input.text.toString()
        return  Users(
            name = name_input.text.toString(),
            username = username_input.text.toString(),
            website = website_input.text.toString(),
            bio = bio_input.text.toString(),
            email = email_input.text.toString(),
            phone = if (phoneStr.isEmpty()) 0 else phoneStr.toLong()
        )
    }


    private fun dialog() {
        val mDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.password_dialog,null)
        val builder = AlertDialog.Builder(requireActivity()).setView(mDialog).setTitle(R.string.please_enter_password)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newPassword:String = mDialog.password_input.text.toString()
                if (newPassword.isNotEmpty()){
                    val credential = EmailAuthProvider.getCredential(mUsers.email,newPassword)
                    mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
                        if (it.isSuccessful){
                            mAuth.currentUser!!.updateEmail(mPadingUser.email).addOnCompleteListener {
                                if (it.isSuccessful){
                                    updateUser(mPadingUser)
                                }else{
                                    requireActivity().showToast(it.exception!!.message!!)
                                }
                            }
                        }else{
                            requireActivity().showToast(it.exception!!.message!!)
                        }
                    }
                }else{
                    requireActivity().showToast("You should enter your password!!")
                }

            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
        builder.show()
    }


    private fun updateUser(user: Users) {
        val updatesMap = mutableMapOf<String,Any>()
        if (user.name != mUsers.name) updatesMap["name"] = user.name
        if (user.email != mUsers.email) updatesMap["email"] = user.email
        if (user.username != mUsers.username) updatesMap["username"] = user.username
        if (user.website != mUsers.website) updatesMap["website"] = user.website!!
        if (user.bio != mUsers.bio) updatesMap["bio"] = user.bio!!
        if (user.phone != mUsers.phone) updatesMap["phone"] = user.phone!!
        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updatesMap).addOnCompleteListener {
            if (it.isSuccessful){
                requireActivity().showToast("Profile saved")
            }else{
                requireActivity().showToast(it.exception!!.message!!)
            }
        }
    }

    private fun validate(user: Users): String? =
        when{
            user.name.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter userName"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }


}

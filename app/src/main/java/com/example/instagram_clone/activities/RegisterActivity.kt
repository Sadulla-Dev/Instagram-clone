package com.example.instagram_clone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.instagram_clone.R
import com.example.instagram_clone.models.User
import com.example.instagram_clone.ui.coordinateBtnAndInputs
import com.example.instagram_clone.ui.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_email.email_input
import kotlinx.android.synthetic.main.fragment_register_namepass.*
import kotlinx.android.synthetic.main.fragment_register_namepass.password_input

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, NamePassFragment.Listener {
    private val TAG = "RegisterActivity"
    private var mEmail:String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.framLayout, EmailFragment()).commit()
        }
    }

    override fun onNext(email: String) {
        if(email.isNotEmpty()){
            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener {
                if (it.isSuccessful){
                    if (it.result?.signInMethods?.isEmpty() != false){
                        supportFragmentManager.beginTransaction().replace(R.id.framLayout, NamePassFragment())
                            .addToBackStack(null).commit()
                    }else{
                        showToast("This email already exits")
                    }
                }else{
                    showToast(it.exception!!.message!!)
                }
            }

        }else{
            showToast("Please enter email")
        }
    }

    override fun onRegister(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty()){
            val email = mEmail
            if (email!= null) {
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                    if (it.isSuccessful){
                        val user = mkUser(fullName, email)
                        val reference = mDatabase.child("users").child(it.result!!.user!!.uid)
                        reference.setValue(user).addOnCompleteListener{
                            if (it.isSuccessful){
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }else{
                                Log.e(TAG, "failed to create user profile", it.exception)
                                showToast("Somthing wrong happend")
                            }
                        }
                    }else{
                        Log.e(TAG, "failed to create user ", it.exception)
                        showToast("Somthing wrong happend")
                    }
                }
            }else{
                Log.e(TAG, "onRegister: email is null")
                showToast("Please enter email")
                supportFragmentManager.popBackStack()
            }
        }else{
            showToast("Please enter full name and password")
        }
    }

    private fun mkUserName(fullName: String):String = fullName.toLowerCase().replace(" ",".")

    private fun mkUser(fullName: String,email: String):User{
        val userName = mkUserName(fullName)
        return User(name = fullName,username = userName,email = email)
    }

}

class EmailFragment: Fragment(){
    private lateinit var mListener: Listener
    interface Listener {
        fun onNext(email:String)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_email,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        next_btn.setOnClickListener {
            val email = email_input.text.toString()
            mListener.onNext(email)
        }

        coordinateBtnAndInputs(next_btn,email_input)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

class NamePassFragment: Fragment() {
    private lateinit var mListener: Listener
    interface Listener{
        fun onRegister(fullName: String,password:String)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_namepass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        register_btn.setOnClickListener {
            val fullName = full_name_input.text.toString()
            val password = password_input.text.toString()
            mListener.onRegister(fullName,password)
        }
        coordinateBtnAndInputs(register_btn,full_name_input,password_input)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

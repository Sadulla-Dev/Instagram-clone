package com.example.instagram_clone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import com.example.instagram_clone.R


class LoginActivity : AppCompatActivity(), KeyboardVisibilityEventListener, View.OnClickListener {
    private lateinit var mFirebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        KeyboardVisibilityEvent.registerEventListener(this,this)
        coordinateBtnAndInputs(login_btn,email_input,password_input)
        login_btn.setOnClickListener(this)
        mFirebaseAuth = FirebaseAuth.getInstance()
        create_account_text.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.login_btn -> {
                val email = email_input.text.toString().trim()
                val password = password_input.text.toString().trim()
                if (validate(email,password)){
                    mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                        if (it.isSuccessful){
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                }else{
                    showToast("Place enter email and password")
                }
            }
            R.id.create_account_text -> {
                startActivity(Intent(this,RegisterActivity::class.java))
                finish()
            }
        }

    }

    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen){
            create_account_text.visibility = View.GONE
        }else{
            create_account_text.visibility = View.VISIBLE
        }
    }

    private fun validate(email: String,password:String) = email.isNotEmpty() && password.isNotEmpty()

}



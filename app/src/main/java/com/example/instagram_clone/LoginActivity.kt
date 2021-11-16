package com.example.instagram_clone

import android.content.Intent
import android.os.Build
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
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy


class LoginActivity : AppCompatActivity(), KeyboardVisibilityEventListener, TextWatcher, View.OnClickListener {
    private lateinit var mFirebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        KeyboardVisibilityEvent.registerEventListener(this,this)
        login_btn.isEnabled = false
        email_input.addTextChangedListener(this)
        password_input.addTextChangedListener(this)
        login_btn.setOnClickListener(this)
        mFirebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(view: View) {
        val email = email_input.text.toString().trim()
        val password = password_input.text.toString().trim()
        if (validate(email,password)){
            mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                if (it.isSuccessful){
                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
                }
            }
        }else{
            Toast.makeText(this, "enter sssss", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen){
            scroll_view.scrollTo(0,scroll_view.bottom)
            create_account_text.visibility = View.GONE
        }else{
            scroll_view.scrollTo(0,scroll_view.top)
            create_account_text.visibility = View.VISIBLE
        }
    }
    private fun validate(email: String,password:String) = email.isNotEmpty() && password.isNotEmpty()

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(p0: Editable?) {
        login_btn.isEnabled = validate(email_input.text.toString(),password_input.text.toString())

    }


}



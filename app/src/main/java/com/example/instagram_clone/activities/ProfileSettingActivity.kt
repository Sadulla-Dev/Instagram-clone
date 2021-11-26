package com.example.instagram_clone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.instagram_clone.R
import com.example.instagram_clone.utils.FirebaseHelper
import kotlinx.android.synthetic.main.activity_profile_setting.*

class ProfileSettingActivity : AppCompatActivity() {
    private lateinit var mFirbase:FirebaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)
        mFirbase = FirebaseHelper(this)
        sign_out_text.setOnClickListener { mFirbase.auth.signOut() }
        back_image.setOnClickListener { finish()  }
    }
}
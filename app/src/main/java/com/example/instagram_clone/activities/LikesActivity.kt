package com.example.instagram_clone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.instagram_clone.R

class LikesActivity : BaseActivity(3) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        setupBottomNavigation()
    }
}
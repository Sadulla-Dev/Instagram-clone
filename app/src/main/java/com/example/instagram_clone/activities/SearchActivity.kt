package com.example.instagram_clone.activities


import android.os.Bundle
import com.example.instagram_clone.R

class SearchActivity : BaseActivity(1) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setupBottomNavigation()
    }
}
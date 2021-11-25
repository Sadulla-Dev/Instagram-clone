package com.example.instagram_clone.activities

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import com.example.instagram_clone.R
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.bottom_navigation_view.*

open class BaseActivity (val navNumber: Int) : AppCompatActivity() {
    val TAG = "BaseActivity"

    fun setupBottomNavigation() {
        bottom_navigation.setOnNavigationItemSelectedListener {
            val nextActivity =
                when (it.itemId) {
                    R.id.homeFragment -> MainActivity::class.java
                    R.id.searchFragment -> SearchActivity::class.java
                    R.id.shareFragment -> ShareActivity::class.java
                    R.id.likesFragment -> LikesActivity::class.java
                    R.id.profileFragment -> ProfileActivity::class.java
                    else -> {
                        Log.e(ContentValues.TAG, "unknown nav item clicked $it")
                        null
                    }
                }
            if (nextActivity != null) {
                val intent = Intent(this, nextActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(0, 0)
                true
            } else {
                false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (bottom_navigation != null){
            bottom_navigation.menu.getItem(navNumber).isChecked = false
        }
    }
}
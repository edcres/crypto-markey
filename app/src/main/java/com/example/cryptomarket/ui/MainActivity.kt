package com.example.cryptomarket.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cryptomarket.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/** todo:
 * - bottom navigation bug
 *      - the first icon is chosen but the coins fragment is up
 * - animations for transitions between fragments (left: from right, middle: zoom in, zoom out, right: from left)
 *
 * - test the bottom sheet
 *
 * - data/backend stuff
 *      - Data class for the API
 * - display the data in the view as text
 *      - recyclerView for coins list
 *      - recyclerView for news list
 *      - overview data
 * - appbar
 *
 * AFTER:
 * - put data in charts
 * - customize the charts
 * - colors
 * - app icon
 */

/** maybe:
 * - news section
 * - local database
 */

/**
 * check todos:
 *
 */

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }
    }
}
package com.example.cryptomarket.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.cryptomarket.R

/** todo:
 *
 * - have a fragment for coins list and one for market data
 * - configure the view to display the data (in txt views at first)
 *      - recyclerView for coins list
 * - make bottom navigation
 *
 * - data/backend stuff
 *      - Data class for the API
 * - display the data in the view as text
 *      - recyclerView for coins list
 *      -
 * - appbar
 *
 * AFTER:
 * - put data in charts
 * - customize the charts
 */

/** maybe:
 * - news section
 * - local database
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
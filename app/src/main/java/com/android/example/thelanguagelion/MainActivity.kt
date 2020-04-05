package com.android.example.thelanguagelion

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar?.hide()

        // Initialize navigation
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Set up the bottom navigation bar
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_notebook, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Don't display the navigation bar during a lesson or score screen
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if(nd.id == R.id.navigation_lesson || nd.id == R.id.navigation_score) {
                navView.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
            }
        }
    }
}

package com.example.runnertrackingapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.databinding.ActivityMainBinding
import com.example.runnertrackingapp.utils.Utils
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //if the app is launched by clicking on notification then intent must have an action
        navigateToTrackingFragment(intent)

        /* if bottom_navigation_view.setupWithNavController(fragment.findNavController()) is not working then use this
        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.activity_main_fcv) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)*/
        bottom_navigation_view.setupWithNavController(fragment.findNavController())
        bottom_navigation_view.setOnItemReselectedListener {  }
        fragment.findNavController()
            .addOnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment -> bottom_navigation_view.visibility =
                        View.VISIBLE
                    else -> bottom_navigation_view.visibility = View.GONE
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //this is if the app is opened and user clicked on notification then tracking fragment will open
        navigateToTrackingFragment(intent)
    }

    private fun navigateToTrackingFragment(intent: Intent?) {
        //checking action and doing
        if (intent?.action == Utils.ACTION_SHOW_TRACKING_FRAGMENT) {
            fragment.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }
}

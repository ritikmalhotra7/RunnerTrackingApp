package com.example.runnertrackingapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.databinding.ActivityMainBinding
import com.example.runnertrackingapp.utils.Utils
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

        bottom_navigation_view.setupWithNavController(fragment.findNavController())
        fragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
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
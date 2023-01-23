package com.example.runnertrackingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.db.models.Run
import com.example.runnertrackingapp.services.Polyline
import com.example.runnertrackingapp.services.TrackingService
import com.example.runnertrackingapp.ui.viewModels.MainViewModel
import com.example.runnertrackingapp.utils.Utils
import com.example.runnertrackingapp.utils.Utils.ACTION_PAUSE_SERVICE
import com.example.runnertrackingapp.utils.Utils.ACTION_START_OR_RESUME_SERVICE
import com.example.runnertrackingapp.utils.Utils.ACTION_STOP_SERVICE
import com.example.runnertrackingapp.utils.Utils.MAP_ZOOM
import com.example.runnertrackingapp.utils.Utils.POLYLINE_COLOR
import com.example.runnertrackingapp.utils.Utils.POLYLINE_WIDTH
import com.example.runnertrackingapp.utils.Utils.calculatePolylineDistance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.round


const val CANCEL_DIALOG_TRACKING_TAG = "CANCEL_DIALOG_TRACKING_TAG"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var menu: Menu? = null
    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.onCreate(savedInstanceState)
        mapView.getMapAsync {
            map = it
            addAllPolyline()
        }
        savedInstanceState?.let{
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(CANCEL_DIALOG_TRACKING_TAG) as DialogFragment
            cancelTrackingDialog.setYesListener(::stopRun)
        }
        setMenuOptions()
        setupViews()
        subscribeToObservers()
    }

    private fun setupViews() {
        menu?.get(0)?.isVisible = false
        btnToggleRun.setOnClickListener { toggleRun() }
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
    }

    private fun setMenuOptions() {
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_tracking_menu, menu)
                this@TrackingFragment.menu = menu
                this@TrackingFragment.menu?.get(0)?.isVisible = false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.miCancelRun -> showCancelRunDialog()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showCancelRunDialog() {
        DialogFragment().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager,CANCEL_DIALOG_TRACKING_TAG)
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCamera()
        }
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            val formattedTime = Utils.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            if(curTimeInMillis == 0L){
                btnToggleRun.isEnabled = false
                CoroutineScope(Dispatchers.Main).launch {
                    delay(10000L)
                    btnToggleRun.isEnabled = true
                    menu?.get(0)?.isVisible = true
                }
            }
        }
    }

    //updating views
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            btnToggleRun.text = "start"
            btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking) {
            btnToggleRun.text = "stop"
            btnFinishRun.visibility = View.GONE
            if(curTimeInMillis>10*1000){
                menu?.get(0)?.isVisible = true
            }
        }
    }

    //updating camera
    private fun moveCamera() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), MAP_ZOOM)
            )
        }
    }

    //function to start service
    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    //if the view is recreated
    private fun addAllPolyline() {
        pathPoints.forEach { polyline ->
            val polylineOptions = PolylineOptions().apply {
                color(POLYLINE_COLOR)
                width(POLYLINE_WIDTH)
                addAll(polyline)
            }
            map?.addPolyline(polylineOptions)
        }
    }

    //whenever pathPoints is updated
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions().apply {
                color(POLYLINE_COLOR)
                width(POLYLINE_WIDTH)
                add(preLastLatLng)
                add(lastLatLng)
            }
            map?.addPolyline(polylineOptions)

        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
                Log.d("taget","${pos.latitude} ${pos.longitude}")
            }
        }
        bounds.let{
            map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    it.build(),
                    mapView.width,
                    mapView.height,
                    (mapView.height * 0.05).toInt()
                )
            )
        }
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceinMeters = 0
            pathPoints.forEach { polyline ->
                distanceinMeters += calculatePolylineDistance(polyline).toInt()
            }
            val avgSpeed =
                round((distanceinMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceinMeters / 1000f) * weight).toInt()
            val run =
                Run(bmp, dateTimeStamp, avgSpeed, distanceinMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.root_view),
                "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
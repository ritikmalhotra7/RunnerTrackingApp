package com.example.runnertrackingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.services.Polyline
import com.example.runnertrackingapp.services.TrackingService
import com.example.runnertrackingapp.ui.viewModels.MainViewModel
import com.example.runnertrackingapp.utils.Utils.ACTION_PAUSE_SERVICE
import com.example.runnertrackingapp.utils.Utils.ACTION_START_OR_RESUME_SERVICE
import com.example.runnertrackingapp.utils.Utils.MAP_ZOOM
import com.example.runnertrackingapp.utils.Utils.POLYLINE_COLOR
import com.example.runnertrackingapp.utils.Utils.POLYLINE_WIDTH
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var map :GoogleMap? = null
    private var isTracking= false
    private var pathPoints = mutableListOf<Polyline>()

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
        btnToggleRun.setOnClickListener {
            //triggering service
            toggleRun()
        }
        subscribeToObservers()
    }
    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner){
            updateTracking(it)
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner){
            pathPoints = it
            addLatestPolyline()
            moveCamera()
        }
    }
    private fun toggleRun(){
        if(isTracking){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun updateTracking(isTracking:Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            btnToggleRun.text = "start"
            btnFinishRun.visibility = View.VISIBLE
        }else{
            btnToggleRun.text = "stop"
            btnFinishRun.visibility = View.GONE
        }
    }
    private fun moveCamera(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), MAP_ZOOM)
            )
        }
    }
    private fun sendCommandToService(action: String){
        //giving commands to start service
        Intent(requireContext(),TrackingService::class.java).also{
            it.action = action
            requireContext().startService(it)
        }
    }
    private fun addAllPolyline(){
        pathPoints.forEach { polyline ->
            val polylineOptions = PolylineOptions().apply {
                color(POLYLINE_COLOR)
                width(POLYLINE_WIDTH)
                addAll(polyline)
            }
            map?.addPolyline(polylineOptions)
        }
    }
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size>1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size-2]
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
    private fun drawPolyline(){

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
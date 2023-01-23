package com.example.runnertrackingapp.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.adapter.RunAdapter
import com.example.runnertrackingapp.ui.viewModels.MainViewModel
import com.example.runnertrackingapp.utils.SortType
import com.example.runnertrackingapp.utils.Utils.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runnertrackingapp.utils.Utils.hasLocationPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run),EasyPermissions.PermissionCallbacks {

    private lateinit var spinnerSelection: String
    private val viewModel: MainViewModel by viewModels()
    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_run, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
        requestPermissions()
        getData()
        setViews()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun getData() {
        when(viewModel.sortType){
            SortType.DATE -> spFilter.setSelection(0)
            SortType.DISTANCE -> spFilter.setSelection(1)
            SortType.AVG_SPEED -> spFilter.setSelection(2)
            SortType.CALORIES_BURNED -> spFilter.setSelection(3)
            SortType.RUNNING_TIME -> spFilter.setSelection(4)
        }
        viewModel.runs.observe(viewLifecycleOwner){
            runAdapter.setList(it)
        }
    }

    private fun setViews() {
        //spinner
        spFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                when(pos){
                    0-> viewModel.sortRuns(SortType.DATE)
                    1->viewModel.sortRuns(SortType.DISTANCE)
                    2->viewModel.sortRuns(SortType.AVG_SPEED)
                    3->viewModel.sortRuns(SortType.CALORIES_BURNED)
                    4->viewModel.sortRuns(SortType.RUNNING_TIME)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        //recyclerview
        runAdapter = RunAdapter()
        rvRuns.apply {
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun requestPermissions() {
        if (hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}
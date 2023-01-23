package com.example.runnertrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.ui.viewModels.MainViewModel
import com.example.runnertrackingapp.utils.Utils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject lateinit var sharedPref:SharedPreferences
    @set:Inject
    var isFirstTime = true
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_setup, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(savedInstanceState)
    }

    private fun setViews(savedInstanceState:Bundle?) {
        if(!isFirstTime){
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.setupFragment,true).build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment,savedInstanceState, navOptions)
        }
        tvContinue.setOnClickListener {
            val success = writePersonalData()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(
                    requireView(),
                    "Please enter name and weight to continue",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writePersonalData():Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()) return false
        sharedPref.edit().apply{
            putString(Utils.USERNAME_KEY,name)
            putFloat(Utils.WEIGHT_KEY,weight.toFloat())
            putBoolean(Utils.FIRST_TIME_TOGGLE_KEY,false)
            apply()
        }
        val toolbarText = "Let's go, $name"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }

}
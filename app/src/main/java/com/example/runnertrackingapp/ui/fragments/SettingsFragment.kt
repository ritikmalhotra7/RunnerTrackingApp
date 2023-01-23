package com.example.runnertrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.ui.viewModels.MainViewModel
import com.example.runnertrackingapp.utils.Utils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: MainViewModel by viewModels()
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {
        loadFields()
        btnApplyChanges.setOnClickListener {
            if(applyChangesToSharedPrefs()){
                Snackbar.make(
                    requireView(),
                    "Applied changes",
                    Snackbar.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_settingsFragment_to_runFragment)
            }else{
                Snackbar.make(
                    requireView(),
                    "Please enter name and weight to continue",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun loadFields(){
        val name = sharedPrefs.getString(Utils.USERNAME_KEY,"")
        val weight = sharedPrefs.getFloat(Utils.WEIGHT_KEY,80f)
        etName.setText(name)
        etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPrefs():Boolean {
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPrefs.edit().apply{
            putString(Utils.USERNAME_KEY,name)
            putFloat(Utils.WEIGHT_KEY,weight.toFloat())
            apply()
        }
        val toolbarText = "Let's go, $name"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}
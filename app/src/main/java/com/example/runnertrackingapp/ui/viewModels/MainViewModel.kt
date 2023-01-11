package com.example.runnertrackingapp.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.runnertrackingapp.repository.DefaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepo: DefaultRepository) : ViewModel() {

}
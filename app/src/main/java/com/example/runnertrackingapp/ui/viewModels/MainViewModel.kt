package com.example.runnertrackingapp.ui.viewModels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnertrackingapp.db.models.Run
import com.example.runnertrackingapp.db.repository.DefaultRepository
import com.example.runnertrackingapp.utils.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepository: DefaultRepository) :
    ViewModel() {

    val runs = MediatorLiveData<List<Run>>()
    val sortType = SortType.DATE

    val getAllRunsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val getAllRunsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val getAllRunsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val getAllRunsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val getAllRunsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    init {
        runs.addSource(getAllRunsSortedByDate) { result ->
            if (sortType == SortType.DATE) result?.let {
                runs.value = it
            }
        }
        runs.addSource(getAllRunsSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) result?.let {
                runs.value = it
            }
        }
        runs.addSource(getAllRunsSortedByAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED) result?.let {
                runs.value = it
            }
        }
        runs.addSource(getAllRunsSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALORIES_BURNED) result?.let {
                runs.value = it
            }
        }
        runs.addSource(getAllRunsSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNING_TIME) result?.let {
                runs.value = it
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> getAllRunsSortedByDate.value?.let { runs.value = it }
        SortType.DISTANCE -> getAllRunsSortedByDistance.value?.let { runs.value = it }
        SortType.AVG_SPEED -> getAllRunsSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> getAllRunsSortedByCaloriesBurned.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> getAllRunsSortedByTimeInMillis.value?.let { runs.value = it }
    }

    fun insertRun(run: Run) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.insertRun(run)
    }
}
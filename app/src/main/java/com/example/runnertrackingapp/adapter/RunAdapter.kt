package com.example.runnertrackingapp.adapter

import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.runnertrackingapp.databinding.ItemRunBinding
import com.example.runnertrackingapp.db.models.Run
import com.example.runnertrackingapp.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter() : RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.N)
        fun setData(position: Int, item: Run) {
            binding.apply {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = item.timestamp
                }
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

                ivRunImage.load(item.img)
                tvDate.text = dateFormat.format(calendar.time)
                tvAvgSpeed.text = "${item.avgSpeedKMH} km/h"
                tvDistance.text = "${item.distanceInMeter / 1000f} km"
                tvTime.text = Utils.getFormattedStopWatchTime(item.timeInMillis)
                tvCalories.text = "${item.caloriesBurned} kCal"

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.setData(position, item)
    }

    override fun getItemCount(): Int = differ.currentList.size

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Run, newItem: Run) = oldItem.hashCode() == newItem.hashCode()
    }
    val differ = AsyncListDiffer(this, diffCallback)
    fun setList(list:List<Run>){
        differ.submitList(list)
        this.notifyDataSetChanged()
    }
}
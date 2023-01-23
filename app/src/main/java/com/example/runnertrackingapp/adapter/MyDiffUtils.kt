package com.example.runnertrackingapp.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.runnertrackingapp.db.models.Run

class MyDiffUtils(private val oldList:List<Run>,private val newList:List<Run>):DiffUtil.Callback() {
    override fun getOldListSize(): Int {return oldList.size}
    override fun getNewListSize(): Int {return newList.size}

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].id == newList[newItemPosition].id
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }

}
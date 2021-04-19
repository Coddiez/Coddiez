package com.example.coddiez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coddiez.models.contestTimings

class contest_list_adapter
(private val localDataSet: Array<contestTimings>) : RecyclerView.Adapter<contest_list_adapter.ViewHolder>() {




    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameOfContest :TextView = view.findViewById(R.id.contestName)
        val contestStartingTime :TextView = view.findViewById(R.id.startTime)
        val contestEndingTime :TextView = view.findViewById(R.id.endTime)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.contest_data_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameOfContest.text = localDataSet[position].contestName
        holder.contestStartingTime.text = localDataSet[position].startTime
        holder.contestEndingTime.text = localDataSet[position].endTime
    }

    override fun getItemCount(): Int {
        return localDataSet.size
    }

}
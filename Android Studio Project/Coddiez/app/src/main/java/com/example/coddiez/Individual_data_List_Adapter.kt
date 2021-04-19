package com.example.coddiez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Individual_data_List_Adapter(private val localDataSet: Array<Pair<String, Any>>) :
    RecyclerView.Adapter<Individual_data_List_Adapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var propertyName: TextView = view.findViewById(R.id.rankingDataName)
        var propertyValue: TextView = view.findViewById(R.id.rankingDataValue)
        var dots : TextView =view.findViewById(R.id.dotsText)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.individual_datalist_layout, viewGroup, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return localDataSet.size
    }

    override fun onBindViewHolder(holder: Individual_data_List_Adapter.ViewHolder, position: Int) {
        holder.propertyName.text = localDataSet[position].first.capitalize()
        holder.propertyValue.text = localDataSet[position].second.toString()
        if (localDataSet[position].second.toString() == "") {
         holder.dots.visibility= View.GONE
        }
    }
}
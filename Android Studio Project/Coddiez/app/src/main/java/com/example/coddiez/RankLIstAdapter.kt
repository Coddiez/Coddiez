package com.example.coddiez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coddiez.models.RandomColors
import com.example.coddiez.models.Users
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RankLIstAdapter(options: FirestoreRecyclerOptions<Users>, platformI: Int) :
    FirestoreRecyclerAdapter<Users, RankLIstAdapter.ListViewHolder>(
        options
    ) {

    val randomColorsObj = RandomColors()
    var platformIndex: Int = platformI

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameOfPerson: TextView = itemView.findViewById(R.id.personName)
        val score: TextView = itemView.findViewById(R.id.scoreShowingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {


        return ListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.leaderbord_list_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int, model: Users) {
        holder.nameOfPerson.text = model.displayName

        holder.nameOfPerson.setTextColor(randomColorsObj.getRnd())
        holder.score.text = when (platformIndex) {

            0 -> {
                model.rankData.codechefData.toString()
            }
            1 -> {
                model.rankData.codeforcesData.toString()
            }
            2 -> {
                model.rankData.spjData.toString()
            }
            3 -> {
                model.rankData.interviewbitData.toString()
            }
            4 -> {
                model.rankData.leetcodeData.toString()
            }
            else -> model.rankData.codechefData.toString()
        }
    }
}



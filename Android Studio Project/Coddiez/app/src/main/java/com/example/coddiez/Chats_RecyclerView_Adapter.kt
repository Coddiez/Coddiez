package com.example.coddiez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.coddiez.models.ChatModel
import com.example.coddiez.models.GetTimeFromTS
import com.example.coddiez.models.RandomColors
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ChatsRecyclerViewAdapter(options: FirestoreRecyclerOptions<ChatModel>) :
    FirestoreRecyclerAdapter<ChatModel, ChatsRecyclerViewAdapter.ListViewHolder>(
        options
    ) {

    private val getTimeFromTSObj = GetTimeFromTS()
    private val randomColorObj = RandomColors()
    private var prevID: String = ""

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.chatBox)
        val message: TextView = itemView.findViewById(R.id.msgTextView)
        val sender: TextView = itemView.findViewById(R.id.senderName)
        val timeStamp: TextView = itemView.findViewById(R.id.sendingTime)
        val containerS: ConstraintLayout = itemView.findViewById(R.id.chatBoxS)
        val messageS: TextView = itemView.findViewById(R.id.msgTextViewS)
        val timeStampS: TextView = itemView.findViewById(R.id.sendingTimeS)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.msg_recyclerview_layout, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ListViewHolder,
        position: Int,
        model: ChatModel
    ) {
        if (model.senderID == Firebase.auth.currentUser!!.uid) {
            holder.messageS.text = model.message
            holder.timeStampS.text = getTimeFromTSObj.getTimeFromTS(model.sendingTimestamp)
            holder.container.visibility = View.GONE
            holder.containerS.visibility = View.VISIBLE
        } else {
            if ( prevID == model.senderID) {
                holder.sender.visibility = View.GONE
            } else {
                holder.sender.setTextColor(randomColorObj.getRnd())
                holder.sender.text = model.senderName
            }

            holder.message.text = model.message
            holder.timeStamp.text = getTimeFromTSObj.getTimeFromTS(model.sendingTimestamp)
        }
        prevID = model.senderID

    }

}




package com.example.coddiez.daos

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.coddiez.ChatsRecyclerViewAdapter
import com.example.coddiez.models.ChatModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatDao {
     var groupID: String = ""
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private val currentUserID = auth.currentUser!!.uid
    var userName: String = ""
    private var collectionReference: CollectionReference? = null

    fun sendMsg(msgTetx: String) {
        GlobalScope.launch(Dispatchers.IO) {

            if (groupID == "") {
                groupID =
                    db.collection("Users").document(currentUserID).get().await().get("groupID")
                        .toString()
                userName = db.collection("Groups").document(groupID).collection("members")
                    .document(currentUserID).get().await().get("displayName").toString()
            }

            if (collectionReference == null) {
                collectionReference = db.collection("Groups").document(groupID)
                    .collection("Chats")
            }

            collectionReference!!.document()
                .set(ChatModel(msgTetx, currentUserID, userName, System.currentTimeMillis()))

        }
    }

    fun getUsernameAndGroupID() {
        GlobalScope.launch(Dispatchers.IO) {

            groupID =
                db.collection("Users").document(currentUserID).get().await().get("groupID")
                    .toString()
            userName = db.collection("Groups").document(groupID).collection("members")
                .document(currentUserID).get().await().get("displayName").toString()


        }
    }

    fun setUpChatRecyclerView(chatsRecyclerView: RecyclerView) {
        GlobalScope.launch(Dispatchers.IO) {

            if (groupID == "") {

                groupID =
                    db.collection("Users").document(currentUserID).get().await().get("groupID")
                        .toString()

                userName = db.collection("Groups").document(groupID).collection("members")
                    .document(currentUserID).get().await().get("displayName").toString()

            }


            Log.e("Details", "Group ID :$groupID \nUsername$userName")
            collectionReference = db.collection("Groups").document(groupID)
                .collection("Chats")


            Log.e("Collection", "collection assigned to $collectionReference")


            val query =
                collectionReference!!.orderBy(
                    "sendingTimestamp",
                    Query.Direction.ASCENDING
                )
            val chatsRecyclerViewOptions = FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel::class.java).build()


            val chatsAD = ChatsRecyclerViewAdapter(chatsRecyclerViewOptions)
            chatsAD.startListening()
            withContext(Dispatchers.Main) {
                chatsRecyclerView.adapter = chatsAD
            }

        }
    }
}


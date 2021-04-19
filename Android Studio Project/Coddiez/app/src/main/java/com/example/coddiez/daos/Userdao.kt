package com.example.coddiez.daos

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.coddiez.Individual_data_List_Adapter
import com.example.coddiez.models.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject


class Userdao {
    private val db = FirebaseFirestore.getInstance()
    val randomColorObj = RandomColors()
    val auth = Firebase.auth
    val currentUser = auth.currentUser!!.uid
    var mainUsr: Users? = null
    var extraUser: Users? = null
    var isworkingForGettingUserData = false
    val optional = db.collection("Users")

    var collectionForRankList = db.collection("Groups")
    var isWorking: Boolean = false
    var nessecityForUpdation = false
    var isWorkingForced = false
    var generalGroupID: String = ""

    fun compareVol(): Boolean {
        if (db.collection("Groups") == collectionForRankList) {
            Log.e("comparidon", "both are same $optional")
            return true

        } else {
            Log.e(
                "comparidon",
                "both are not same opt: $optional and \n col:$collectionForRankList"
            )
            return false
        }
    }

    fun setupLeaderboardCollection() {

        if (generalGroupID == "") {
            GlobalScope.launch(Dispatchers.IO) {

                generalGroupID =
                    db.collection("Users").document(currentUser).get().await().get("groupID")
                        .toString()


                collectionForRankList =
                    collectionForRankList.document(generalGroupID).collection("members")
                Log.e(
                    "collection set suc  ",
                    "collection : $collectionForRankList and\nid:$generalGroupID"
                )
            }
        } else {
            collectionForRankList =
                collectionForRankList.document(generalGroupID).collection("members")
            Log.e(
                "collection set suc  ",
                "collection : $collectionForRankList and\nid:$generalGroupID"
            )
        }

    }


    fun checkDataRefreshingNessecityAndUpdateAcc(rankDataList: ArrayList<platforms_list>) {
        isWorking = true
        GlobalScope.launch(Dispatchers.IO) {
            var booltoReturn: Boolean

            var ts: Long = 0
            var tempbull: Boolean = false

            Log.e("Group ID check", "Group ID default : $generalGroupID")

            if (generalGroupID == "") {
                generalGroupID =
                    db.collection("Users").document(currentUser).get().await().get("groupID")
                        .toString()
                Log.e("Group ID ", "Group ID received : $generalGroupID")

            }


            db.collection("Groups").document(generalGroupID).get()
                .addOnSuccessListener { documents ->
                    ts = documents.getLong("timestampForUpdation")!!
                    tempbull = documents.getBoolean("isNewbieHere") != false
                    Log.e("success", "data : $documents")

                    Log.e("values received", "ts = $ts  and \ntempbull = $tempbull ")
                    if (tempbull) {
                        booltoReturn = true
                        Log.e("conf", "returned at tempbull ")

                    } else {
                        val diff = System.currentTimeMillis() - ts

                        Log.e("timestamp", "ts is ${System.currentTimeMillis()}")
                        Log.e("diff", "Difference is ${diff / 60000} minutes")
                        booltoReturn = diff >= 3600000  //an hour
                    }

                    if (booltoReturn) {
                        nessecityForUpdation = true
                        getAllUsersData(rankDataList)
                    } else {
                        nessecityForUpdation = false
                        isWorking = false
                    }


                }.addOnFailureListener { exception ->
                    Log.e("error", "Error is : ${exception.message}")
                    Log.e("values received", "ts = $ts  and \ntempbull = $tempbull ")
                    isWorking = false
                    nessecityForUpdation = false
                }
        }

    }


    fun addUserToGroup(
        user: Users,
        timeStampData: timestampForDtaUpdation
    ) {
        user.let {
            GlobalScope.launch(Dispatchers.IO) {
                val collectionToAddInGroup =
                    db.collection("Groups")
                        .document(user.instituteName + "--" + user.joiningYear + "--" + user.courseName)
                collectionToAddInGroup.collection("members").document(user.userId).set(it)

                timeStampData.timestampForUpdation = System.currentTimeMillis()
                collectionToAddInGroup.set(timeStampData)

            }
        }
    }

    fun addUserToUsersCollection(
        user: Users,
        userGroupNameData: userGroupdata,
    ) {
        user.let {
            GlobalScope.launch(Dispatchers.IO) {

                userGroupNameData.groupID =
                    user.instituteName + "--" + user.joiningYear + "--" + user.courseName

                val collectionToAddUserData = db.collection("Users")
                collectionToAddUserData.document(user.userId).set(userGroupNameData)

            }
        }
    }

    fun getAllUsersData(rankDataList: ArrayList<platforms_list>) {
        isWorking = true

        GlobalScope.launch(Dispatchers.IO) {
            rankDataList.clear()


            val tempRankObj = platforms_list()

            if (generalGroupID == "") {
                generalGroupID =
                    db.collection("Users").document(currentUser).get().await().get("groupID")
                        .toString()
                Log.e("Group ID ", "Group ID received : $generalGroupID")

            }

            db.collection("Groups").document(generalGroupID).collection("members").get()
                .addOnSuccessListener { docs ->
                    if (docs != null) {
                        for (doc in docs) {
                            tempRankObj.codechef = doc.get("codeChefHandle").toString()
                            tempRankObj.codeforces = doc.get("codeForcesHandle").toString()
                            tempRankObj.spoj = doc.get("spojHandle").toString()
                            tempRankObj.interviewBit = doc.get("interviewBitHandle").toString()
                            tempRankObj.leetCode = doc.get("leetCodeHandle").toString()

                            rankDataList.add(
                                platforms_list(
                                    doc.get("codeChefHandle").toString(),
                                    doc.get("codeForcesHandle").toString(),
                                    doc.get("spojHandle").toString(),
                                    doc.get("interviewBitHandle").toString(),
                                    doc.get("leetCodeHandle").toString()
                                )
                            )
                            Log.d("data fetched", "data added : $tempRankObj")
//                            rankDataList.add(tempRankObj)
                        }
                        isWorking = false
                    }

                }

        }
    }

    fun setRankingDataToDatabase(
        context: Context,
        rankingDataList: ArrayList<rankData>,
        forcedRefreshData: LinearLayout?,
        forcedRefreshingTheDataINProgress: LinearLayout?,
    ) {
        isWorkingForced = true
        GlobalScope.launch(Dispatchers.IO) {
            Log.e("Group ID ", "Group ID received : $generalGroupID")


            val idList = ArrayList<String>()
            db.collection("Groups").document(generalGroupID).collection("members").get()
                .addOnSuccessListener { docs ->
                    if (docs != null) {
                        for (doc in docs) {
                            idList.add(doc.id)
                            Log.e("fetching IDs", "currunt ID : ${doc.id}")
                        }
                    }

                    val size = idList.size
                    Log.e("ID fetch ", "numbers of ID received : $size")

                    for (i in 0 until size) {

                        db.collection("Groups").document(generalGroupID).collection("members")
                            .document(idList[i]).update(
                                mapOf(
                                    "rankData" to rankingDataList[i],
                                )
                            ).addOnSuccessListener {
                                Log.e(
                                    "Progress",
                                    "at ${i + 1} out of $size in updating data to database\n Data : ${rankingDataList[i]} \nTo : ${idList[i]}"
                                )



                                if (i == size - 1) {

                                    isWorkingForced = false


                                    Log.e(
                                        "Success",
                                        "rank data uploading to the database completed successfully"
                                    )
                                    db.collection("Groups").document(generalGroupID).update(
                                        mapOf(
                                            "isNewbieHere" to false,
                                            "timestampForUpdation" to System.currentTimeMillis()
                                        )
                                    )

                                    Log.e(
                                        "Views CHeck",
                                        "forecedRefreshingTheDataINProgress is ${forcedRefreshingTheDataINProgress}\n\nforecedRefreshData.visibility is $forcedRefreshData"
                                    )



                                    if (forcedRefreshingTheDataINProgress != null) {

                                        forcedRefreshingTheDataINProgress.visibility = View.GONE
                                        forcedRefreshData!!.visibility = View.VISIBLE

                                    }


                                }

                            }.addOnFailureListener {
                                isWorkingForced = false
                                Log.e(
                                    "Failure",
                                    "rank data uploading to the database failed"
                                )
                                if (forcedRefreshingTheDataINProgress != null) {

                                    forcedRefreshingTheDataINProgress.visibility = View.GONE
                                    forcedRefreshData!!.visibility = View.VISIBLE

                                }
                            }
                    }

                }.addOnFailureListener {
                    isWorkingForced = false


                    Toast.makeText(
                        context,
                        "An internal error occurred :${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    if (forcedRefreshData != null) {
                        if (forcedRefreshingTheDataINProgress != null) {
                            forcedRefreshingTheDataINProgress.visibility = View.GONE
                        }
                        forcedRefreshData.visibility = View.VISIBLE
                    }
                }
        }
    }

    fun loadndividualDataAndPutItHere(
        contextOfActivity: Context,
        individualRankDataObj: IndividualRankData,
        rankingsBaseUrl: String,
        rankingPlatfomQuery: Array<String>,
        personalRankDataRecyclerView: RecyclerView,
        forecedRefreshPerosnalData: LinearLayout,
        forecedRefreshingThePersonalDataINProgress: LinearLayout
    ) {
        GlobalScope.launch(Dispatchers.IO) {

            if (generalGroupID == "") {
                generalGroupID =
                    db.collection("Users").document(currentUser).get().await().get("groupID")
                        .toString()
            }

            mainUsr =
                db.collection("Groups").document(generalGroupID).collection("members")
                    .document(currentUser).get().await().toObject(Users::class.java)!!

            val tempPlatformsListDataObj = arrayOf(
                mainUsr!!.codeChefHandle,
                mainUsr!!.codeForcesHandle,
                mainUsr!!.spojHandle,
                mainUsr!!.interviewBitHandle,
                mainUsr!!.leetCodeHandle
            )


            val individualDataqueue: RequestQueue = Volley.newRequestQueue(contextOfActivity)
            var toCmplInd = 0

            for (i in 0..4) {
                if (tempPlatformsListDataObj[i] != "" && tempPlatformsListDataObj[i] != " ") {
                    toCmplInd++
                }
            }

            Log.e("Index", "index to complete is $toCmplInd")

            for (i in 0..4) {


                if (tempPlatformsListDataObj[i] != "" && tempPlatformsListDataObj[i] != " ") {
                    val jsonObjectRequest = JsonObjectRequest(
                        Request.Method.GET,
                        rankingsBaseUrl + rankingPlatfomQuery[i] + "/" + tempPlatformsListDataObj[i],
                        null,
                        { response ->
                            if (response.getString("status") == "Success") {
                                parseResponseAccordingly(
                                    i,
                                    individualRankDataObj,
                                    response,
                                    personalRankDataRecyclerView
                                )
                                individualRankDataObj.dataStatus[i] = true
                            } else if (i == 0) {
                                val ad = Individual_data_List_Adapter(
                                    arrayOf(
                                        Pair(
                                            " No Data available  \n Try changing username ",
                                            ""
                                        )
                                    )
                                )
                                personalRankDataRecyclerView.adapter = ad
                            }

                            toCmplInd--
                            if (toCmplInd == 0) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    forecedRefreshingThePersonalDataINProgress.visibility =
                                        View.GONE
                                    forecedRefreshPerosnalData.visibility = View.VISIBLE
                                    personalRankDataRecyclerView.visibility = View.VISIBLE
                                }

                            }


                            Log.e("Response InData", "Response : $response")
                        },
                        { error ->
                            Log.e("Failure", "Error : $error \n i is :$i")

                            val ad = Individual_data_List_Adapter(
                                arrayOf(
                                    Pair(
                                        " No Data available  \n Try changing username ",
                                        ""
                                    )
                                )
                            )

                            personalRankDataRecyclerView.adapter = ad

                            forecedRefreshingThePersonalDataINProgress.visibility = View.GONE
                            forecedRefreshPerosnalData.visibility = View.VISIBLE
                            personalRankDataRecyclerView.visibility = View.VISIBLE
                        })

                    individualDataqueue.add(jsonObjectRequest)

                }


//                if (i == 4) {
//                    CoroutineScope(Dispatchers.Main).launch {
//                        forecedRefreshingThePersonalDataINProgress.visibility = View.GONE
//                        forecedRefreshPerosnalData.visibility = View.VISIBLE
//                        personalRankDataRecyclerView.visibility = View.VISIBLE
//                    }
//                }


            }


        }.invokeOnCompletion { }
    }

    private fun parseResponseAccordingly(
        i: Int,
        individualRankDataObj: IndividualRankData,
        response: JSONObject?,
        personalRankDataRecyclerView: RecyclerView
    ) {
        when (i) {
            0 -> {
                individualRankDataObj.codechefDataI["rating"] = response!!.get("rating")
                individualRankDataObj.codechefDataI["stars"] = response.get("stars")
                individualRankDataObj.codechefDataI["highest_rating"] =
                    response.get("highest_rating")
                individualRankDataObj.codechefDataI["global_rank"] = response.get("global_rank")
                individualRankDataObj.codechefDataI["country_rank"] = response.get("country_rank")


                val ad = Individual_data_List_Adapter(
                    individualRankDataObj.codechefDataI.toList().toTypedArray()
                )
                Log.e("Adapter", "Adapter is $ad")

                personalRankDataRecyclerView.adapter = ad

                Log.e("Codechef Data", "Data Added : ${individualRankDataObj.codechefDataI}")

            }
            1 -> {
                individualRankDataObj.codeForcesDataI["rating"] = response!!.get("rating")
                individualRankDataObj.codeForcesDataI["max rating"] = response.get("max rating")
                individualRankDataObj.codeForcesDataI["rank"] =
                    response.get("rank")
                individualRankDataObj.codeForcesDataI["max rank"] = response.get("max rank")

                Log.e("Codechef Data", "Data Added : ${individualRankDataObj.codeForcesDataI}")
            }
            2 -> {
                individualRankDataObj.spojDataI["points"] = response!!.get("points")
                individualRankDataObj.spojDataI["rank"] =
                    response.get("rank")

                Log.e("Codechef Data", "Data Added : ${individualRankDataObj.spojDataI}")
            }
            3 -> {
                individualRankDataObj.interviewBitDataI["rank"] = response!!.get("rank")
                individualRankDataObj.interviewBitDataI["score"] = response.get("score")

                Log.e("Codechef Data", "Data Added : ${individualRankDataObj.interviewBitDataI}")
            }
            4 -> {
                individualRankDataObj.leetCodeDataI["total_problems_solved"] =
                    response!!.get("total_problems_solved")
                individualRankDataObj.leetCodeDataI["acceptance_rate"] =
                    response.get("acceptance_rate")
                individualRankDataObj.leetCodeDataI["easy_questions_solved"] =
                    response.get("easy_questions_solved")
                individualRankDataObj.leetCodeDataI["total_easy_questions"] =
                    response.get("total_easy_questions")
                individualRankDataObj.leetCodeDataI["medium_questions_solved"] =
                    response.get("medium_questions_solved")
                individualRankDataObj.leetCodeDataI["total_medium_questions"] =
                    response.get("total_medium_questions")
                individualRankDataObj.leetCodeDataI["hard_questions_solved"] =
                    response.get("hard_questions_solved")
                individualRankDataObj.leetCodeDataI["total_hard_questions"] =
                    response.get("total_hard_questions")
                individualRankDataObj.leetCodeDataI["contribution_points"] =
                    response.get("contribution_points")
                individualRankDataObj.leetCodeDataI["contribution_problems"] =
                    response.get("contribution_problems")
                individualRankDataObj.leetCodeDataI["contribution_testcases"] =
                    response.get("contribution_testcases")
                individualRankDataObj.leetCodeDataI["reputation"] = response.get("reputation")




                Log.e("Codechef Data", "Data Added : ${individualRankDataObj.leetCodeDataI}")
            }

        }
    }

    fun getUserDataByIDAndSet(
        givenID: String?
    ) {

        isworkingForGettingUserData = true
        GlobalScope.launch(Dispatchers.IO) {
            val idToUse: String = givenID ?: currentUser
            Log.e("ID", "ID is $idToUse")

            val tempGroupID: String = if (givenID == null && generalGroupID != "") {
                generalGroupID
            } else {
                db.collection("Users").document(idToUse).get().await().get("groupID")
                    .toString()
            }

            Log.e("GroupID", "group ID is $tempGroupID")


            val tempUsr: Users = if (givenID == null && mainUsr != null) {
                mainUsr!!
            } else {
                db.collection("Groups").document(tempGroupID).collection("members")
                    .document(idToUse).get().await().toObject(Users::class.java)!!
            }

            if (givenID == null) {
                mainUsr = tempUsr
            } else {
                extraUser = tempUsr
            }

            isworkingForGettingUserData = false


//            setUpLayoutForLinks(
//                tempUsr,
//                codeForcesLinkBtn,
//                codeChefLinkBtn,
//                spoJLinkBtn,
//                interviewBitLinkBtn,
//                leetCodeLinkBtn,
//                hackerRankLinkBtn,
//                gitHubLinkBtn,
//                linkedInLinkBtn,
//                instagramLinkBtn,
//                faceBookLinkBtn,
//                personNameTextView,
//                courseNameView,
//                instituteNameView
//            )

        }


    }


}




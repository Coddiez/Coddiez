package com.example.coddiez

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.coddiez.daos.ChatDao
import com.example.coddiez.daos.IndividualRankData
import com.example.coddiez.daos.Userdao
import com.example.coddiez.models.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity2 : AppCompatActivity() {

    //    Declaratioons

    private var mainUser: Users = Users()
    private lateinit var rankListAdapter: RankLIstAdapter
    private var contestTimingIdex: Int = 0
    private var contestplatformIndex: Int = 0
    private var curruntSelectedPlatformForSelfProgress = 0
    private val individualDataObj = IndividualRankData()
    private var contestBaseUrl: String = "https://kontests.net/api/v1/"
    private var gettUrlByHadle = GetUrlByHadle()
    private var contestPlatformQuery = arrayOf(
        "hacker_rank",
        "code_chef",
        "codeforces",
        "top_coder",
        "hacker_earth",
        "leet_code",
        "at_coder",
        "kick_start"
    )
    private val rankingsBaseUrl: String = "https://competitive-coding-api.herokuapp.com/api/"
    private var isWorkGoingOnForRankData: Int = 0
    private var curruntSelectedPlatformLeadeBoard = 0
    private var rankingPlatfomQuery = arrayOf(
        "codechef",
        "codeforces",
        "spoj",
        "interviewbit",
        "leetcode"
    )
    private var leaderBoardRankingDataPlatformChoiceForFirebaseQuery = arrayOf(
        "codechefData",
        "codeforcesData",
        "spjData",
        "interviewbitData",
        "leetcodeData"
    )

    private var curruntLayoutIndex: Int = 0
    private val userdao = Userdao()

    var chatDao = ChatDao()

    private val platformnsList = ArrayList<platforms_list>()
    private val rankingDataList = ArrayList<rankData>()

    lateinit var mainHandler: Handler
    private lateinit var queueForRankData: RequestQueue

    var curruntTimings = ArrayList<contestTimings>()
    var futureTimings = ArrayList<contestTimings>()

    private var hasBeenClickedBefore = arrayOf(true, false, false, false)
    private val randomAnimeImageUrlObj = GetRandomAnimeImageUrl()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        chatDao.getUsernameAndGroupID()
        randomAnimeImageUrlObj.getURL(this)
        queueForRankData = Volley.newRequestQueue(this)

        initAll()
    }


    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTextTask)
    }


    private fun initAll() {
        fetchAndSetContestData()

        bottomNavSetter()


        contestTimingButtonsSetter()
        onclickListenersForcontestButtons()


        personalRankDataRecyclerView.layoutManager = LinearLayoutManager(this)
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        mainHandler = Handler(Looper.getMainLooper())

        setUpOnClickListenersForLeaderboardDataButtons()



        individualDataObj.loadndividualData(
            this,
            userdao,
            rankingsBaseUrl,
            rankingPlatfomQuery,
            personalRankDataRecyclerView,
            forecedRefreshPerosnalData,
            forecedRefreshingThePersonalDataINProgress,
        )

        individualDataButtonsOnClickListenersSetter()
        forcedRefreshingDataOnClickListenerSetter()
        chatsSendOnCLickListnerSetter()

    }

    private fun chatsSendOnCLickListnerSetter() {
        msgSendBtn.setOnClickListener {
            if (msgEditText.text.isNotEmpty()) {
                chatDao.sendMsg(msgEditText.text.toString())
                msgEditText.setText("")
            }
        }
    }

    private fun forcedRefreshingDataOnClickListenerSetter() {

        personalRankingDataRefreshButton.setOnClickListener {
            personalRankDataRecyclerView.visibility = View.GONE
            forecedRefreshPerosnalData.visibility = View.INVISIBLE
            forecedRefreshingThePersonalDataINProgress.visibility = View.VISIBLE
            individualDataObj.loadndividualData(
                this,
                userdao,
                rankingsBaseUrl,
                rankingPlatfomQuery,
                personalRankDataRecyclerView,
                forecedRefreshPerosnalData,
                forecedRefreshingThePersonalDataINProgress,
            )


        }


        rankingDataRefreshButton.setOnClickListener {

            forecedRefreshData.visibility = View.GONE
            forecedRefreshingTheDataINProgress.visibility = View.VISIBLE

            Thread {

                Log.e("Confirmation", "being called")

                userdao.getAllUsersData(platformnsList)
                while (userdao.isWorking) {
                    Thread.sleep(1000)
                    Log.e("wait", "waiting")

                }

                Log.d("Loading Complete", "platform data received\nData : $platformnsList")

                rankingDataList.clear()
                val size = platformnsList.size


                for (i in 0 until size) {
                    rankingDataList.add(rankData())
                    for (j in 0 until 5) {
                        isWorkGoingOnForRankData++
                        loadRankingDataPlatformWise(getUsernameByIndex(platformnsList[i], j), j, i)
                    }
                }

                while (isWorkGoingOnForRankData != 0) {
                    Thread.sleep(4000)
                    Log.e("wait", "waiting for data from volley")
                }

                Log.e("Ranking Data List", "Data is :$rankingDataList")

                userdao.setRankingDataToDatabase(
                    this,
                    rankingDataList,
                    forecedRefreshData,
                    forecedRefreshingTheDataINProgress
                )

                while (userdao.isWorkingForced) {
                    Thread.sleep(500)
                    Log.e("wait", "waiting for confirmation about isworkingForced")
                }

                setUpRankListRecyclerView(curruntSelectedPlatformLeadeBoard)

            }.start()
        }
    }

    private fun individualDataButtonsOnClickListenersSetter() {
        codechefForSelfProgress.setOnClickListener {
            if (curruntSelectedPlatformForSelfProgress != 0) {

                Log.e("Individual Data", "Data is :${individualDataObj.codechefDataI}")

                setUpIndividualDataListRecyclerView(0)
                turnCurruntNonSelectedForIndividualData()
                curruntSelectedPlatformForSelfProgress = 0
                turnToSelectedForIndividualData()

            }
        }
        codeforcesForSelfProgress.setOnClickListener {
            if (curruntSelectedPlatformForSelfProgress != 1) {

                Log.e("Individual Data", "Data is :${individualDataObj.codeForcesDataI}")


                setUpIndividualDataListRecyclerView(1)
                turnCurruntNonSelectedForIndividualData()
                curruntSelectedPlatformForSelfProgress = 1
                turnToSelectedForIndividualData()

            }
        }
        spojForSelfProgress.setOnClickListener {

            Log.e("Individual Data", "Data is :${individualDataObj.spojDataI}")


            if (curruntSelectedPlatformForSelfProgress != 2) {
                setUpIndividualDataListRecyclerView(2)
                turnCurruntNonSelectedForIndividualData()
                curruntSelectedPlatformForSelfProgress = 2
                turnToSelectedForIndividualData()

            }
        }
        interviewBitForSelfProgress.setOnClickListener {
            Log.e("Individual Data", "Data is :${individualDataObj.interviewBitDataI}")

            if (curruntSelectedPlatformForSelfProgress != 3) {
                setUpIndividualDataListRecyclerView(3)
                turnCurruntNonSelectedForIndividualData()
                curruntSelectedPlatformForSelfProgress = 3
                turnToSelectedForIndividualData()

            }
        }
        leetCodeForSelfProgress.setOnClickListener {

            Log.e("Individual Data", "Data is :${individualDataObj.leetCodeDataI}")


            if (curruntSelectedPlatformForSelfProgress != 4) {
                setUpIndividualDataListRecyclerView(4)
                turnCurruntNonSelectedForIndividualData()
                curruntSelectedPlatformForSelfProgress = 4
                turnToSelectedForIndividualData()
            }
        }
    }

    private fun turnToSelectedForIndividualData() {
        when (curruntSelectedPlatformForSelfProgress) {

            0 -> {
                codechefForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.whitemain
                    )
                )
                codechefForSelfProgress.background =
                    ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            1 -> {
                codeforcesForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.whitemain
                    )
                )
                codeforcesForSelfProgress.background =
                    ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            2 -> {
                spojForSelfProgress.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                spojForSelfProgress.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            3 -> {
                interviewBitForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.whitemain
                    )
                )
                interviewBitForSelfProgress.background =
                    ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            4 -> {
                leetCodeForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.whitemain
                    )
                )
                leetCodeForSelfProgress.background =
                    ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
        }
    }

    private fun turnCurruntNonSelectedForIndividualData() {

        when (curruntSelectedPlatformForSelfProgress) {
            0 -> {
                codechefForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                codechefForSelfProgress.background = null
            }
            1 -> {
                codeforcesForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                codeforcesForSelfProgress.background = null
            }
            2 -> {
                spojForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                spojForSelfProgress.background = null
            }
            3 -> {
                interviewBitForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                interviewBitForSelfProgress.background = null
            }
            4 -> {
                leetCodeForSelfProgress.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                leetCodeForSelfProgress.background = null
            }
        }
    }

    private fun setUpIndividualDataListRecyclerView(i: Int) {
        if (individualDataObj.dataStatus[i]) {
            when (i) {
                0 -> {
                    val ad = Individual_data_List_Adapter(
                        individualDataObj.codechefDataI.toList().toTypedArray()
                    )
                    Log.d("Adapter", "Adapter is $ad")
                    personalRankDataRecyclerView.adapter = ad
                }
                1 -> {
                    val ad = Individual_data_List_Adapter(
                        individualDataObj.codeForcesDataI.toList().toTypedArray()
                    )
                    Log.d("Adapter", "Adapter is $ad")
                    personalRankDataRecyclerView.adapter = ad
                }
                2 -> {
                    val ad = Individual_data_List_Adapter(
                        individualDataObj.spojDataI.toList().toTypedArray()
                    )
                    Log.d("Adapter", "Adapter is $ad")
                    personalRankDataRecyclerView.adapter = ad
                }
                3 -> {
                    val ad = Individual_data_List_Adapter(
                        individualDataObj.interviewBitDataI.toList().toTypedArray()
                    )
                    personalRankDataRecyclerView.adapter = ad

                    Log.d("Adapter", "Adapter is $ad")
                }
                4 -> {
                    val ad = Individual_data_List_Adapter(
                        individualDataObj.leetCodeDataI.toList().toTypedArray()
                    )
                    Log.d("Adapter", "Adapter is $ad")
                    personalRankDataRecyclerView.adapter = ad
                }
            }

        } else {
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

    }


    private fun setUpOnClickListenersForLeaderboardDataButtons() {
        codechefForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 0) {
                setUpRankListRecyclerView(0)
                turnCurruntNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 0
                turnToSelectedForLeaderBoard()

            }
        }
        codeforcesForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 1) {
                setUpRankListRecyclerView(1)
                turnCurruntNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 1
                turnToSelectedForLeaderBoard()

            }
        }
        spojForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 2) {
                setUpRankListRecyclerView(2)
                turnCurruntNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 2
                turnToSelectedForLeaderBoard()

            }
        }
        interviewBitForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 3) {
                setUpRankListRecyclerView(3)
                turnCurruntNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 3
                turnToSelectedForLeaderBoard()

            }
        }
        leetCodeForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 4) {
                setUpRankListRecyclerView(4)
                turnCurruntNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 4
                turnToSelectedForLeaderBoard()
            }
        }
    }

    private fun turnToSelectedForLeaderBoard() {
        when (curruntSelectedPlatformLeadeBoard) {
            0 -> {
                codechefForRank.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                codechefForRank.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            1 -> {
                codeforcesForRank.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                codeforcesForRank.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            2 -> {
                spojForRank.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                spojForRank.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            3 -> {
                interviewBitForRank.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                interviewBitForRank.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            4 -> {
                leetCodeForRank.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                leetCodeForRank.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
        }


    }

    private fun turnCurruntNonSelectedForLeaderBoard() {
        when (curruntSelectedPlatformLeadeBoard) {
            0 -> {
                codechefForRank.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                codechefForRank.background = null
            }
            1 -> {
                codeforcesForRank.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                codeforcesForRank.background = null
            }
            2 -> {
                spojForRank.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                spojForRank.background = null
            }
            3 -> {
                interviewBitForRank.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                interviewBitForRank.background = null
            }
            4 -> {
                leetCodeForRank.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                leetCodeForRank.background = null
            }
        }
    }

    fun setUpRankListRecyclerView(platformI: Int) {
        Thread {

            if (userdao.compareVol()) {
                userdao.setupLeaderboardCollection()
                while (userdao.compareVol()) {
                    Thread.sleep(1000)
                }
            }

            val collectionForLeaderBoard: CollectionReference =
                userdao.collectionForRankList

            Log.e("assign Success", "collection assigned to $collectionForLeaderBoard")


            val queryForLeaderboard =
                collectionForLeaderBoard.orderBy(
                    "rankData.${leaderBoardRankingDataPlatformChoiceForFirebaseQuery[platformI]}",
                    Query.Direction.DESCENDING
                )
            val leaderboardRecylerViewOptions = FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(queryForLeaderboard, Users::class.java).build()


            runOnUiThread {
                rankListAdapter = RankLIstAdapter(leaderboardRecylerViewOptions, platformI)
                rankListAdapter.startListening()
                rankListRecyclerView.adapter = rankListAdapter
                rankListRecyclerView.layoutManager = LinearLayoutManager(this)
            }

        }.start()

    }

    private val updateTextTask = object : Runnable {
        override fun run() {

            updateLeaderboardDataOfDatabase()
            mainHandler.postDelayed(this, 300000)
        }
    }

    private fun updateLeaderboardDataOfDatabase() {

        Thread {

            Log.e("Confirmation", "being called")

            userdao.checkDataRefreshingNessecityAndUpdateAcc(platformnsList)
            while (userdao.isWorking) {
                Thread.sleep(1000)
                Log.e("wait", "waiting")

            }

            Log.d("Loading Complete", "platform data received\nData : $platformnsList")

            if (userdao.nessecityForUpdation) {
                rankingDataList.clear()
                val size = platformnsList.size


                for (i in 0 until size) {
                    rankingDataList.add(rankData())
                    for (j in 0 until 5) {
                        isWorkGoingOnForRankData++
                        loadRankingDataPlatformWise(getUsernameByIndex(platformnsList[i], j), j, i)
                    }
                }

                while (isWorkGoingOnForRankData != 0) {
                    Thread.sleep(4000)
                    Log.e("wait", "waiting for data from volley")
                }


                userdao.setRankingDataToDatabase(
                    this,
                    rankingDataList,
                    null,
                    null
                )
            }


        }.start()
    }

    private fun getUsernameByIndex(platformsList: platforms_list, j: Int): String {
        when (j) {
            0 -> {
                return platformsList.codechef
            }
            1 -> {
                return platformsList.codeforces
            }
            2 -> {
                return platformsList.spoj
            }
            3 -> {
                return platformsList.interviewBit
            }
            4 -> {
                return platformsList.leetCode
            }
            else -> {
                return platformsList.codechef
            }
        }
    }

    private fun loadRankingDataPlatformWise(
        handleProvided: String,
        platformI: Int,
        lastIndex: Int
    ) {
        if (handleProvided == "" || handleProvided == " ") {
            setThisDataToRankData(0.00, platformI, lastIndex)
            isWorkGoingOnForRankData--
            if (isWorkGoingOnForRankData == 0) {
                Log.d(
                    "data gathering",
                    "individual score data received\nData : $rankingDataList"
                )

            }
        } else {
            var valueToReturn: Double = 0.00
            GlobalScope.launch(Dispatchers.IO) {

                try {
                    val stringRequest =
                        JsonObjectRequest(
                            Request.Method.GET,
                            rankingsBaseUrl + rankingPlatfomQuery[platformI] + "/" + handleProvided,
                            null,
                            { response ->
                                Log.d(
                                    "data received",
                                    "Response is: ${response}, \n length : ${

                                        response
                                            .length()
                                    }"
                                )

                                Log.e(
                                    "URL",
                                    "URL was: ${rankingsBaseUrl + rankingPlatfomQuery[platformI] + "/" + handleProvided}"
                                )

                                if (response.getString("status") == "Success") {
                                    valueToReturn =
                                        findRankingDataFromJson(response, platformI)
                                }
                                setThisDataToRankData(valueToReturn, platformI, lastIndex)
                                isWorkGoingOnForRankData--
                                if (isWorkGoingOnForRankData == 0) {
                                    Log.d(
                                        "data gathering",
                                        "individual score data received\nData : $rankingDataList"
                                    )

                                }
                            },
                            { error ->
                                Log.e("Error data Fetching", error.toString())
                                setThisDataToRankData(0.00, platformI, lastIndex)

                                isWorkGoingOnForRankData--
                                if (isWorkGoingOnForRankData == 0) {
                                    Log.d(
                                        "data gathering",
                                        "individual score data received\nData : $rankingDataList"
                                    )

                                }


                            })

                    queueForRankData.add(stringRequest)

                } catch (error: Error) {
                    setThisDataToRankData(0.00, platformI, lastIndex)

                    Log.e(
                        "Contest Data Error",
                        "Error Occurred While fetching data :\n \tError : ${error.toString()}"
                    )
//                errorWhileLoadingContestsDataViewsAdjustment()
                }

            }


        }
    }

    private fun setThisDataToRankData(i: Double, platformI: Int, lastIndex: Int) {

        when (platformI) {
            0 -> {
                rankingDataList[lastIndex].codechefData = i.toInt()
            }
            1 -> {
                rankingDataList[lastIndex].codeforcesData = i.toInt()
            }
            2 -> {
                rankingDataList[lastIndex].spjData = i
            }
            3 -> {
                rankingDataList[lastIndex].interviewbitData = i.toInt()
            }
            4 -> {
                rankingDataList[lastIndex].leetcodeData = i.toInt()
            }
        }

    }

    private fun findRankingDataFromJson(jsonData: JSONObject, platformI: Int): Double {
        when (platformI) {
            0 -> {
                return jsonData.getString("highest_rating").toDouble()
            }
            1 -> {
                return if (
                    jsonData.getString("max rating") == "Unrated") {
                    0.00
                } else {
                    jsonData.getString("max rating").toDouble()
                }
            }
            2 -> {
                return jsonData.getString("points").toDouble()
            }
            3 -> {
                return jsonData.getString("score").toDouble()
            }
            4 -> {
                return jsonData.getString("contribution_points").toDouble()
            }

        }
        return 0.00
    }


    private fun contestTimingButtonsSetter() {
        ongoing.setOnClickListener {
            if (contestTimingIdex == 1) {
                contestTimingIdex = 0
                checkAndHandleViews()
                updatedContestListData()
                upcoming.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                upcoming.background = null
                ongoing.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                ongoing.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
        }
        upcoming.setOnClickListener {
            if (contestTimingIdex == 0) {
                contestTimingIdex = 1
                checkAndHandleViews()
                updatedContestListData()
                ongoing.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                ongoing.background = null
                upcoming.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                upcoming.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
        }

    }


    private fun fetchAndSetContestData() {


        val queue = Volley.newRequestQueue(this)
        GlobalScope.launch(Dispatchers.IO) {

            try {
                val stringRequest =
                    StringRequest(
                        Request.Method.GET,
                        contestBaseUrl + contestPlatformQuery[contestplatformIndex],
                        { response ->
                            Log.d(
                                "data received",
                                "Response is: ${response.toString()}, \n length : ${
                                    JSONArray(
                                        response
                                    ).length()
                                }"
                            )

                            devideList(JSONArray(response))
                            checkAndHandleViews()
                            updatedContestListData()
                        },
                        { error ->
                            Log.e("Errordata Fetching", error.toString())
                            if (error.networkResponse != null && error.networkResponse.statusCode == 200) {
                                showNoDataAvailableForContest()
                            }
                        })

                queue.add(stringRequest)

            } catch (error: Error) {
                Log.e(
                    "Contest Data Error",
                    "Error Occurred While fetching data :\n \tError : ${error.toString()}"
                )
//                errorWhileLoadingContestsDataViewsAdjustment()
            }

        }
    }

    private fun loadingDataViewsChangeForContestsList() {
        if (errorInContestDataLayout.visibility == View.VISIBLE) {
            errorInContestDataLayout.visibility = View.GONE
        }
        contestDetailsList.visibility = View.GONE
        loadingContestData.visibility = View.VISIBLE
    }

    private fun updatedContestListData() {
        contestDetailsList.layoutManager = LinearLayoutManager(this)
        val ad: contest_list_adapter
        if (contestTimingIdex == 0) {
            if (curruntTimings.size != 0) {
                ad = contest_list_adapter(curruntTimings.toTypedArray())
                contestDetailsList.adapter = ad
//                rankListRecyclerView.adapter = ad

            } else {
                showNoDataAvailableForContest()
            }
        } else {
            if (futureTimings.size != 0) {
                ad = contest_list_adapter(futureTimings.toTypedArray())
                contestDetailsList.adapter = ad
            } else {
                showNoDataAvailableForContest()
            }

        }

    }


    private fun devideList(jsonArray: JSONArray) {

        curruntTimings.clear()
        futureTimings.clear()


        var tempschedualeObj: contestTimings
        var tempJsonObject: JSONObject
        var lengthObj = jsonArray.length()


        for (i in 0 until lengthObj) {
            tempJsonObject = jsonArray.getJSONObject(i)
            tempschedualeObj = contestTimings(
                tempJsonObject.getString("name"),
                timeFormatterForContestList(tempJsonObject.getString("start_time")),
                timeFormatterForContestList(tempJsonObject.getString("end_time"))
            )
            if (tempJsonObject.getString("status") == "CODING") {
                curruntTimings.add(tempschedualeObj)
            } else {
                futureTimings.add(tempschedualeObj)
            }
        }

        Log.d(
            "data division",
            "length of ongoing contests list : ${curruntTimings.size}  \n length of future contest list : ${futureTimings.size}"
        )
    }


    private fun timeFormatterForContestList(string: String): String {


        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd-MM-yyyy\nHH:mm", Locale.ENGLISH)
        outputFormat.timeZone = TimeZone.getTimeZone("GMT+11")

        val date: Date = inputFormat.parse(string)!!
        val formattedDate: String = outputFormat.format(date)
        Log.e("TIME CHECK WO O", "Result is :$formattedDate")

        return formattedDate

    }


    private fun checkAndHandleViews() {

        loadingContestData.visibility = View.GONE

        if (contestDetailsList.visibility == View.GONE) {
            contestDetailsList.visibility = View.VISIBLE
        }
        if (errorInContestDataLayout.visibility == View.VISIBLE) {
            errorInContestDataLayout.visibility = View.GONE
        }
    }

    private fun showNoDataAvailableForContest() {
        contestDetailsList.visibility = View.GONE
        errorInContestDataLayout.visibility = View.VISIBLE
    }


    private fun bottomNavSetter() {

        bottomNavigationView.setOnNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.home -> {
                    setCurruntLayout(0)
                    curruntLayoutIndex = 0
                }
                R.id.leaderBoard -> {
                    if (!hasBeenClickedBefore[1]) {
                        setUpRankListRecyclerView(0)
                        hasBeenClickedBefore[1] = true
                    }
                    setCurruntLayout(1)
                    curruntLayoutIndex = 1

                }
                R.id.chats -> {

                    if (!hasBeenClickedBefore[2]) {
                        setUpChats()
                        hasBeenClickedBefore[2] = true
                    }
                    setCurruntLayout(2)
                    curruntLayoutIndex = 2
                }
                R.id.profile -> {

                    if (!hasBeenClickedBefore[3]) {
                        setUpProfileTop()
                        hasBeenClickedBefore[3] = true
                    }


                    setCurruntLayout(3)
                    curruntLayoutIndex = 3
                }

            }
            true
        }
    }

    private fun setUpProfileTop() {
        Log.e("Called", "thread has been called00")

        Thread {

            Log.e("Called", "thread has been called")
            runOnUiThread {
                Glide
                    .with(this)
                    .load(randomAnimeImageUrlObj.url)
                    .centerCrop()
                    .placeholder(R.color.whitesecondary)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(personProfilePicture)



            }

            if (userdao.mainUsr == null) {
                userdao.getUserDataByIDAndSet(null)
            }

            while (userdao.isworkingForGettingUserData) {
                Thread.sleep(500)
            }

            Log.e("User Data Received", "User : ${userdao.mainUsr}")


            runOnUiThread {
                val tempUsr = userdao.mainUsr!!
                setUpOnCLickByUserName(codeForcesLinkBtn, tempUsr.codeForcesHandle,0)
                setUpOnCLickByUserName(codeChefLinkBtn, tempUsr.codeChefHandle,1)
                setUpOnCLickByUserName(spoJLinkBtn, tempUsr.spojHandle,2)
                setUpOnCLickByUserName(interviewBitLinkBtn, tempUsr.interviewBitHandle,2)
                setUpOnCLickByUserName(leetCodeLinkBtn, tempUsr.leetCodeHandle,4)
                setUpOnCLickByUserName(hackerRankLinkBtn, tempUsr.hackerRankHandle,5)
                setUpOnCLickByUserName(gitHubLinkBtn, tempUsr.gitHubHandle,6)
                setUpOnCLickByUserName(linkedInLinkBtn, tempUsr.linkedInHandle,7)
                setUpOnCLickByUserName(instagramLinkBtn, tempUsr.instagramHandle,8)
                setUpOnCLickByUserName(faceBookLinkBtn, tempUsr.facebookHandle,9)


                personNameTextView.text = tempUsr.displayName
                courseNameView.setTextColor(userdao.randomColorObj.getRnd())
                instituteNameView.setTextColor(userdao.randomColorObj.getRnd())
                courseNameView.text = tempUsr.courseName
                instituteNameView.text = tempUsr.instituteName

            }
        }.start()

    }


    //                codeForcesLinkBtn: ImageButton,
//                codeChefLinkBtn: ImageButton,
//                spoJLinkBtn: ImageButton,
//                interviewBitLinkBtn: ImageButton,
//                leetCodeLinkBtn: ImageButton,
//                hackerRankLinkBtn: ImageButton,
//                gitHubLinkBtn: ImageButton,
//                linkedInLinkBtn: ImageButton,
//                instagramLinkBtn: ImageButton,
//                faceBookLinkBtn: ImageButton,
//                personNameTextView: TextView,
//                courseNameView: TextView,
//                instituteNameView: TextView


//                private fun setUpLayoutForLinks(
//                    tempUsr: Users,
//
//                ) {

    private fun setUpOnCLickByUserName(btnGiven: ImageButton, handleGiven: String, i : Int) {

        Log.e("Handle", "handle provided for $btnGiven is \n $handleGiven")
        if (handleGiven!=""&&handleGiven!=" ") {
            btnGiven.visibility = View.VISIBLE
            btnGiven.setOnClickListener {
                val openURL = Intent(android.content.Intent.ACTION_VIEW)
                openURL.data = Uri.parse(gettUrlByHadle.getLink(handleGiven, i))
                startActivity(openURL)
            }
        }
    }


    private fun setUpChats() {
        chatDao.setUpChatRecyclerView(chatsRecyclerView)

    }

    private fun setCurruntLayout(i: Int) {

        if (curruntLayoutIndex != i) {
            when (i) {
                0 -> {
                    if (curruntLayoutIndex == 3) {
                        profile_top_portion.visibility = View.GONE
                        homeContestsListDataContainer.visibility = View.VISIBLE
                    } else {
                        if (curruntLayoutIndex == 1) {
                            leaderBoardLayout.visibility = View.GONE
                        } else {
                            chatLayout.visibility = View.GONE
                        }

                        homeContestsListDataContainer.visibility = View.VISIBLE
                        individualDataConstrainLayoutContainer.visibility = View.VISIBLE

                    }
                }
                1 -> {
                    if (curruntLayoutIndex == 0 || curruntLayoutIndex == 3) {
                        individualDataConstrainLayoutContainer.visibility = View.GONE
                        if (curruntLayoutIndex == 0) {
                            homeContestsListDataContainer.visibility = View.GONE
                        } else {
                            profile_top_portion.visibility = View.GONE
                        }
                    } else {
                        chatLayout.visibility = View.GONE
                    }

                    leaderBoardLayout.visibility = View.VISIBLE
                }
                2 -> {

                    Log.e("Chats", "Being called")
                    if (curruntLayoutIndex == 0 || curruntLayoutIndex == 3) {
                        individualDataConstrainLayoutContainer.visibility = View.GONE
                        if (curruntLayoutIndex == 0) {
                            homeContestsListDataContainer.visibility = View.GONE
                        } else {
                            profile_top_portion.visibility = View.GONE
                        }
                    } else {
                        leaderBoardLayout.visibility = View.GONE
                    }

                    chatLayout.visibility = View.VISIBLE
                }
                3 -> {
                    if (curruntLayoutIndex == 0) {
                        homeContestsListDataContainer.visibility = View.GONE
                        profile_top_portion.visibility = View.VISIBLE
                    } else {
                        if (curruntLayoutIndex == 1) {
                            leaderBoardLayout.visibility = View.GONE
                        } else {
                            chatLayout.visibility = View.GONE
                        }

                        profile_top_portion.visibility = View.VISIBLE
                        individualDataConstrainLayoutContainer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    private fun onclickListenersForcontestButtons() {
        hackerrank.setOnClickListener {
            if (contestplatformIndex != 0) {
                loadingDataViewsChangeForContestsList()

                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(0)
                contestplatformIndex = 0
                fetchAndSetContestData()
            }
        }
        codechef.setOnClickListener {
            if (contestplatformIndex != 1) {
                loadingDataViewsChangeForContestsList()

                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(1)
                contestplatformIndex = 1
                fetchAndSetContestData()
            }
        }
        codeforces.setOnClickListener {
            if (contestplatformIndex != 2) {
                loadingDataViewsChangeForContestsList()

                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(2)
                contestplatformIndex = 2
                fetchAndSetContestData()
            }
        }
        topCoder.setOnClickListener {
            if (contestplatformIndex != 3) {
                loadingDataViewsChangeForContestsList()

                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(3)
                contestplatformIndex = 3
                fetchAndSetContestData()
            }
        }
        hacker_earth.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestplatformIndex != 4) {
                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(4)
                contestplatformIndex = 4
                fetchAndSetContestData()
            }
        }
        leetCode.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestplatformIndex != 5) {
                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(5)
                contestplatformIndex = 5
                fetchAndSetContestData()
            }
        }
        atCoder.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestplatformIndex != 6) {
                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(6)
                contestplatformIndex = 6
                fetchAndSetContestData()
            }
        }
        kickStart.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestplatformIndex != 7) {
                turnCurruntNonSelectedForContestList()
                turnToSelectedForContestsList(7)
                contestplatformIndex = 7
                fetchAndSetContestData()
            }
        }

    }

    private fun turnCurruntNonSelectedForContestList() {


        when (contestplatformIndex) {
            0 -> {
                hackerrank.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                hackerrank.background = null
            }
            1 -> {
                codechef.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                codechef.background = null
            }
            2 -> {
                codeforces.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                codeforces.background = null
            }
            3 -> {
                topCoder.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                topCoder.background = null
            }
            4 -> {
                hacker_earth.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.textcolorNonSelected
                    )
                )
                hacker_earth.background = null
            }
            5 -> {
                leetCode.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                leetCode.background = null
            }
            6 -> {
                atCoder.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                atCoder.background = null
            }
            7 -> {
                kickStart.setTextColor(ContextCompat.getColor(this, R.color.textcolorNonSelected))
                kickStart.background = null
            }

        }
    }


    private fun turnToSelectedForContestsList(i: Int) {
        when (i) {
            0 -> {
                hackerrank.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                hackerrank.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            1 -> {
                codechef.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                codechef.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            2 -> {
                codeforces.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                codeforces.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            3 -> {
                topCoder.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                topCoder.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            4 -> {
                hacker_earth.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.whitemain
                    )
                )
                hacker_earth.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            5 -> {
                leetCode.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                leetCode.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            6 -> {
                atCoder.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                atCoder.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }
            7 -> {
                kickStart.setTextColor(ContextCompat.getColor(this, R.color.whitemain))
                kickStart.background = ContextCompat.getDrawable(this, R.drawable.grey_bg)
            }

        }

    }

}
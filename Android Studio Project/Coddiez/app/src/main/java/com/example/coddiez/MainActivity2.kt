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

    //    Declarations

    private lateinit var rankListAdapter: RankLIstAdapter
    private var contestTimingIdex: Int = 0
    private var contestPlatformIndex: Int = 0
    private var currantSelectedPlatformForSelfProgress = 0
    private val individualDataObj = IndividualRankData()
    private var contestBaseUrl: String = "https://kontests.net/api/v1/"
    private var getUrlByHandle = GetUrlByHadle()
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

    private var currantLayoutIndex: Int = 0
    private val userDao = Userdao()

    private var chatDao = ChatDao()

    private val platformsList = ArrayList<platforms_list>()
    private val rankingDataList = ArrayList<rankData>()

    lateinit var mainHandler: Handler
    private lateinit var queueForRankData: RequestQueue

    private var currantTimings = ArrayList<contestTimings>()
    var futureTimings = ArrayList<contestTimings>()

    private var hasBeenClickedBefore = arrayOf(true, false, false, false)
    private val randomAnimeImageUrlObj = GetRandomAnimeImageUrl()








    //    loads initAll() for listeners setup and loads users's username data
//    Also Creates Volley Request queue
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        randomAnimeImageUrlObj.getURL(this)
        chatDao.getUsernameAndGroupID()
        queueForRankData = Volley.newRequestQueue(this)

        initAll()
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateLeaderBoardDataOfAll)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateLeaderBoardDataOfAll)
    }




    //    Do all listener setup stuff
    private fun initAll() {
        fetchAndSetContestData()

        bottomNavSetter()


        contestTimingButtonsSetter()
        onclickListenersForContestButtons()


        personalRankDataRecyclerView.layoutManager = LinearLayoutManager(this)
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        mainHandler = Handler(Looper.getMainLooper())

        setUpOnClickListenersForLeaderBoardDataButtons()



        individualDataObj.loadndividualData(
            this,
            userDao,
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

    //    set up onClick Listener for Chat send btn
    private fun chatsSendOnCLickListnerSetter() {
        msgSendBtn.setOnClickListener {
            if (msgEditText.text.isNotEmpty()) {
                chatDao.sendMsg(msgEditText.text.toString())
                msgEditText.setText("")
            }
        }
    }



    //    set up onClick Listener for forced data refreshing buttons
    private fun forcedRefreshingDataOnClickListenerSetter() {

        personalRankingDataRefreshButton.setOnClickListener {
            personalRankDataRecyclerView.visibility = View.GONE
            forecedRefreshPerosnalData.visibility = View.INVISIBLE
            forecedRefreshingThePersonalDataINProgress.visibility = View.VISIBLE
            individualDataObj.loadndividualData(
                this,
                userDao,
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

                userDao.getAllUsersData(platformsList)
                while (userDao.isWorking) {
                    Thread.sleep(1000)
                    Log.e("wait", "waiting")

                }

                Log.d("Loading Complete", "platform data received\nData : $platformsList")

                rankingDataList.clear()
                val size = platformsList.size


                for (i in 0 until size) {
                    rankingDataList.add(rankData())
                    for (j in 0 until 5) {
                        isWorkGoingOnForRankData++
                        loadRankingDataPlatformWise(getUsernameByIndex(platformsList[i], j), j, i)
                    }
                }

                while (isWorkGoingOnForRankData != 0) {
                    Thread.sleep(4000)
                    Log.e("wait", "waiting for data from volley")
                }

                Log.e("Ranking Data List", "Data is :$rankingDataList")

                userDao.setRankingDataToDatabase(
                    this,
                    rankingDataList,
                    forecedRefreshData,
                    forecedRefreshingTheDataINProgress
                )

                while (userDao.isWorkingForced) {
                    Thread.sleep(500)
                    Log.e("wait", "waiting for confirmation about isworkingForced")
                }

                setUpRankListRecyclerView(curruntSelectedPlatformLeadeBoard)

            }.start()
        }
    }






//    All CONTEST TIMINGS DATA STUFF

    //    listeners setter for contest timing choice buttons ( ongoing || Upcoming )
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
    private fun onclickListenersForContestButtons() {
        hackerrank.setOnClickListener {
            if (contestPlatformIndex != 0) {
                loadingDataViewsChangeForContestsList()

                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(0)
                contestPlatformIndex = 0
                fetchAndSetContestData()
            }
        }
        codechef.setOnClickListener {
            if (contestPlatformIndex != 1) {
                loadingDataViewsChangeForContestsList()

                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(1)
                contestPlatformIndex = 1
                fetchAndSetContestData()
            }
        }
        codeforces.setOnClickListener {
            if (contestPlatformIndex != 2) {
                loadingDataViewsChangeForContestsList()

                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(2)
                contestPlatformIndex = 2
                fetchAndSetContestData()
            }
        }
        topCoder.setOnClickListener {
            if (contestPlatformIndex != 3) {
                loadingDataViewsChangeForContestsList()

                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(3)
                contestPlatformIndex = 3
                fetchAndSetContestData()
            }
        }
        hacker_earth.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestPlatformIndex != 4) {
                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(4)
                contestPlatformIndex = 4
                fetchAndSetContestData()
            }
        }
        leetCode.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestPlatformIndex != 5) {
                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(5)
                contestPlatformIndex = 5
                fetchAndSetContestData()
            }
        }
        atCoder.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestPlatformIndex != 6) {
                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(6)
                contestPlatformIndex = 6
                fetchAndSetContestData()
            }
        }
        kickStart.setOnClickListener {
            loadingDataViewsChangeForContestsList()

            if (contestPlatformIndex != 7) {
                turnCurrentNonSelectedForContestList()
                turnToSelectedForContestsList(7)
                contestPlatformIndex = 7
                fetchAndSetContestData()
            }
        }

    }
    //    change Background of currant selected button of individual data platform buttons to null
    private fun turnCurrentNonSelectedForContestList() {


        when (contestPlatformIndex) {
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
    //    Change background of clicked platform button to selected
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
    //    Loads and set data to the recycler view of contest data by volley request
    private fun fetchAndSetContestData() {


        val queue = Volley.newRequestQueue(this)
        GlobalScope.launch(Dispatchers.IO) {

            try {
                val stringRequest =
                    StringRequest(
                        Request.Method.GET,
                        contestBaseUrl + contestPlatformQuery[contestPlatformIndex],
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
    //    Error Handling in contest data list
    private fun loadingDataViewsChangeForContestsList() {
        if (errorInContestDataLayout.visibility == View.VISIBLE) {
            errorInContestDataLayout.visibility = View.GONE
        }
        contestDetailsList.visibility = View.GONE
        loadingContestData.visibility = View.VISIBLE
    }
    //    setup Recycler VIew According to timings choice
    private fun updatedContestListData() {
        contestDetailsList.layoutManager = LinearLayoutManager(this)
        val ad: contest_list_adapter
        if (contestTimingIdex == 0) {
            if (currantTimings.size != 0) {
                ad = contest_list_adapter(currantTimings.toTypedArray())
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
    //    Devide contest timing data list into upcoming and ongoing list
    private fun devideList(jsonArray: JSONArray) {

        currantTimings.clear()
        futureTimings.clear()


        var tempschedualeObj: contestTimings
        var tempJsonObject: JSONObject
        val lengthObj = jsonArray.length()


        for (i in 0 until lengthObj) {
            tempJsonObject = jsonArray.getJSONObject(i)
            tempschedualeObj = contestTimings(
                tempJsonObject.getString("name"),
                timeFormatterForContestList(tempJsonObject.getString("start_time")),
                timeFormatterForContestList(tempJsonObject.getString("end_time"))
            )
            if (tempJsonObject.getString("status") == "CODING") {
                currantTimings.add(tempschedualeObj)
            } else {
                futureTimings.add(tempschedualeObj)
            }
        }

        Log.d(
            "data division",
            "length of ongoing contests list : ${currantTimings.size}  \n length of future contest list : ${futureTimings.size}"
        )
    }
    //    Format time for loaded data ( form GMT to IST)
    private fun timeFormatterForContestList(string: String): String {

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd-MM-yyyy\nHH:mm", Locale.ENGLISH)
        outputFormat.timeZone = TimeZone.getTimeZone("GMT+11")

        val date: Date = inputFormat.parse(string)!!
        val formattedDate: String = outputFormat.format(date)
        Log.e("TIME CHECK WO O", "Result is :$formattedDate")

        return formattedDate

    }
    //    make list visible when data loaded
    private fun checkAndHandleViews() {

        loadingContestData.visibility = View.GONE

        if (contestDetailsList.visibility == View.GONE) {
            contestDetailsList.visibility = View.VISIBLE
        }
        if (errorInContestDataLayout.visibility == View.VISIBLE) {
            errorInContestDataLayout.visibility = View.GONE
        }
    }
    //    Error handling
    private fun showNoDataAvailableForContest() {
        contestDetailsList.visibility = View.GONE
        errorInContestDataLayout.visibility = View.VISIBLE
    }







//    All INDIVIDUAL DATA STUFF

    //    set Up listeners for self progress data listeners
    private fun individualDataButtonsOnClickListenersSetter() {
        codechefForSelfProgress.setOnClickListener {
            if (currantSelectedPlatformForSelfProgress != 0) {

                Log.e("Individual Data", "Data is :${individualDataObj.codechefDataI}")

                setUpIndividualDataListRecyclerView(0)
                turnCurrentNonSelectedForIndividualData()
                currantSelectedPlatformForSelfProgress = 0
                turnToSelectedForIndividualData()

            }
        }
        codeforcesForSelfProgress.setOnClickListener {
            if (currantSelectedPlatformForSelfProgress != 1) {

                Log.e("Individual Data", "Data is :${individualDataObj.codeForcesDataI}")


                setUpIndividualDataListRecyclerView(1)
                turnCurrentNonSelectedForIndividualData()
                currantSelectedPlatformForSelfProgress = 1
                turnToSelectedForIndividualData()

            }
        }
        spojForSelfProgress.setOnClickListener {

            Log.e("Individual Data", "Data is :${individualDataObj.spojDataI}")


            if (currantSelectedPlatformForSelfProgress != 2) {
                setUpIndividualDataListRecyclerView(2)
                turnCurrentNonSelectedForIndividualData()
                currantSelectedPlatformForSelfProgress = 2
                turnToSelectedForIndividualData()

            }
        }
        interviewBitForSelfProgress.setOnClickListener {
            Log.e("Individual Data", "Data is :${individualDataObj.interviewBitDataI}")

            if (currantSelectedPlatformForSelfProgress != 3) {
                setUpIndividualDataListRecyclerView(3)
                turnCurrentNonSelectedForIndividualData()
                currantSelectedPlatformForSelfProgress = 3
                turnToSelectedForIndividualData()

            }
        }
        leetCodeForSelfProgress.setOnClickListener {

            Log.e("Individual Data", "Data is :${individualDataObj.leetCodeDataI}")


            if (currantSelectedPlatformForSelfProgress != 4) {
                setUpIndividualDataListRecyclerView(4)
                turnCurrentNonSelectedForIndividualData()
                currantSelectedPlatformForSelfProgress = 4
                turnToSelectedForIndividualData()
            }
        }
    }
    //    change Background of currant selected button of individual data platform buttons to null
    private fun turnToSelectedForIndividualData() {
        when (currantSelectedPlatformForSelfProgress) {

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
    //    Change background of clicked platform button to selected
    private fun turnCurrentNonSelectedForIndividualData() {

        when (currantSelectedPlatformForSelfProgress) {
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

    //    Individual Data recycler view setter when platform changed
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







//    ALL LEADERBOARD STUFF

    //    set Up listeners for leaderBoard platform selector
    private fun setUpOnClickListenersForLeaderBoardDataButtons() {
        codechefForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 0) {
                setUpRankListRecyclerView(0)
                turnCurrentNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 0
                turnToSelectedForLeaderBoard()
            }
        }
        codeforcesForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 1) {
                setUpRankListRecyclerView(1)
                turnCurrentNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 1
                turnToSelectedForLeaderBoard()
            }
        }
        spojForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 2) {
                setUpRankListRecyclerView(2)
                turnCurrentNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 2
                turnToSelectedForLeaderBoard()

            }
        }
        interviewBitForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 3) {
                setUpRankListRecyclerView(3)
                turnCurrentNonSelectedForLeaderBoard()
                curruntSelectedPlatformLeadeBoard = 3
                turnToSelectedForLeaderBoard()

            }
        }
        leetCodeForRank.setOnClickListener {
            if (curruntSelectedPlatformLeadeBoard != 4) {
                setUpRankListRecyclerView(4)
                turnCurrentNonSelectedForLeaderBoard()
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
    private fun turnCurrentNonSelectedForLeaderBoard() {
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
    private fun setUpRankListRecyclerView(platformI: Int) {
        Thread {

            if (userDao.compareVol()) {
                userDao.setupLeaderboardCollection()
                while (userDao.compareVol()) {
                    Thread.sleep(1000)
                }
            }

            val collectionForLeaderBoard: CollectionReference =
                userDao.collectionForRankList

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

    //    Task called every 5 minutes to check when last data updating was and if it is more than and hour it updates data
    private val updateLeaderBoardDataOfAll = object : Runnable {
        override fun run() {

            updateLeaderBoardDataOfDatabase()
            mainHandler.postDelayed(this, 300000)
        }
    }
    //    This Function is being called by the task every 5 minutes
    private fun updateLeaderBoardDataOfDatabase() {

        Thread {

            Log.e("Confirmation", "being called")

            userDao.checkDataRefreshingNessecityAndUpdateAcc(platformsList)
            while (userDao.isWorking) {
                Thread.sleep(1000)
                Log.e("wait", "waiting")

            }

            Log.d("Loading Complete", "platform data received\nData : $platformsList")

            if (userDao.nessecityForUpdation) {
                rankingDataList.clear()
                val size = platformsList.size


                for (i in 0 until size) {
                    rankingDataList.add(rankData())
                    for (j in 0 until 5) {
                        isWorkGoingOnForRankData++
                        loadRankingDataPlatformWise(getUsernameByIndex(platformsList[i], j), j, i)
                    }
                }

                while (isWorkGoingOnForRankData != 0) {
                    Thread.sleep(4000)
                    Log.e("wait", "waiting for data from volley")
                }


                userDao.setRankingDataToDatabase(
                    this,
                    rankingDataList,
                    null,
                    null
                )
            }


        }.start()
    }

    //    Fetches the ranking data to uplopad by volley request
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
    //    When fetching user data from apis, to avoid repeatation this function returns username for queries by index and returns by default codeChef username if there is any error
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
    //    to avoid repeatation this function set data to rankData list of Rank data class by index query instead of platform
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
    //    as all platforms have different response for query , to avoid repeatation this is used
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








//    BOTTOM NAV BAR STUFF

    //    Setting Up bottom navigation Bar
    private fun bottomNavSetter() {

        bottomNavigationView.setOnNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.home -> {
                    setCurrentLayout(0)
                    currantLayoutIndex = 0
                }
                R.id.leaderBoard -> {
                    if (!hasBeenClickedBefore[1]) {
                        setUpRankListRecyclerView(0)
                        hasBeenClickedBefore[1] = true
                    }
                    setCurrentLayout(1)
                    currantLayoutIndex = 1

                }
                R.id.chats -> {

                    if (!hasBeenClickedBefore[2]) {
                        setUpChats()
                        hasBeenClickedBefore[2] = true
                    }
                    setCurrentLayout(2)
                    currantLayoutIndex = 2
                }
                R.id.profile -> {

                    if (!hasBeenClickedBefore[3]) {
                        setUpProfileTop()
                        hasBeenClickedBefore[3] = true
                    }


                    setCurrentLayout(3)
                    currantLayoutIndex = 3
                }

            }
            true
        }
    }
    private fun setCurrentLayout(i: Int) {

        if (currantLayoutIndex != i) {
            when (i) {
                0 -> {
                    if (currantLayoutIndex == 3) {
                        profile_top_portion.visibility = View.GONE
                        homeContestsListDataContainer.visibility = View.VISIBLE
                    } else {
                        if (currantLayoutIndex == 1) {
                            leaderBoardLayout.visibility = View.GONE
                        } else {
                            chatLayout.visibility = View.GONE
                        }

                        homeContestsListDataContainer.visibility = View.VISIBLE
                        individualDataConstrainLayoutContainer.visibility = View.VISIBLE

                    }
                }
                1 -> {
                    if (currantLayoutIndex == 0 || currantLayoutIndex == 3) {
                        individualDataConstrainLayoutContainer.visibility = View.GONE
                        if (currantLayoutIndex == 0) {
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
                    if (currantLayoutIndex == 0 || currantLayoutIndex == 3) {
                        individualDataConstrainLayoutContainer.visibility = View.GONE
                        if (currantLayoutIndex == 0) {
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
                    if (currantLayoutIndex == 0) {
                        homeContestsListDataContainer.visibility = View.GONE
                        profile_top_portion.visibility = View.VISIBLE
                    } else {
                        if (currantLayoutIndex == 1) {
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






//    PROFILE STUFF

    //    Updates profile data
    private fun setUpProfileTop() {
        Log.e("Called", "thread has been called00")

        Thread {

            Log.e("Called", "thread has been called")
            runOnUiThread {
                Glide
                    .with(this)
                    .load(randomAnimeImageUrlObj.url)
                    .centerCrop()
                    .error(R.color.whitesecondary)
                    .placeholder(R.color.whitesecondary)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(personProfilePicture)

            }

            if (userDao.mainUsr == null) {
                userDao.getUserDataByIDAndSet(null)
            }

            while (userDao.isworkingForGettingUserData) {
                Thread.sleep(500)
            }

            Log.e("User Data Received", "User : ${userDao.mainUsr}")


            runOnUiThread {
                val tempUsr = userDao.mainUsr!!
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
                courseNameView.setTextColor(userDao.randomColorObj.getRnd())
                instituteNameView.setTextColor(userDao.randomColorObj.getRnd())
                courseNameView.text = tempUsr.courseName
                instituteNameView.text = tempUsr.instituteName

            }
        }.start()

    }
    //    Set on click listeners to the profile link buttons and
    private fun setUpOnCLickByUserName(btnGiven: ImageButton, handleGiven: String, i : Int) {

        Log.e("Handle", "handle provided for $btnGiven is \n $handleGiven")
        if (handleGiven!=""&&handleGiven!=" ") {
            btnGiven.visibility = View.VISIBLE
            btnGiven.setOnClickListener {
                val openURL = Intent(android.content.Intent.ACTION_VIEW)
                openURL.data = Uri.parse(getUrlByHandle.getLink(handleGiven, i))
                startActivity(openURL)
            }
        }
    }




    private fun setUpChats() {
        chatDao.setUpChatRecyclerView(chatsRecyclerView)

    }



}
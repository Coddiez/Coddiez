package com.example.coddiez.daos

import android.content.Context
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

class IndividualRankData {

    var codechefDataI: MutableMap<String, Any> = mutableMapOf(
        "rating" to "0",
        "stars" to "1★",
        "highest_rating" to "0",
        "global_rank" to "0",
        "country_rank" to "0",
    )
    var codeForcesDataI: MutableMap<String, Any> = mutableMapOf(
        "rating" to "0",
        "max rating" to "0",
        "rank" to "newbie",
        "max rank" to "newbie"
    )
    var spojDataI: MutableMap<String, Any> = mutableMapOf(
        "points" to "0",
        "rank" to "0"
    )
    var interviewBitDataI: MutableMap<String, Any> = mutableMapOf(
        "rank" to "0",
        "score" to "0"
    )
    var leetCodeDataI: MutableMap<String, Any> = mutableMapOf(
        "ranking" to "0",
        "total_problems_solved" to "0",
        "acceptance_rate" to "0%",
        "easy_questions_solved" to "0",
        "total_easy_questions" to "0",
        "medium_questions_solved" to "0",
        "total_medium_questions" to "0",
        "hard_questions_solved" to "0",
        "total_hard_questions" to "0",
        "contribution_points" to "0",
        "contribution_problems" to "0",
        "contribution_testcases" to "0",
        "reputation" to "0"
    )

    var dataStatus = arrayOf(false, false, false, false, false)


    fun loadndividualData(
        context: Context,
        userdao: Userdao,
        rankingsBaseUrl: String,
        rankingPlatfomQuery: Array<String>,
        personalRankDataRecyclerView: RecyclerView,
        forecedRefreshPerosnalData: LinearLayout,
        forecedRefreshingThePersonalDataINProgress: LinearLayout,

    ) {
        userdao.loadndividualDataAndPutItHere(
            context,
            this,
            rankingsBaseUrl,
            rankingPlatfomQuery,
            personalRankDataRecyclerView,
            forecedRefreshPerosnalData,
            forecedRefreshingThePersonalDataINProgress,

        )
    }


}
package com.example.coddiez.models

class GetUrlByHadle {

    val baseUrl = arrayOf(
        "https://codeforces.com/profile/",
        "https://www.codechef.com/users/",
        "https://www.spoj.com/users/",
        "https://www.interviewbit.com/profile/",
        "https://leetcode.com/",
        "https://www.hackerrank.com/",
        "https://github.com/",
        "https://www.linkedin.com/in/",
        "https://www.instagram.com/",
        "https://www.facebook.com/"
    )

    fun getLink(handle:String, index:Int): String {
        return baseUrl[index] + handle + "/"
    }


}
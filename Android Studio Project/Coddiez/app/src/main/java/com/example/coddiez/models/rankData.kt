package com.example.coddiez.models

class rankData(

    var codechefData: Int = 0,
    var codeforcesData: Int = 0,
    var spjData: Double = 0.00,
    var interviewbitData: Int = 0,
    var leetcodeData: Int = 0
) {
    override fun toString(): String {
        return "codechefData : ${this.codechefData}\n " +
                "codeforces : ${this.codeforcesData}\n" +
                "Spoj : ${this.spjData}\n" +
                "interviewBit : ${this.interviewbitData}\n" +
                "leetcode : ${this.leetcodeData}\n"
    }


}



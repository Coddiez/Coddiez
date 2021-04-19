package com.example.coddiez.models

class platforms_list(
    var codechef: String = "",
    var codeforces: String = "",
    var spoj: String = "",
    var interviewBit: String = "",
    var leetCode: String = ""

){
    override fun toString(): String {
        return "[ CodeChef : ${this.codechef} \nCodeForces : ${this.codeforces}  \nSpoj : ${this.spoj} \nInterviewBit : ${this.interviewBit} \nLeetCode : ${this.leetCode}\n ]"
    }
}
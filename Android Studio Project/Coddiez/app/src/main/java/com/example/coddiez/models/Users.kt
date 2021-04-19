package com.example.coddiez.models

class Users (
    var userId :String="",
    var courseName :String= "",
    var instituteName :String = "",
    var joiningYear:Int = 0,
    var displayName :String="",
    var codeChefHandle:String="",
    var codeForcesHandle:String="",
    var hackerRankHandle:String="",
    var spojHandle:String="",
    var interviewBitHandle:String="",
    var leetCodeHandle:String="",
    var gitHubHandle:String="",
    var linkedInHandle:String="",
    var instagramHandle:String="",
    var facebookHandle:String="",
    var rankData: rankData = rankData()
){
    override fun toString(): String {
        return " var userId :$userId " +
                "    var courseName :$courseName " +
                "    var instituteName :$instituteName " +
                "    var joiningYear:$joiningYear" +
                "    var displayName :$displayName " +
                "    var codeChefHandle:$codeChefHandle" +
                "    var codeForcesHandle:$codeForcesHandle" +
                "    var hackerRankHandle:$hackerRankHandle" +
                "    var spojHandle:$spojHandle" +
                "    var interviewBitHandle:$interviewBitHandle" +
                "    var leetCodeHandle:$leetCodeHandle" +
                "    var gitHubHandle:$gitHubHandle" +
                "    var linkedInHandle:$linkedInHandle" +
                "    var instagramHandle:$instagramHandle" +
                "    var facebookHandle:$facebookHandle" +
                "    var rankData: $rankData"
    }
}
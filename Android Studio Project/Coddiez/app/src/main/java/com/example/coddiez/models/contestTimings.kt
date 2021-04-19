package com.example.coddiez.models

class contestTimings(
    var contestName: String = "",
    var startTime: String = "",
    var endTime: String = ""
) {
    override fun toString(): String {
        return " [name: ${this.contestName}, start: ${this.startTime}, end: ${this.endTime}]"
    }
}
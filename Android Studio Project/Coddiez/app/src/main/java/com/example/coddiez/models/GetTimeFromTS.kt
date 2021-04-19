package com.example.coddiez.models

import java.text.SimpleDateFormat
import java.util.*

class GetTimeFromTS {

     fun getTimeFromTS(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

}
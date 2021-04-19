package com.example.coddiez.models

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GetRandomAnimeImageUrl {

    val imageQueryArr = arrayOf(
        "waifu",
        "neko",
        "shinobu",
        "megumin",
        "bully",
        "cuddle",
        "cry",
        "hug",
        "awoo",
        "kiss",
        "lick",
        "pat",
        "smug",
        "bonk",
        "yeet",
        "blush",
        "smile",
        "wave",
        "smile",
        "wave",
        "highfive",
        "handhold",
        "nom",
        "bite",
        "glomp",
        "kill",
        "slap",
        "happy",
        "wink",
        "poke",
        "dance",
        "cringe",
        "blush"
    )

    var url = ""
    val size = imageQueryArr.size


    public fun getURL(context: Context) {
        var queue = Volley.newRequestQueue(context)
        GlobalScope.launch(Dispatchers.IO) {

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                "https://waifu.pics/api/sfw/" + imageQueryArr[(0 until size).random()],
                null,
                { response ->
                    if (!response.isNull("url")) {
                        url = response.get("url").toString()
                        Log.e("Response", "Received resonse is $response \n url is ${url}")
                    }

                },
                { error ->
                    Log.e("Error", "Error is $error ")

                })

            queue.add(jsonObjectRequest)
        }

    }
}


package com.nabeel.chatapp

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

data class Message(val message: String, val author: String?, val time: String) {
    companion object {
        fun from(map: HashMap<String, String>) = object {
            val author by map
            val time by map
            val message by map

            val data = Message(message, author, time)
        }.data
    }
}

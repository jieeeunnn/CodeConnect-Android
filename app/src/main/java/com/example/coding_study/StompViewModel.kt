package com.example.coding_study

import android.util.Log
import androidx.lifecycle.ViewModel
import ua.naiksoftware.stomp.StompClient

class StompViewModel: ViewModel() {
    private var stompClient: StompClient? = null

    fun setStompClient(client: StompClient?) {
        stompClient = client
        Log.e("StompViewModel setStompClient", "$stompClient")
    }

    fun getStompClient(): StompClient? {
        Log.e("StompViewModel getStrompCllient return stomp", "$stompClient")
        return stompClient
    }
}
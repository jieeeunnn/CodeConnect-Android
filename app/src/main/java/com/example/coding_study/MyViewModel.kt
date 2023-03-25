package com.example.coding_study

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Item(val nickname: String, val title: String, val content: String, val field: String, val num: String)

class MyViewModel : ViewModel() {
    val items = ArrayList<Item>()
    val itemsLiveData = MutableLiveData<ArrayList<Item>>()

    var clickedItem : Int = -1
    val itemClickEvent = MutableLiveData<Int>()

    init {
        addItem(
            Item("Hansung", "한성대입구역 스터디 구합니다",
            "이번주 토요일 오후에 안드로이드 스터디 하실 분들 구합니다!", "안드로이드", "4")
        )
    }
    fun addItem(item: Item) {
        items.add(item)
        itemsLiveData.value = items
    }

    fun updateItem(item: Item, pos: Int) {
        items[pos] = item
        itemsLiveData.value = items
    }
}
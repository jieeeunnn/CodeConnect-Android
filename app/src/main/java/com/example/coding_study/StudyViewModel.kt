package com.example.coding_study

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 게시글의 정보를 담는 데이터 클래스 Post
data class Post(
    var nickname: String,
    var title: String,
    var content: String,
    var num: Int,
    var field: String,
    var currentTime: String
)

class StudyViewModel : ViewModel() {

    val postList = MutableLiveData<List<Post>??>() // 게시글 목록을 저장하는 MutableLiveData. 이 리스트는 null일 수 있음
    val onPostAdded = MutableLiveData<Post>()

    init {
        postList.value = mutableListOf() // postList를 빈 리스트로 초기화
        //addPost(Post("hansung", "스터디 모집", "스터디 모집합니다", 5, "안드로이드", "2023.04.06"))
    }


    //게시글을 추가하는 함수
    fun addPost(post: Post) {
        val list = postList.value?.toMutableList() ?: mutableListOf() // null이면 빈 리스트로 초기화
        list.add(post)
        postList.value = list // 데이터 목록을 업데이트
        onPostAdded.value = post // 새로운 데이터가 추가되었음을 알리기 위한 목적
    }

/*
    val postAdded = MutableLiveData<Unit>()
    fun onPostAdded(post: Post) {
        val posts = postList.value?.toMutableList() ?: mutableListOf()
        posts.add(post)
        postList.value = posts
        postAdded.value = Unit
    }
 */




    /*
    val items = ArrayList<Item>()
    val itemsLiveData = MutableLiveData<ArrayList<Item>>()

    var clickedItem: Int = -1
    val itemClickEvent = MutableLiveData<Int>()

    // MutableLiveData
    private val _myData = MutableLiveData<String>()
    val myData: LiveData<String> get() = _myData // 외부에 노출될 LiveData


    fun addItem(item: Item) {
        items.add(item)
        itemsLiveData.value = items
    }

    fun updateItem(item: Item, pos: Int) {
        items[pos] = item
        itemsLiveData.value = items
    }

    init {
        // LiveData 관찰 코드
        myData.observeForever { data ->
            // myData가 변경될 때 수행할 작업
            val item = Item("nickname", "title", "content", "field", "num")
            addItem(item)
            /*
        addItem(
            Item("Hansung", "한성대입구역 스터디 구합니다",
            "이번주 토요일 오후에 안드로이드 스터디 하실 분들 구합니다!", "안드로이드", "4")
        )
             */
        }
    }

     */
}
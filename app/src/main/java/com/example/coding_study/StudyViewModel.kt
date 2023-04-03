package com.example.coding_study

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//data class Item(val nickname: String, val title: String, val content: String, val field: String, val num: String)
// 게시글의 정보를 담는 데이터 클래스 Post
data class Post(
    var nickname: String,
    var title: String,
    var content: String,
    var num: Int,
    var field: String,
    var currentTime: String
)
// 시간을 원하는 형식으로 표시하기
//val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
//val formattedTime = dateFormat.format(currentTime)

class StudyViewModel : ViewModel() {

    val postList = MutableLiveData<List<Post>??>() // 게시글 목록을 저장하는 MutableLiveData. 이 리스트는 null일 수 있음

    init {
        postList.value = mutableListOf() // postList를 빈 리스트로 초기화
    }

    //게시글을 추가하는 함수
    fun addPost(post: Post) {
        val list = postList.value?.toMutableList()
        list?.add(post)
        postList.value = list
    }

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
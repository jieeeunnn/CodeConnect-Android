package com.example.coding_study

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyPageViewModel : ViewModel() {
    private val myProfileData = MutableLiveData<MyProfile>()

    fun setMyProfile(myProfile: MyProfile) {
        myProfileData.value = myProfile
    }

    fun getMyProfile(): LiveData<MyProfile> {
        return myProfileData
    }
}

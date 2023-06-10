package com.example.coding_study.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyPageAddressViewModel : ViewModel() {
    private val selectedAddress = MutableLiveData<String>()

    fun selectAddress(address:String) {
        selectedAddress.value = address
    }

    fun getSelectedAddress() : LiveData<String> {
        return selectedAddress
    }
}
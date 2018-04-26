package com.rukiasoft.codewars.ui.search

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import com.google.gson.Gson
import com.rukiasoft.codewars.persistence.entities.UserInfo
import com.rukiasoft.codewars.repository.UserInfoError
import com.rukiasoft.codewars.repository.UserInfoRequests
import com.rukiasoft.codewars.utils.Constants
import com.rukiasoft.codewars.vo.Resource
import com.rukiasoft.codewars.vo.Status
import timber.log.Timber
import javax.inject.Inject


class SearchViewModel @Inject constructor(private val userInfoRequests: UserInfoRequests) : ViewModel() {

    var animateFab: MutableLiveData<Boolean> = MutableLiveData()
    var searchCardVisible = ObservableBoolean()

    var userInfo: MediatorLiveData<Resource<Void>> = MediatorLiveData()

    init {
        searchCardVisible.set(false)
        animateFab.value = false
    }

    fun search(name: String) {
        val info = userInfoRequests.downloadUserInfo(name, Constants.DEFAULT_NUMBER_OF_RETRIES)
        userInfo.addSource(info, {
            it?.let {response ->
                when (response.status) {

                    Status.SUCCESS -> {
                        userInfo.value = Resource.success(null)
                        //userInfo.removeSource(info)
                    }
                    Status.ERROR -> {
                        val message = try {
                            val userError: UserInfoError = Gson().fromJson(response.message, UserInfoError::class.java)
                            userError.reason
                        }catch (e: Exception){
                            ""
                        }
                        userInfo.value = Resource.error(message,null)
                        userInfo.removeSource(info)
                    }
                    Status.LOADING -> {
                        userInfo.value = Resource.loading(null)
                    }
                }
            }
        })
    }



}
package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.users.FollowFollowerUser
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.view.SafeUnbox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

class FollowFollowerViewModel(
    val accountRelation: AccountRelation,
    val misskeyAPI: MisskeyAPI,
    val user: User?,
    val type: Type,
    private val encryption: Encryption
) : ViewModel(){
    @Suppress("UNCHECKED_CAST")
    class Factory(
        val accountRelation: AccountRelation,
        val miApplication: MiApplication,
        val user: User?,
        val type: Type,
        val encryption: Encryption
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val misskeyAPI = miApplication.getMisskeyAPI(accountRelation)!!
            if(modelClass == FollowFollowerViewModel::class.java){
                return FollowFollowerViewModel(
                    accountRelation,
                    misskeyAPI,
                    user,
                    type,
                    encryption
                ) as T
            }
            throw IllegalArgumentException("use FollowFollowerViewModel::class.java")
        }
    }

    enum class Type{
        FOLLOWING,
        FOLLOWER
    }

    val tag = "FollowFollowerViewModel"

    val userId = user?.id?: accountRelation.account.id

    val store = if(type == Type.FOLLOWER){
        misskeyAPI::followers
    }else{
        misskeyAPI::following
    }
    val streamingAdapter: StreamingAdapter by lazy {
        StreamingAdapter(accountRelation.getCurrentConnectionInformation(), encryption).apply{
            val mainCapture = MainCapture(GsonFactory.create())
            mainCapture.addListener(Listener())
            addObserver(UUID.randomUUID().toString(), mainCapture)
        }

    }
    val isInitializing = MutableLiveData<Boolean>(false)

    val followFollowerViewDataList = object : MutableLiveData<List<FollowFollowerViewData>>(){
        override fun onActive() {
            streamingAdapter.connect()
        }

        override fun onInactive() {
            streamingAdapter.disconnect()
        }
    }
    private var mIsLoading: Boolean = false

    fun loadInit(){
        viewModelScope.launch(Dispatchers.IO){
            if(mIsLoading){
                return@launch
            }
            mIsLoading = true
            isInitializing.postValue(true)
            val request = RequestUser(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                userId = userId,
                limit = 30
            )
            val response = store(request).execute()
            val viewDataList = response.body()?.toViewDataList()
            if(response.code() != 200 || viewDataList == null){
                mIsLoading = false
                isInitializing.postValue(false)
                Log.d(tag, "FollowFollowerViewModel, error, status:${response.code()}, errMsg:${response.errorBody()?.string()}")
                return@launch
            }

            followFollowerViewDataList.postValue(viewDataList)
            isInitializing.postValue(false)
            mIsLoading = false
        }
    }



    fun loadOld(){
        viewModelScope.launch(Dispatchers.IO){
            if(mIsLoading){
                return@launch
            }
            mIsLoading = true
            val untilId = followFollowerViewDataList.value?.lastOrNull()?.id
            if(untilId == null){
                mIsLoading = false
                return@launch loadInit()
            }
            val request = RequestUser(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                untilId = untilId,
                userId = userId,
                limit = 30
            )

            val response = store(request).execute()
            val tmpViewDataList = response.body()?.toViewDataList()
            if(response.code() != 200 || tmpViewDataList == null){
                mIsLoading = false
                Log.d(tag, "FollowFollowerViewModel, error, status:${response.code()}, errMsg:${response.errorBody()?.string()}")
                return@launch
            }

            followFollowerViewDataList.postValue(followFollowerViewDataList.value.toArrayList().apply{
                addAll(tmpViewDataList)
            })
            mIsLoading = false
        }
    }

    private inner class Listener : MainCapture.AbsListener(){
        override fun follow(user: User) {
            followFollowerViewDataList.value?.forEach{
                if(it.user.id == user.id){
                    it.isFollowing.postValue(true)
                }
            }
        }

        override fun unFollowed(user: User) {
            followFollowerViewDataList.value?.forEach {
                if(it.user.id == user.id){
                    it.isFollowing.postValue(false)
                }
            }
        }
    }

    fun followOrUnfollow(followFollowerViewData: FollowFollowerViewData){

        if(SafeUnbox.unbox(followFollowerViewData.isFollowing.value)){
            viewModelScope.launch(Dispatchers.IO){
                misskeyAPI.unFollowUser(RequestUser(
                    i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                    userId = followFollowerViewData.user.id
                )).execute()
            }
        }else{
            viewModelScope.launch(Dispatchers.IO){
                misskeyAPI.followUser(RequestUser(
                    i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                    userId = followFollowerViewData.user.id
                )).execute()
            }

        }

    }

    val showUserEventBus = EventBus<User>()
    fun showUser(followFollowerViewData: FollowFollowerViewData){
        showUserEventBus.event = followFollowerViewData.user
    }

    private fun List<FollowFollowerViewData>?.toArrayList() : ArrayList<FollowFollowerViewData>{
        return if(this == null){
            ArrayList()
        }else{
            ArrayList(this)
        }
    }

    private fun List<FollowFollowerUser>.toViewDataList() : List<FollowFollowerViewData>{
        return this.map{
            FollowFollowerViewData(it)
        }
    }
}
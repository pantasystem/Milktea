package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.MediatorLiveData
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.users.User

open class UsersLiveData : MediatorLiveData<List<UserViewData>>(){

    private var mMainCapture: MainCapture? = null

    override fun onActive() {
        super.onActive()

        mMainCapture?.putListener(listener)
    }
    override fun onInactive() {
        super.onInactive()

        mMainCapture?.removeListener(listener)
    }
    fun setMainCapture(mainCapture: MainCapture){
        mMainCapture?.removeListener(listener)
        mMainCapture = mainCapture
        mainCapture.putListener(listener)
    }

    val listener = object : MainCapture.AbsListener(){

        override fun follow(user: User) {
            super.follow(user)

            value?.forEach {
                if(it.userId == user.id){
                    it.user.postValue(user)
                }
            }
        }

        override fun unFollowed(user: User) {
            value?.forEach {
                if(it.userId == user.id){
                    it.user.postValue(user)
                }
            }
        }
    }

}
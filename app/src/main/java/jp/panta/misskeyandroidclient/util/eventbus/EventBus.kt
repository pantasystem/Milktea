package jp.panta.misskeyandroidclient.util.eventbus

import android.os.Handler
import android.util.Log
import androidx.arch.core.internal.SafeIterableMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

class EventBus <T>(val limitMilliTime: Long = 500){
    private val mHandler = Handler()

    private var isLimiting: Boolean = false

    private val mObservers = HashMap<Observer<T>, ObserverWrapper>()

    var event: T? = null
        set(value){
            field = value
            handleEvent(value)
        }


    fun observe(lifecycleOwner: LifecycleOwner,observer: Observer<T>){
        val wrapper = ObserverWrapper(lifecycleOwner, observer)
        synchronized(mObservers){
            //mObservers[observer] = wrapper
            val beforeObserver = mObservers[observer]
            mObservers[observer] = wrapper
            if(beforeObserver == null){
                lifecycleOwner.lifecycle.addObserver(wrapper)
            }
        }
    }

    fun removeObserver(observer: Observer<T>){
        val removed = mObservers[observer]
        if(removed != null){
            removed.onStateChanged(removed.lifecycleOwner, Lifecycle.Event.ON_DESTROY)
        }
    }

    /*fun postEvent(e: T?){
        event = e
        //handleEvent(e)
    }*/

    private fun handleEvent(e: T?){
        Log.d("EventBus", "handleEventが呼び出された")
        synchronized(mObservers){
            val iterator = mObservers.iterator()

            while(iterator.hasNext()){
                val next = iterator.next().value
                when {
                    next.isActive() -> {
                        /*if(!isLimiting){
                            isLimiting = true
                            next.observer.onChanged(e)
                            //mHandler.removeCallbacks(limiterRelease)
                            //mHandler.postDelayed(limiterRelease, limitMilliTime)
                        }else{
                            Log.d("EventBus", "リミッター制限中")
                        }*/
                        next.observer.onChanged(e)

                    }
                    /*next.lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED) -> {

                        iterator.remove()
                    }*/
                    else -> {

                    }
                }
            }
        }

    }

    private inner class ObserverWrapper(val lifecycleOwner: LifecycleOwner, val observer: Observer<T>) : LifecycleEventObserver{
        fun isActive(): Boolean{
            return lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            synchronized(mObservers){
                if(lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED){
                    val removed = mObservers.remove(observer)
                    Log.d("EventBus", "removed: ${removed != null}")
                    removed?.lifecycleOwner?.lifecycle?.removeObserver(this)
                }
            }

        }
    }

    private val limiterRelease = Runnable{
        isLimiting = false
    }
}
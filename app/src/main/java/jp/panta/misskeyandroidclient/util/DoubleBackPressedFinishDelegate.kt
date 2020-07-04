package jp.panta.misskeyandroidclient.util

import android.os.Handler
import java.util.concurrent.atomic.AtomicInteger

class DoubleBackPressedFinishDelegate {

    private val mHandler = Handler()

    private var mCounter = 0


    fun back(): Boolean{
        return if(++mCounter >= 2){
            true
        }else{
            mHandler.removeCallbacks(mCounterTimeReset)
            mHandler.postDelayed(mCounterTimeReset, 2000)
            false
        }
    }

    private val mCounterTimeReset = Runnable { mCounter = 0 }


}
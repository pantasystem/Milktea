package jp.panta.misskeyandroidclient.view

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*

class SimpleElapsedTime(val getString: (TimeUnit)-> String) {

    enum class TimeUnit{
        YEAR, MONTH, DATE,
        HOUR, MINUTE, SECOND, FUTURE, NOW
    }

    // 送られてくる時間はUS
    fun format(date: Instant): String{

        val nowDate = Date()
        val epoch = date.toEpochMilliseconds()
        val nowEpoch = Clock.System.now().toEpochMilliseconds()


        return when(val elapsedMilliTime = nowEpoch - epoch){
            in Long.MIN_VALUE until 5 * 1000 ->{
                // 5秒未満
                getString.invoke(TimeUnit.FUTURE)
            }
            in 5 * 1000 until 10 * 1000 ->{
                // 5秒以上 10秒未満
                getString.invoke(TimeUnit.NOW)
            }
            in 10 * 1000 until 6 * 10 * 1000 ->{
                // 10秒以上 1分未満
                (elapsedMilliTime / 1000).toString() + getString.invoke(TimeUnit.SECOND)
            }
            in 6 * 10 * 1000 until 60 * (6 * 10 * 1000) ->{
                // 1分以上 60分未満
                (elapsedMilliTime / (6 * 10 * 1000)).toString() + getString.invoke(TimeUnit.MINUTE)
            }
            in 1 * (60 * (6 * 10 * 1000)) until 24 * (60 * (6 * 10 * 1000)) ->{
                // 1時間以上 24時間未満
                (elapsedMilliTime / (1 * (60 * (6 * 10 * 1000)))).toString() + getString.invoke(TimeUnit.HOUR)
            }
            in 1 * (24 * (60 * (6 * 10 * 1000))) until 30 * (24 * (60 * (6 * 10 * 1000L))) ->{
                // 1日以上 30日未満
                (elapsedMilliTime / (1 * (24 * (60 * (6 * 10 * 1000))))).toString() + getString.invoke(TimeUnit.DATE)
            }
            in 30 * (24 * (60 * (6 * 10 * 1000L))) until  365 * (24 * (60 * (6 * 10 * 1000L))) ->{
                // 30日以上 365未満
                (elapsedMilliTime / (30 * (24 * (60 * (6 * 10 * 1000L))))).toString() + getString.invoke(TimeUnit.MONTH)
            }
            else ->{
                // 365日以上
                (elapsedMilliTime / (365 * (24 * (60 * (6 * 10 * 1000L))))).toString() + getString.invoke(TimeUnit.YEAR)
            }
        }
    }
}
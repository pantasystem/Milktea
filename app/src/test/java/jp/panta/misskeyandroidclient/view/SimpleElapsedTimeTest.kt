package jp.panta.misskeyandroidclient.view

import jp.panta.misskeyandroidclient.ui.SimpleElapsedTime
import kotlinx.datetime.Clock
import org.junit.Test

import org.junit.Before
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class SimpleElapsedTimeTest {

    private lateinit var simple: SimpleElapsedTime

    @Before
    fun init(){
        simple = SimpleElapsedTime{
            when(it){
                SimpleElapsedTime.TimeUnit.YEAR -> "年前"
                SimpleElapsedTime.TimeUnit.MONTH -> "ヶ月前"
                SimpleElapsedTime.TimeUnit.DATE -> "日前"
                SimpleElapsedTime.TimeUnit.HOUR -> "時間前"
                SimpleElapsedTime.TimeUnit.MINUTE -> "分前"
                SimpleElapsedTime.TimeUnit.SECOND -> "秒前"
                SimpleElapsedTime.TimeUnit.NOW -> "今"
                SimpleElapsedTime.TimeUnit.FUTURE -> "未来"
            }
        }
    }

    @Test
    fun futureFormatTest() {

        val instant = Clock.System.now()

        assert(simple.format(instant) == "未来")

    }

    @ExperimentalTime
    @Test
    fun nowTest(){

        val nowTest = Clock.System.now().plus((-9).days)
        assert(simple.format(nowTest) == "今")

    }
/*
    @Test
    fun secondTest(){
        val secondMin = Calendar.getInstance().apply{
            add(Calendar.SECOND, - 10)
        }.time
        val f =simple.format(secondMin)
        println(f)
        assert(f.endsWith("秒前"))

        val secondMax = Calendar.getInstance().apply{
            add(Calendar.SECOND, - 59)
        }.time
        val f2 = simple.format(secondMax)
        println(f2)
        assert(f2.endsWith("秒前"))

    }

    /*@Test
    fun minuteTest(){
        val minuteMin = Calendar.getInstance().apply{
            add(Calendar.SECOND, - 60)
        }.time
        val f = simple.format(minuteMin)
        println(f)
        assert(f.endsWith("1分前"))

        val minuteMax = Calendar.getInstance().apply{
            add(Calendar.MINUTE, - 59)
        }.time
        val f2 = simple.format(minuteMax)
        println(f2)
        assert(f2 == "59分前")
    }
*/
    @Test
    fun hourTest(){
        val hourMin = Calendar.getInstance().apply{
            add(Calendar.MINUTE, - 60)
        }.time
        val f = simple.format(hourMin)
        println(f)
        assert(f == "1時間前")

        val hourMax = Calendar.getInstance().apply{
            add(Calendar.HOUR, - 23 )
        }.time
        val f2 = simple.format(hourMax)
        println(f2)
        assert(f2 == "23時間前")
    }

    @Test
    fun dateTest(){
        val dateMin = Calendar.getInstance().apply{
            add(Calendar.HOUR,- 24)
        }.time
        val f = simple.format(dateMin)
        println(f)
        assert(f == "1日前")

        val dateMax = Calendar.getInstance().apply{
            add(Calendar.DATE, - 29)
        }.time
        val f2 = simple.format(dateMax)
        println(f2)
        assert(f2 == "29日前")
    }

    @Test
    fun monthTest(){
        val monthMin = Calendar.getInstance().apply{
            add(Calendar.DATE, - 30)
        }.time
        val f = simple.format(monthMin)
        println(f)
        assert(f == "1ヶ月前")

        val monthMax = Calendar.getInstance().apply{
            add(Calendar.MONTH, - 12)
        }.time
        val f2 = simple.format(monthMax)
        println(f2)

    }

*/

}
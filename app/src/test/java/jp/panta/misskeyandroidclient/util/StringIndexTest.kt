package jp.panta.misskeyandroidclient.util

import org.junit.jupiter.api.Test


class StringIndexTest {

    @Test
    fun substringTest(){
        val text = "0123456789"
        println(text.substring(0, 9))
        println(text.substring(0, text.length))
    }

    @Test
    fun whileText(){
        var counter = 0
        while(counter < 10){
            println(counter)
            counter++
        }
        println("終了後:$counter")
    }

    @Test
    fun emojiCharTest(){
        val text = "☺️"
        println(text.length)
        text.forEach{
            println(it)
        }
    }
}
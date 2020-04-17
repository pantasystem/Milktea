package jp.panta.misskeyandroidclient.mfm

import org.junit.Assert.*
import org.junit.Test

class MFMParserTest{

    @Test
    fun mfmLessSimpleText(){
        val node = MFMParser.parse("Hello world")
        println(node)
    }

    @Test
    fun quoteTest(){
        val text = "> Hello quote\n\r>> Hello world"
        println(text)
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun quoteEmptyTest(){
        val text = ">\n"
        println(text)
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun doubleSpaceTest(){
        val text = ">  "
        println(text)
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun testBlock(){
        val text = "<i>test<small>小さい文字のテスト</small></i>"
        println(text)
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun testBlockInQuoteTest(){
        val text = "<i>\n> must error\n </i>"
        println(text)
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun testSameBlockNest(){
        val text = "<i><i>error</i></i>"
        val node = MFMParser.parse(text)
        println(node)
    }


}
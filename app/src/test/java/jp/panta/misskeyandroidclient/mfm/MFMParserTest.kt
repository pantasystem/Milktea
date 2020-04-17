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
}
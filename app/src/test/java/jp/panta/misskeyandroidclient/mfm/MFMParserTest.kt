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
        val text = "> Hello quote\n>>>>> Hello world"
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


    @Test
    fun testStrike(){
        val text = "~~strike~~ <i>~~nest strike~~</i>"
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun testBold(){
        val text = "**bold** ~~**bold strike**~~ **bold**"
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun testCode(){
        val text = "```code<i>aaaa</i>```"
        val node = MFMParser.parse(text)
        println(node)
    }

    @Test
    fun testTitle(){
        val text = "[title]\n【title2】\n"
        println(MFMParser.parse(text))
    }

    @Test
    fun search1Test(){
        val text = "検索実装できた？ Search"
        println(MFMParser.parse(text))
    }

    @Test
    fun search2Test(){
        val text = "hogehogehoge~~~\n検索実装できた？ [検索]\n testtest"
        println(MFMParser.parse(text))
    }

    @Test
    fun linkTest(){
        val text = "awefawef[みすきーあいおーはこちら](https://misskey.io)awefwaef"
        println(MFMParser.parse(text))
    }

    @Test
    fun linkTest2(){
        val text = "> <i>[みすきーあいおーはこちら](https://misskey.io)italic</i>quote\n root"
        println(MFMParser.parse(text))
    }
}
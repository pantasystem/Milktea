package jp.panta.misskeyandroidclient.mfm

import net.pantasystem.milktea.common_android.mfm.ElementType
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.model.emoji.Emoji
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

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
        val root = MFMParser.parse(text)
        Assertions.assertNotNull(root)
        var node: Node? = root!!.childElements[1] as Node
        for (i in 0 until 4) {
            node = node!!.childElements[0] as Node
            Assertions.assertEquals(ElementType.QUOTE, node.elementType)
        }
        //assertEquals(ElementType.TEXT, node?.elementType)
        Assertions.assertEquals(ElementType.TEXT, node!!.childElements[0].elementType)
        println("node:$node")
    }

    @Test
    fun quoteEmptyTest(){
        val text = ">\n"
        println(text)
        val node = MFMParser.parse(text)
        val n = node!!.childElements[0]
        Assertions.assertEquals(ElementType.TEXT, n.elementType)
        println(node)
    }

    @Test
    fun doubleSpaceTest(){
        val text = ">  "
        println(text)
        val node = MFMParser.parse(text)
        Assertions.assertEquals(ElementType.QUOTE, node!!.childElements[0].elementType)
        println(node)
    }

    @Test
    fun testBlock(){
        val text = "<i>test<small>小さい文字のテスト</small></i>"
        println(text)
        val node = MFMParser.parse(text)
        val italic = node!!.childElements[0] as Node
        Assertions.assertEquals(ElementType.ITALIC, italic.elementType)
        Assertions.assertEquals("test", (italic.childElements[0] as Text).text)
        Assertions.assertEquals(ElementType.SMALL, italic.childElements[1].elementType)

        println(node)
    }

    @Test
    fun testBlockInQuoteTest(){
        val text = "<i>\n> must error\n </i>"
        println(text)
        val node = MFMParser.parse(text)
        val italic = node!!.childElements[0] as Node
        Assertions.assertEquals(ElementType.ITALIC, italic.elementType)
        Assertions.assertEquals("\n> must error\n ", (italic.childElements[0] as Text).text)
        println(node)
    }

    @Test
    fun testSameBlockNest(){
        val text = "<i><i>error</i></i>"
        val node = MFMParser.parse(text)
        val italic = node!!.childElements[0] as Node
        println(node)

        Assertions.assertEquals(ElementType.ITALIC, italic.elementType)
        Assertions.assertEquals(ElementType.TEXT, italic.childElements[0].elementType)
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
    fun testMustErrorTitle2(){
        val text = "[yes][no]"
        println(MFMParser.parse(text))
    }

    @Test
    fun testErrorTitle(){
        val text = "[aa[inner]aa]"
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

    @Test
    fun emojiTest(){
        val emojis = listOf(
            Emoji(
                null,
                "kawaii",
                null,
                null,
                null,
                null,
                null
            ),
            Emoji(
                null,
                "ai",
                null,
                null,
                null,
                null,
                null
            ),
            Emoji(
                null,
                "misskey",
                null,
                null,
                null,
                null,
                null
            )
        )
        val text = "Hello world **:ai:は:kawaii::misskey:**"
        println(MFMParser.parse(text, emojis))
    }

    @Test
    fun hashTagTest(){
        val text = "#Hello world #こんにちは,#これはハッシュタグに入らないよ"
        println(MFMParser.parse(text))
    }

    @Test
    fun nestedHashTagTest(){
        val text = "<i>#Hello world</i>"
        println(MFMParser.parse(text))
    }

    @Test
    fun mentionTest(){
        val text = "@Panta@misskey.io\nhogehoge @Panta"
        println(MFMParser.parse(text))
    }

    @Test
    fun urlTest(){
        val text = "https://misskey.io http://misskey.io hogehttps://misskey.io"
        println(MFMParser.parse(text))
    }

    @Test
    fun percentUrlTest(){
        val url = "https://ja.wikipedia.org/wiki/%E3%82%AA%E3%83%BC%E3%83%97%E3%83%B3%E3%82%BD%E3%83%BC%E3%82%B9%E3%82%BD%E3%83%95%E3%83%88%E3%82%A6%E3%82%A7%E3%82%A2"
        println(MFMParser.parse(url))
    }

    @Test
    fun memOverflowText() {
        val text = """
            plain
            **test**
            test [search]
        """.trimIndent()
        println(MFMParser.parse(text))
    }
}
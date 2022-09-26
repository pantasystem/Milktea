package net.pantasystem.milktea.model.notes.muteword

import org.junit.Assert
import org.junit.Test

class WordFilterConfigTextParserTest {

    @Test
    fun fromText_Give1LineWord() {
        val text = "test\n"
        val result = WordFilterConfigTextParser.fromText(text).getOrThrow()
        Assert.assertEquals(
            WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test")))),
            result
        )

    }

    @Test
    fun fromText_Give1LineWordNotExistsReturnCode() {
        val text = "test"
        val result = WordFilterConfigTextParser.fromText(text).getOrThrow()
        Assert.assertEquals(
            WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test")))),
            result
        )
    }


    @Test
    fun fromText_GiveAnyOrText() {
        val text = "hoge piyo fuga"
        val result = WordFilterConfigTextParser.fromText(text).getOrThrow()
        Assert.assertEquals(
            WordFilterConfig(listOf(FilterConditionType.Normal(listOf("hoge", "piyo", "fuga")))),
            result
        )
    }

    @Test
    fun fromText_GiveAnyAndConditionsText() {
        val text = "hoge\npiyo \nfuga"
        val result = WordFilterConfigTextParser.fromText(text).getOrThrow()
        Assert.assertEquals(
            WordFilterConfig(
                listOf(
                    FilterConditionType.Normal(listOf("hoge")),
                    FilterConditionType.Normal(listOf("piyo")),
                    FilterConditionType.Normal(listOf("fuga"))
                )
            ),
            result
        )
    }

    @Test
    fun fromText_GiveAnyAndConditionAndOrConditionText() {
        val text = "hoge fizz panta\npiyo buzz\n fuga moga"
        val result = WordFilterConfigTextParser.fromText(text).getOrThrow()
        Assert.assertEquals(
            WordFilterConfig(
                listOf(
                    FilterConditionType.Normal(listOf("hoge", "fizz", "panta")),
                    FilterConditionType.Normal(listOf("piyo", "buzz")),
                    FilterConditionType.Normal(listOf("fuga", "moga"))
                )
            ),
            result
        )
    }

    @Test
    fun fromText_GiveRegex() {
        val text = "hoge fizz panta\npiyo buzz\n fuga moga\n\\hogepiyo\\"
        val result = WordFilterConfigTextParser.fromText(text).getOrThrow()
        Assert.assertEquals(
            WordFilterConfig(
                listOf(
                    FilterConditionType.Normal(listOf("hoge", "fizz", "panta")),
                    FilterConditionType.Normal(listOf("piyo", "buzz")),
                    FilterConditionType.Normal(listOf("fuga", "moga")),
                    FilterConditionType.Regex("\\hogepiyo\\")
                )
            ),
            result
        )
    }

    @Test
    fun fromConfig_GiveAnyAndConditionAndOrCondition() {
        val config = WordFilterConfig(
            listOf(
                FilterConditionType.Normal(listOf("hoge", "fizz", "panta")),
                FilterConditionType.Normal(listOf("piyo", "buzz")),
                FilterConditionType.Normal(listOf("fuga", "moga"))
            )
        )
        Assert.assertEquals(
            "hoge fizz panta\npiyo buzz\nfuga moga",
            WordFilterConfigTextParser.fromConfig(config).getOrThrow()
        )
    }

    @Test
    fun fromConfig_GiveRegex() {
        val config = WordFilterConfig(
            listOf(
                FilterConditionType.Normal(listOf("hoge", "fizz", "panta")),
                FilterConditionType.Normal(listOf("piyo", "buzz")),
                FilterConditionType.Normal(listOf("fuga", "moga")),
                FilterConditionType.Regex("\\hogepiyo\\")
            )
        )
        Assert.assertEquals(
            "hoge fizz panta\npiyo buzz\nfuga moga\n\\hogepiyo\\",
            WordFilterConfigTextParser.fromConfig(config).getOrThrow()
        )

    }


}
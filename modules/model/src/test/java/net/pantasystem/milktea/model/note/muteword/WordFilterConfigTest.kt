package net.pantasystem.milktea.model.note.muteword

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WordFilterConfigTest {

    @Test
    fun checkMatchText_GiveNullText_ReturnFalse() {
        val config = WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test"))))
        Assertions.assertFalse(config.checkMatchText(null))
    }

    @Test
    fun checkMatchText_GiveEmptyText_ReturnFalse() {
        val config = WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test"))))
        Assertions.assertFalse(config.checkMatchText(""))
    }

    @Test
    fun checkMatchText_GiveMatchText_ReturnTrue() {
        val config = WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test"))))
        Assertions.assertTrue(config.checkMatchText("test"))
    }

    @Test
    fun checkMatchText_GiveNotMatchText_ReturnFalse() {
        val config = WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test"))))
        Assertions.assertFalse(config.checkMatchText("hoge"))
    }

    @Test
    fun checkMatchText_GiveMatchTextWithRegex_ReturnTrue() {
        val config = WordFilterConfig(listOf(FilterConditionType.Regex("""\d+\""")))
        Assertions.assertTrue(config.checkMatchText("123"))
    }

    @Test
    fun checkMatchText_GiveNotMatchTextWithRegex_ReturnFalse() {
        val config = WordFilterConfig(listOf(FilterConditionType.Regex("""\d{3}\""")))
        Assertions.assertFalse(config.checkMatchText("aa"))
    }

    @Test
    fun checkMatchText_GiveMatchTextWithNormalAndRegex_ReturnTrue() {
        val config = WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test")), FilterConditionType.Regex("""\d{3}\""")))
        Assertions.assertTrue(config.checkMatchText("test"))
        Assertions.assertTrue(config.checkMatchText("123"))
    }

    @Test
    fun checkMatchText_GiveNotMatchTextWithNormalAndRegex_ReturnFalse() {
        val config = WordFilterConfig(listOf(FilterConditionType.Normal(listOf("test")), FilterConditionType.Regex("""\d{3}\""")))
        Assertions.assertFalse(config.checkMatchText("hoge"))
        Assertions.assertFalse(config.checkMatchText("12"))
    }

    @Test
    fun checkMatchText_GiveMatchTextWithNormalAndRegexAndNormal_ReturnTrue() {
        val config = WordFilterConfig(
            listOf(
                FilterConditionType.Normal(listOf("test")),
                FilterConditionType.Regex("""\d{3}\"""),
                FilterConditionType.Normal(listOf("hoge"))
            )
        )
        Assertions.assertTrue(config.checkMatchText("test"))
        Assertions.assertTrue(config.checkMatchText("123"))
        Assertions.assertTrue(config.checkMatchText("hoge test 123"))

    }


}
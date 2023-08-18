package net.pantasystem.milktea.model.note.muteword

sealed interface FilterConditionType {
    data class Normal(val words: List<String>) : FilterConditionType
    data class Regex(val pattern: String) : FilterConditionType {
        val regex: kotlin.text.Regex by lazy {
            kotlin.text.Regex(pattern.substring(1 until (pattern.length - 1)))
        }
    }
}
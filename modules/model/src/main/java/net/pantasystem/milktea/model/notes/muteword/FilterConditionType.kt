package net.pantasystem.milktea.model.notes.muteword

sealed interface FilterConditionType {
    data class Normal(val words: List<String>) : FilterConditionType
    data class Regex(val pattern: String) : FilterConditionType
}
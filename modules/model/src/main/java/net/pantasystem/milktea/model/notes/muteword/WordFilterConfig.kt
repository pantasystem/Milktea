package net.pantasystem.milktea.model.notes.muteword

data class WordFilterConfig(
    val conditions: List<FilterConditionType>
) {
    companion object

    fun checkMatchText(text: String?): Boolean {
        if (text == null) {
            return false
        }
        return conditions.any { type ->
            when(type) {
                is FilterConditionType.Normal -> {
                    type.words.all { word ->
                        text.contains(word)
                    }
                }
                is FilterConditionType.Regex -> {
                    type.regex.matches(text)
                }
            }
        }
    }
}

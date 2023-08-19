package net.pantasystem.milktea.model.note.muteword

object WordFilterConfigTextParser {

    fun fromText(text: String): Result<WordFilterConfig> {
        val conditions = text.split("\n").filterNot {
            it.isBlank()
        }.map { line ->
            if (line.startsWith("\\") && line.endsWith("\\")) {
                FilterConditionType.Regex(line)
            } else {
                val words = line.split(" ").filterNot {
                    it.isBlank()
                }
                FilterConditionType.Normal(words)
            }
        }
        return Result.success(WordFilterConfig(conditions))
    }

    fun fromConfig(config: WordFilterConfig): Result<String> {
        val text = config.conditions.filter {
            when(it) {
                is FilterConditionType.Normal -> it.words.isNotEmpty()
                is FilterConditionType.Regex -> it.pattern.isNotBlank()
            }
        }.map { filterConditionType ->
            when(filterConditionType) {
                is FilterConditionType.Normal -> {
                    if (filterConditionType.words.size > 1) {
                        filterConditionType.words.reduce { acc, s ->
                            "$acc $s"
                        }
                    } else {
                        filterConditionType.words.first()
                    }
                }
                is FilterConditionType.Regex -> {
                    filterConditionType.pattern
                }
            }
        }.reduce { acc, s ->
            "$acc\n$s"
        }
        return Result.success(text)
    }


}
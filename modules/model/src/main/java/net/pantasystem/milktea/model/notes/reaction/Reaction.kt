package net.pantasystem.milktea.model.notes.reaction

data class Reaction(val reaction: String) {
    fun isLocal(): Boolean {
        return !reaction.contains("@") || reaction.replace(":", "").split("@").getOrNull(1) == "."
    }

    fun getName(): String? {
        return reaction.replace(":", "").split("@").getOrNull(0)
    }

    fun getHost(): String? {
        return reaction.replace(":", "").split("@").getOrNull(1)
    }

    fun getNameAndHost(): String {
        if (!isCustomEmojiFormat()) {
            return reaction
        }
        val text = getName() ?: reaction
        val host = getHost()
        return if (host == null) {
            text
        } else {
            "$text@$host"
        }
    }

    /**
     * フォーマットがカスタム絵文字のものであるか判定する
     */
    fun isCustomEmojiFormat(): Boolean {
        // 初めのコロンと末端のコロンで２文字、カスタム絵文字の名称で１文字
        return reaction.codePointCount(0, reaction.length) >= 3
                && reaction.startsWith(":")
                && reaction.endsWith(":")
    }
}
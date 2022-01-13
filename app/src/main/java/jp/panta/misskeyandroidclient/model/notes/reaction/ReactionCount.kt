package jp.panta.misskeyandroidclient.model.notes.reaction

import java.io.Serializable

data class ReactionCount(
    val reaction: String,
    val count: Int
) : Serializable {

    fun isLocal(): Boolean {
        return !reaction.contains("@") || reaction.replace(":", "").split("@").getOrNull(1) == "."
    }

    fun increment(): ReactionCount {
        return copy(
            count = (count + 1)
        )
    }
    fun decrement(): ReactionCount {
        if(count <= 0) {
            return this
        }
        return copy(
            count = (count - 1)
        )
    }
}
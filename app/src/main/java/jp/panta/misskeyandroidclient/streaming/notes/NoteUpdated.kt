package jp.panta.misskeyandroidclient.streaming.notes


data class NoteUpdated (
    val id: String,
    val type: Type
) {
    sealed class Type {
        class Reacted (
            val reaction: String,
            val userId: String,
        ) : Type()

        class Unreacted (
            val reaction: String,
            val userId: String,
        ) : Type()

        class PollVoted(
            val choice: Int,
            val userId: String
        ) : Type()

        object Deleted : Type()
    }


}


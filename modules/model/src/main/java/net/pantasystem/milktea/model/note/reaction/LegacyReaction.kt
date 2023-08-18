package net.pantasystem.milktea.model.note.reaction

object LegacyReaction {

    val reactionMap = mapOf(
        "angry" to "\uD83D\uDCA2",  // 💢
        "confused" to "\uD83D\uDE25", // 😥
        "congrats" to "\uD83C\uDF89", // 🎉
        "hmm" to "\uD83E\uDD14",    //  🤔
        "laugh" to "\uD83D\uDE06",  // 😆
        "like" to "\uD83D\uDC4D", // 👍
        "love" to "❤", //❤
        "pudding" to "\uD83C\uDF6E", // 🍮
        "rip" to "\uD83D\uDE07", // 😇
        "surprise" to "\uD83D\uDE2E", // 😮
        "star" to "⭐" // ⭐
    )

    val defaultReaction = listOf("angry", "confused", "congrats", "hmm", "laugh", "like", "love", "pudding", "rip", "surprise", "star")

}
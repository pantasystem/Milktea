package jp.panta.misskeyandroidclient.model.notes.poll

data class Poll(
    val choices: List<Choice>,
    val expiresAt: String,
    val multiple: Boolean
){
    data class Choice(val text: String, val votes: Int, val isVoted: Boolean)
}
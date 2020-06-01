package jp.panta.misskeyandroidclient.model.notes.draft



data class DraftNote(
    val accountId: String,
    val visibility: String = "public",
    var visibleUserIds: List<String>? = null,
    val text: String?,
    val cw: String? = null,
    var draftFiles: List<DraftFile>? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    val draftPoll: DraftPoll? = null


){

    var draftNoteId: Long? = null

}
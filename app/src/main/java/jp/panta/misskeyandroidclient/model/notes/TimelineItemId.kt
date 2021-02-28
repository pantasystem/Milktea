package jp.panta.misskeyandroidclient.model.notes


data class TimelineItem(
    val id: Id,
    val noteId: Note.Id,
    val note: StatefulNote,
) {

    data class Id (
        val noteId: Note.Id,
        val featureId: String?
    )
}
package net.pantasystem.milktea.data.infrastructure.note

import android.content.SharedPreferences
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.TimelineScrollPositionRepository

class TimelineScrollPositionRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : TimelineScrollPositionRepository {
    override suspend fun save(pageId: Long, noteId: Note.Id) {
        sharedPreferences.edit()
            .putString("timeline_scroll_position_note_id_$pageId", noteId.noteId)
            .putLong("timeline_scroll_position_account_id_$pageId", noteId.accountId)
            .apply()
    }

    override suspend fun get(pageId: Long): Note.Id? {
        val noteId = sharedPreferences.getString("timeline_scroll_position_note_id_$pageId", null)
        val accountId = sharedPreferences.getLong("timeline_scroll_position_account_id_$pageId", 0L).takeIf {
            it > 0L
        }
        return if (noteId == null || accountId == null) {
            null
        } else {
            Note.Id(accountId, noteId)
        }
    }

    override suspend fun remove(pageId: Long) {
        sharedPreferences.edit()
            .remove("timeline_scroll_position_note_id_$pageId")
            .remove("timeline_scroll_position_account_id_$pageId")
            .apply()
    }
}
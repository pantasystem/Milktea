package net.pantasystem.milktea.model.notes

interface TimelineScrollPositionRepository {

    suspend fun save(pageId: Long, noteId: Note.Id)

    suspend fun get(pageId: Long): Note.Id?

    suspend fun remove(pageId: Long)

}
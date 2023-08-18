package net.pantasystem.milktea.data.infrastructure.note.impl

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDeletedException
import net.pantasystem.milktea.model.note.NoteNotFoundException
import net.pantasystem.milktea.model.note.make
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InMemoryNoteDataSourceTest {

    @Test
    fun get_ThrowsNoteDeletedExceptionGiveDeletedNote(): Unit = runBlocking {
        val noteDataSource = InMemoryNoteDataSource(MemoryCacheCleaner())
        val id = Note.Id(0L, "testId")
        noteDataSource.delete(id)
        val result = noteDataSource.get(id)
        Assertions.assertNotNull(result.exceptionOrNull())
        Assertions.assertThrows(NoteDeletedException::class.java) {
            result.getOrThrow()
        }
    }

//    @Test
//    fun get_ThrowsNoteRemovedExceptionGiveRemovedNote(): Unit = runBlocking {
//        val noteDataSource = InMemoryNoteDataSource(MemoryCacheCleaner())
//        val id = Note.Id(0L, "testId")
//        noteDataSource.remove(id)
//        val result = noteDataSource.get(id)
//        Assertions.assertNotNull(result.exceptionOrNull())
//        Assertions.assertThrows(NoteRemovedException::class.java) {
//            result.getOrThrow()
//        }
//    }

    @Test
    fun get_ThrowsNoteNotFoundExceptionGiveNotExistsNote(): Unit = runBlocking {
        val noteDataSource = InMemoryNoteDataSource(MemoryCacheCleaner())
        val id = Note.Id(0L, "testId")
        val result = noteDataSource.get(id)
        Assertions.assertThrows(NoteNotFoundException::class.java) {
            result.getOrThrow()
        }
    }

    @Test
    fun get_ReturnsNoteGiveExistsNote(): Unit = runBlocking {
        val noteDataSource = InMemoryNoteDataSource(MemoryCacheCleaner())
        val id = Note.Id(0L, "testId")
        val testNote = Note.make(id, User.Id(0L, "testUserId"))
        noteDataSource.add(testNote)
        val result = noteDataSource.get(id)
        Assertions.assertEquals(testNote, result.getOrThrow())
    }
}
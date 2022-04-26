package net.pantasystem.milktea.model.notes.reaction

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.generateEmptyNote
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryCount
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

interface MyClass {
    suspend fun num(): Int
}
class ClassUnderTest(
    val myClass: MyClass,
) {
    suspend fun render() {
        myClass.num()
        //println("num:${myClass.num()}")
    }
}
class ToggleReactionUseCaseTest {

    @Test
    fun mockTest() {
        val myClass = mock<MyClass>() {
            onBlocking {
                num()
            } doReturn 10
        }
        val underTest = ClassUnderTest(myClass)
        runBlocking {
            underTest.render()
        }
        verifyBlocking(myClass) {
            num()
        }
    }


    @Test
    fun giveCustomEmojiReaction() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId")
        )
        val createReactionDTO = CreateReaction(targetNote.id, ":kawaii:")


        var isCalledReaction = false
        val noteRepository = object : NoteRepository {
            override suspend fun create(createNote: CreateNote) = throw NoSuchMethodException()
            override suspend fun delete(noteId: Note.Id) = true
            override suspend fun find(noteId: Note.Id) = targetNote
            override suspend fun reaction(createReaction: CreateReaction): Boolean {
                Assert.assertEquals(createReactionDTO, createReaction)
                isCalledReaction = true
                return true
            }
            override suspend fun unreaction(noteId: Note.Id) = true
        }

        val meta = Meta(
            uri = "misskey.io",
            emojis = listOf(
                Emoji(name = "kawaii")
            )
        )
        val reactionHistoryDao = object : ReactionHistoryDao {
            override fun findAll(): List<ReactionHistory>? = null
            override fun insert(reactionHistory: ReactionHistory) = Unit
            override fun sumReactions(instanceDomain: String): List<ReactionHistoryCount> = emptyList()
        }
        val useCase = ToggleReactionUseCase(
            getAccount = {
                Account(
                    "testId",
                    "misskey.io",
                    instanceType = Account.InstanceType.MISSKEY,
                    encryptedToken = "test",
                    userName = "test",
                    accountId = 0L,
                    pages = emptyList(),
                )
            },
            noteRepository = noteRepository,
            fetchMeta = object : FetchMeta {
                override suspend fun fetch(instanceDomain: String, isForceFetch: Boolean) = meta
            },
            reactionHistoryDao = reactionHistoryDao
        )

        runBlocking {
            useCase(targetNote.id, ":kawaii:").getOrThrow()
        }
        Assert.assertTrue(isCalledReaction)

    }
}
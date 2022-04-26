package net.pantasystem.milktea.model.notes.reaction

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
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
import org.mockito.kotlin.*

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

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                reaction(createReactionDTO)
            } doReturn true
            onBlocking {
                find(targetNote.id)
            } doReturn targetNote
        }

        val meta = Meta(
            uri = "misskey.io",
            emojis = listOf(
                Emoji(name = "kawaii")
            )
        )
        val reactionHistoryDao = mock<ReactionHistoryDao>()
        val account = Account(
            "testId",
            "misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            encryptedToken = "test",
            userName = "test",
            accountId = 0L,
            pages = emptyList(),
        )
        val getAccount = mock<GetAccount> {
            onBlocking {
                get(any())
            } doReturn account
        }
        val fetchMeta = mock<FetchMeta> {
            onBlocking {
                fetch(account.instanceDomain)
            } doReturn meta
        }
        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            fetchMeta = fetchMeta,
            reactionHistoryDao = reactionHistoryDao
        )

        runBlocking {
            useCase(targetNote.id, ":kawaii:").getOrThrow()
        }
        verifyBlocking(noteRepository) {
            reaction(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            insert(ReactionHistory(":kawaii:", "misskey.io"))
        }
    }
}
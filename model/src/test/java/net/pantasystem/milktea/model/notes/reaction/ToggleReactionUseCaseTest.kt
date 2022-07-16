package net.pantasystem.milktea.model.notes.reaction

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.generateEmptyNote
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class ToggleReactionUseCaseTest {

    @Test
    fun giveCustomEmojiReactionWhenHasMyReaction() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId"),
            myReaction = ":kawaii:"
        )

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
            onBlocking {
                unreaction(targetNote.id).getOrThrow()
            } doReturn true
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

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            fetchMeta = fetchMeta,
            reactionHistoryDao = reactionHistoryDao,
            checkEmoji = checkEmoji
        )
        runBlocking {
            useCase(targetNote.id, ":kawaii:").getOrThrow()
        }

        verifyBlocking(noteRepository) {
            unreaction(targetNote.id)
        }
    }

    @Test
    fun giveCustomEmojiReactionWhenHasOtherMyReaction() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId"),
            myReaction = ":kawaii:"
        )
        val createReactionDTO = CreateReaction(targetNote.id, ":wakaranai:")

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
            onBlocking {
                unreaction(targetNote.id).getOrThrow()
            } doReturn true
            onBlocking {
                reaction(createReactionDTO).getOrThrow()
            } doReturn true
        }

        val meta = Meta(
            uri = "misskey.io",
            emojis = listOf(
                Emoji(name = "kawaii"), Emoji(name = "wakaranai")
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

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            fetchMeta = fetchMeta,
            reactionHistoryDao = reactionHistoryDao,
            checkEmoji = checkEmoji
        )
        runBlocking {
            useCase(targetNote.id, ":wakaranai:").getOrThrow()
        }

        verifyBlocking(noteRepository) {
            unreaction(targetNote.id)
            reaction(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            insert(ReactionHistory(":wakaranai:", "misskey.io"))
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
                reaction(createReactionDTO).getOrThrow()
            } doReturn true
            onBlocking {
                find(targetNote.id).getOrThrow()
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
        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }
        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            fetchMeta = fetchMeta,
            reactionHistoryDao = reactionHistoryDao,
            checkEmoji = checkEmoji
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

    @Test
    fun giveUnknownCharacter() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId")
        )
        val createReactionDTO = CreateReaction(targetNote.id, "👍")

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                reaction(createReactionDTO).getOrThrow()
            } doReturn true
            onBlocking {
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
        }

        val meta = Meta(uri = "misskey.io",)
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
            reactionHistoryDao = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn false
            }
        )

        runBlocking {
            useCase(targetNote.id, "unknown").getOrThrow()
        }
        verifyBlocking(noteRepository) {
            reaction(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            insert(ReactionHistory("👍", "misskey.io"))
        }
    }
    @Test
    fun giveMultiByteEmoji() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId")
        )
        val createReactionDTO = CreateReaction(targetNote.id, "🥺")

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                reaction(createReactionDTO).getOrThrow()
            } doReturn true
            onBlocking {
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
        }

        val meta = Meta(uri = "misskey.io",)
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
            reactionHistoryDao = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn true
            }
        )

        runBlocking {
            useCase(targetNote.id, "🥺").getOrThrow()
        }
        verifyBlocking(noteRepository) {
            reaction(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            insert(ReactionHistory("🥺", "misskey.io"))
        }
    }

    @Test
    fun giveLegacyReaction() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId")
        )
        val createReactionDTO = CreateReaction(targetNote.id, "like")

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                reaction(createReactionDTO).getOrThrow()
            } doReturn true
            onBlocking {
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
        }

        val meta = Meta(uri = "misskey.io",)
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
            reactionHistoryDao = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn true
            }
        )

        runBlocking {
            useCase(targetNote.id, "like").getOrThrow()
        }
        verifyBlocking(noteRepository) {
            reaction(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            insert(ReactionHistory("like", "misskey.io"))
        }
    }
}
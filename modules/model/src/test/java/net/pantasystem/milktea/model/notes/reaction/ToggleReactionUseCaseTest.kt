package net.pantasystem.milktea.model.notes.reaction

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.generateEmptyNote
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import org.junit.jupiter.api.Test
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

        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                delete(targetNote.id).getOrThrow()
            } doReturn true
        }

        val meta = Meta(
            uri = "misskey.io",
            emojis = listOf(
                Emoji(name = "kawaii")
            )
        )
        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            token = "test",
            userName = "test",
            accountId = 0L,
            pages = emptyList(),
        )
        val getAccount = mock<GetAccount> {
            onBlocking {
                get(any())
            } doReturn account
        }
        val fetchMeta = mock<MetaRepository> {
            onBlocking {
                find(account.normalizedInstanceDomain)
            } doReturn Result.success(meta)
        }

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "", version = "", software = NodeInfo.Software(
                        name = "misskey",
                        version = "v13.0.0"
                    )
                )
            )
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            metaRepository = fetchMeta,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            nodeInfoRepository = nodeInfoRepository,
        )
        runBlocking {
            useCase(targetNote.id, ":kawaii:").getOrThrow()
        }

        verifyBlocking(reactionRepository) {
            delete(targetNote.id)
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

        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                delete(targetNote.id).getOrThrow()
            } doReturn true
            onBlocking {
                create(createReactionDTO).getOrThrow()
            } doReturn true
        }

        val meta = Meta(
            uri = "https://misskey.io",
            emojis = listOf(
                Emoji(name = "kawaii"), Emoji(name = "wakaranai")
            )
        )
        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            token = "test",
            userName = "test",
            accountId = 0L,
            pages = emptyList(),
        )
        val getAccount = mock<GetAccount> {
            onBlocking {
                get(any())
            } doReturn account
        }
        val fetchMeta = mock<MetaRepository> {
            onBlocking {
                find(account.normalizedInstanceDomain)
            } doReturn Result.success(meta)
        }

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "", version = "", software = NodeInfo.Software(
                        name = "misskey",
                        version = "v13.0.0"
                    )
                )
            )
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            metaRepository = fetchMeta,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            nodeInfoRepository = nodeInfoRepository,
        )
        runBlocking {
            useCase(targetNote.id, ":wakaranai:").getOrThrow()
        }

        verifyBlocking(reactionRepository) {
            delete(targetNote.id)
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(ReactionHistory(":wakaranai:", "https://misskey.io"))
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
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                create(createReactionDTO).getOrThrow()
            } doReturn true
        }

        val meta = Meta(
            uri = "https://misskey.io",
            emojis = listOf(
                Emoji(name = "kawaii")
            )
        )
        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            token = "test",
            userName = "test",
            accountId = 0L,
            pages = emptyList(),
        )
        val getAccount = mock<GetAccount> {
            onBlocking {
                get(any())
            } doReturn account
        }
        val fetchMeta = mock<MetaRepository> {
            onBlocking {
                find(account.normalizedInstanceDomain)
            } doReturn Result.success(meta)
        }
        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "", version = "", software = NodeInfo.Software(
                        name = "misskey",
                        version = "v13.0.0"
                    )
                )
            )
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            metaRepository = fetchMeta,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            nodeInfoRepository = nodeInfoRepository,
        )

        runBlocking {
            useCase(targetNote.id, ":kawaii:").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(ReactionHistory(":kawaii:", "https://misskey.io"))
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
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                create(createReactionDTO).getOrThrow()
            } doReturn true
        }

        val meta = Meta(uri = "https://misskey.io",)
        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            token = "test",
            userName = "test",
            accountId = 0L,
            pages = emptyList(),
        )
        val getAccount = mock<GetAccount> {
            onBlocking {
                get(any())
            } doReturn account
        }
        val fetchMeta = mock<MetaRepository> {
            onBlocking {
                find(account.normalizedInstanceDomain)
            } doReturn Result.success(meta)
        }

        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "", version = "", software = NodeInfo.Software(
                        name = "misskey",
                        version = "v13.0.0"
                    )
                )
            )
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            metaRepository = fetchMeta,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn false
            },
            reactionRepository = reactionRepository,
            nodeInfoRepository = nodeInfoRepository
        )

        runBlocking {
            useCase(targetNote.id, "unknown").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(ReactionHistory("👍", "https://misskey.io"))
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
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                create(createReactionDTO).getOrThrow()
            } doReturn true
        }

        val meta = Meta(uri = "https://misskey.io",)
        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            token = "test",
            userName = "test",
            accountId = 0L,
            pages = emptyList()
        )
        val getAccount = mock<GetAccount> {
            onBlocking {
                get(any())
            } doReturn account
        }
        val fetchMeta = mock<MetaRepository> {
            onBlocking {
                find(account.normalizedInstanceDomain)
            } doReturn Result.success(meta)
        }

        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "", version = "", software = NodeInfo.Software(
                        name = "misskey",
                        version = "v13.0.0"
                    )
                )
            )
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            metaRepository = fetchMeta,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn true
            },
            reactionRepository = reactionRepository,
            nodeInfoRepository = nodeInfoRepository,
        )

        runBlocking {
            useCase(targetNote.id, "🥺").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(ReactionHistory("🥺", "https://misskey.io"))
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
                find(targetNote.id).getOrThrow()
            } doReturn targetNote
        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                create(createReactionDTO).getOrThrow()
            } doReturn true
        }

        val meta = Meta(uri = "https://misskey.io",)
        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            token = "test",
            userName = "test",
            accountId = 0L,
            pages = emptyList()
        )
        val getAccount = mock<GetAccount> {
            onBlocking {
                get(any())
            } doReturn account
        }
        val fetchMeta = mock<MetaRepository> {
            onBlocking {
                find(account.normalizedInstanceDomain)
            } doReturn Result.success(meta)
        }

        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "", version = "", software = NodeInfo.Software(
                        name = "misskey",
                        version = "v13.0.0"
                    )
                )
            )
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            metaRepository = fetchMeta,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn true
            },
            reactionRepository = reactionRepository,
            nodeInfoRepository = nodeInfoRepository
        )

        runBlocking {
            useCase(targetNote.id, "like").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(ReactionHistory("like", "https://misskey.io"))
        }
    }
}
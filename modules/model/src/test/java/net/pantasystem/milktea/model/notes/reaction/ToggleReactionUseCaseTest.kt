package net.pantasystem.milktea.model.notes.reaction

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import net.pantasystem.milktea.model.instance.Meta
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
            reactionCounts = listOf(
                ReactionCount(
                    ":kawaii:",
                    1,
                    me = true,
                )
            ),
            myReaction = ":kawaii:"
        )

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                find(targetNote.id).getOrThrow()
            } doReturn targetNote

        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                delete(DeleteReaction(targetNote.id, ":kawaii:")).getOrThrow()
            } doReturn true
        }

        val meta = Meta(
            uri = "misskey.io",

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

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(
                    listOf(
                        Emoji(
                            name = "kawaii"
                        )
                    )
                )
            },
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(InstanceInfoType.Misskey(meta))
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )
        runBlocking {
            useCase(targetNote.id, ":kawaii:").getOrThrow()
        }

        verifyBlocking(reactionRepository) {
            delete(DeleteReaction(targetNote.id, ":kawaii:"))
        }
    }

    @Test
    fun giveCustomEmojiReactionWhenHasOtherMyReaction() = runBlocking {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId"),
            reactionCounts = listOf(
                ReactionCount(":kawaii:", 1, true)
            ),
        )
        val createReactionDTO = CreateReaction(targetNote.id, ":wakaranai:")

        val noteRepository = mock<NoteRepository> {
            onBlocking {
                find(targetNote.id).getOrThrow()
            } doReturn targetNote

        }

        val reactionRepository = mock<ReactionRepository> {
            onBlocking {
                delete(DeleteReaction(targetNote.id, ":kawaii:")).getOrThrow()
            } doReturn true
            onBlocking {
                create(createReactionDTO).getOrThrow()
            } doReturn true
        }

        val meta = Meta(
            uri = "https://misskey.io",

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

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(listOf(Emoji(name = "wakaranai")))
            },
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(InstanceInfoType.Misskey(meta))
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )
        runBlocking {
            useCase(targetNote.id, ":wakaranai:").getOrThrow()
        }

        verifyBlocking(reactionRepository) {
            delete(DeleteReaction(targetNote.id, ":kawaii:"))
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(
                ReactionHistory(
                    reaction = ":wakaranai:",
                    instanceDomain = "https://misskey.io",
                    accountId = account.accountId,
                    targetPostId = targetNote.id.noteId,
                    targetUserId = targetNote.userId.id,
                )
            )
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

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(listOf(Emoji("kawaii")))
            },
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(InstanceInfoType.Misskey(meta))
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )

        runBlocking {
            useCase(targetNote.id, ":kawaii:").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(
                ReactionHistory(
                    reaction = ":kawaii:",
                    instanceDomain = "https://misskey.io",
                    targetUserId = targetNote.userId.id,
                    targetPostId = targetNote.id.noteId,
                    accountId = account.accountId
                )
            )
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

        val meta = Meta(uri = "https://misskey.io")
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

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn false
            },
            reactionRepository = reactionRepository,
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(InstanceInfoType.Misskey(meta))
            },
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(emptyList())
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )

        runBlocking {
            useCase(targetNote.id, "unknown").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(
                ReactionHistory(
                    reaction = "👍",
                    instanceDomain = "https://misskey.io",
                    accountId = account.accountId,
                    targetPostId = targetNote.id.noteId,
                    targetUserId = targetNote.userId.id,
                )
            )
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

        val meta = Meta(uri = "https://misskey.io")
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


        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn true
            },
            reactionRepository = reactionRepository,
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(InstanceInfoType.Misskey(meta))
            },
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(emptyList())
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )

        runBlocking {
            useCase(targetNote.id, "🥺").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(
                ReactionHistory(
                    reaction = "🥺",
                    instanceDomain = "https://misskey.io",
                    accountId = account.accountId,
                    targetUserId = targetNote.userId.id,
                    targetPostId = targetNote.id.noteId,
                )
            )
        }
    }

    @Test
    fun giveLegacyReaction() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId")
        )
        val createReactionDTO = CreateReaction(targetNote.id, "\uD83D\uDE06")

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

        val meta = Meta(uri = "https://misskey.io")
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

        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = mock {
                onBlocking {
                    checkEmoji(any())
                } doReturn false
            },
            reactionRepository = reactionRepository,
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(listOf())
            },
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(InstanceInfoType.Misskey(meta))
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )

        runBlocking {
            useCase(targetNote.id, "laugh").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(
                ReactionHistory(
                    reaction = "\uD83D\uDE06",
                    instanceDomain = "https://misskey.io",
                    accountId = account.accountId,
                    targetPostId = targetNote.id.noteId,
                    targetUserId = targetNote.userId.id,
                )
            )
        }
    }

    @Test
    fun giveRemoteEmojiWhenFedibird() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId")
        )
        val createReactionDTO = CreateReaction(targetNote.id, "kawaii@misskey.io")

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

        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://fedibird.com",
            instanceType = Account.InstanceType.MASTODON,
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

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }


        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(emptyList())
            },
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Mastodon(
                        MastodonInstanceInfo(
                            uri = "",
                            title = "",
                            description = "",
                            email = "",
                            version = "",
                            urls = MastodonInstanceInfo.Urls(streamingApi = null),
                            configuration = MastodonInstanceInfo.Configuration(
                                emojiReactions = MastodonInstanceInfo.Configuration.EmojiReactions(
                                    maxReactions = 1000,
                                    maxReactionsPerAccount = 1
                                ),
                                polls = null,
                                statuses = null,
                            ),
                            fedibirdCapabilities = listOf("emoji_reaction"),
                            pleroma = null,
                        )
                    )
                )
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )

        runBlocking {
            useCase(targetNote.id, "kawaii@misskey.io").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(
                ReactionHistory(
                    reaction = "kawaii@misskey.io",
                    instanceDomain = "https://fedibird.com",
                    accountId = account.accountId,
                    targetUserId = targetNote.userId.id,
                    targetPostId = targetNote.id.noteId,
                )
            )
        }
    }

    @Test
    fun giveRemoteEmojiWhenMastodonAndCanMultipleReaction() {
        val targetNote = generateEmptyNote().copy(
            text = "test",
            id = Note.Id(accountId = 0L, "testId"),
            reactionCounts = listOf(
                ReactionCount(
                    ":iizo:",
                    2,
                    true,
                ),
                ReactionCount(
                    ":dame:",
                    1,
                    false,
                )
            )
        )
        val createReactionDTO = CreateReaction(targetNote.id, "kawaii@misskey.io")

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

        val reactionHistoryDao = mock<ReactionHistoryRepository>()
        val account = Account(
            "testId",
            "https://fedibird.com",
            instanceType = Account.InstanceType.MASTODON,
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

        val checkEmoji = mock<CheckEmoji> {
            onBlocking {
                checkEmoji(any())
            } doReturn true
        }


        val useCase = ToggleReactionUseCase(
            getAccount = getAccount,
            noteRepository = noteRepository,
            reactionHistoryRepository = reactionHistoryDao,
            checkEmoji = checkEmoji,
            reactionRepository = reactionRepository,
            customEmojiRepository = mock() {
                onBlocking {
                    findByName(any(), any())
                } doReturn Result.success(emptyList())
            },
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Mastodon(
                        MastodonInstanceInfo(
                            uri = "",
                            title = "",
                            description = "",
                            email = "",
                            version = "",
                            urls = MastodonInstanceInfo.Urls(streamingApi = null),
                            configuration = MastodonInstanceInfo.Configuration(
                                emojiReactions = MastodonInstanceInfo.Configuration.EmojiReactions(
                                    maxReactions = 1000,
                                    maxReactionsPerAccount = 2
                                ),
                                polls = null,
                                statuses = null,
                            ),
                            fedibirdCapabilities = listOf("emoji_reaction"),
                            pleroma = null,
                        )
                    )
                )
            },
            userRepository = mock() {
                onBlocking {
                    sync(any())
                } doReturn Result.success(Unit)
            }
        )

        runBlocking {
            useCase(targetNote.id, "kawaii@misskey.io").getOrThrow()
        }
        verifyBlocking(reactionRepository) {
            create(createReactionDTO)
        }

        verifyBlocking(reactionHistoryDao) {
            create(
                ReactionHistory(
                    reaction = "kawaii@misskey.io",
                    instanceDomain = "https://fedibird.com",
                    targetPostId = targetNote.id.noteId,
                    targetUserId = targetNote.userId.id,
                    accountId = account.accountId,
                )
            )
        }
    }
}
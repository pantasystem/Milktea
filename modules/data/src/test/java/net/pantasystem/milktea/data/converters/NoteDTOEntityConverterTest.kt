package net.pantasystem.milktea.data.converters

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteVisibilityType
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class NoteDTOEntityConverterTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun convert() = runTest {

        val converter = NoteDTOEntityConverter(
            mock() {
                onBlocking {
                    findIn(any())
                } doReturn Result.success(emptyList())
            },
            mock() {
                onBlocking {
                    findBySourceUrls(any())
                } doReturn emptyList()
            },
            mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        Meta(
                            uri = "https://misskey.pantasystem.com",
                        )
                    )
                )
            }
        )

        val account = Account(
            remoteId = "test-id",
            instanceDomain = "https://misskey.pantasystem.com",
            userName = "Panta",
            instanceType = Account.InstanceType.MISSKEY,
            token = "",
        ).copy(accountId = 1L)

        val noteDTO = NoteDTO(
            id = "noteId",
            createdAt = Clock.System.now(),
            text = "test",
            cw = "cw",
            userId = "user-1",
            replyId = "reply-id",
            renoteId = "renote-id",
            viaMobile = false,
            visibility = NoteVisibilityType.Public,
            localOnly = false,
            visibleUserIds = listOf(),
            rawReactionEmojis = net.pantasystem.milktea.api.misskey.emoji.EmojisType.None,
            url = null,
            uri = null,
            renoteCount = 0,
            reactionCounts = null,
            rawEmojis = null,
            replyCount = 0,
            user = UserDTO(
                id = "idididi",
                userName = "test",
            ),
            files = listOf(),
            fileIds = listOf(),
            poll = null,
            reNote = null,
            reply = null,
            myReaction = "😇",
            tmpFeaturedId = null,
            promotionId = null,
            channelId = null,
            app = null,
            channel = null
        )

        val result = converter.convert(
            noteDTO, account
        )
        Assertions.assertEquals(Note.Id(account.accountId, noteDTO.id), result.id)
        Assertions.assertEquals(noteDTO.cw, result.cw)
        Assertions.assertEquals(noteDTO.text, result.text)
        Assertions.assertEquals(User.Id(account.accountId, noteDTO.userId), result.userId)
        Assertions.assertEquals(noteDTO.replyId?.let {
            Note.Id(account.accountId, it)
        }, result.replyId)
        Assertions.assertEquals(noteDTO.renoteId?.let {
            Note.Id(account.accountId, it)
        }, result.renoteId)
        Assertions.assertEquals(noteDTO.cw, result.cw)
        Assertions.assertEquals(noteDTO.text, result.text)

    }
}
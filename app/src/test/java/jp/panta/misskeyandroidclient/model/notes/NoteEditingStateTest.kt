package jp.panta.misskeyandroidclient.model.notes

import junit.framework.TestCase
import net.pantasystem.milktea.model.notes.AddMentionResult
import net.pantasystem.milktea.model.notes.NoteEditingState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility

class NoteEditingStateTest : TestCase() {

    fun testCheckValidateWhenNoInputsOfFailure() {
        val state = NoteEditingState()
        assertFalse(state.checkValidate(3000))
    }

    fun testCheckValidationWhenOverTextLengthOfFailure() {
        val text = "ほげほげ😇"
        val state = NoteEditingState(text = text)
        assertFalse(state.checkValidate(text.codePointCount(0, text.length) - 1))
    }

    fun testCheckValidationWhenUnderTextLengthOfSuccess() {
        // NOTE: マルチバイトコードでも問題ないことを検証したい
        val text = "helloほげほげ😄😄💢"
        val state = NoteEditingState(text = text)
        assertTrue(state.checkValidate(text.codePointCount(0, text.length)))
    }

    fun testCheckValidationWhenOnlyFileOfSuccess() {
        val state = NoteEditingState(files = listOf(AppFile.Remote(id = FileProperty.Id(0L, "id"))))
        assertTrue(state.checkValidate())
    }


    fun testCheckValidationWhenJustFileCountsOfSuccess() {
        val state = NoteEditingState(files = listOf("a", "b", "c", "d").map {
            AppFile.Remote(FileProperty.Id(0L, it))
        })
        assertTrue(state.checkValidate())
    }

    fun testCheckValidationWhenOverFileCountsAndJustTextLengthOfFailure() {
        val state = NoteEditingState(
            files = listOf("a", "b", "c", "d", "e").map {
                AppFile.Remote(FileProperty.Id(0L, it))
            },
            text = "ほげぴよ"
        )
        assertFalse(state.checkValidate())
    }

    fun testCheckValidationOnlyRenoteIdOnSuccess() {
        val state = NoteEditingState(
            renoteId = Note.Id(0L, "")
        )
        assertTrue(state.checkValidate())
    }

    fun testCheckValidationOnlyRenoteIdAndOverTextLengthOfFailure() {
        val state = NoteEditingState(
            renoteId = Note.Id(0L, ""),
            text = "ほげぴよ"
        )
        assertFalse(state.checkValidate(3))
    }


    fun testChangeText() {
        var state = NoteEditingState()
        state = state.changeText("ほげ")
        assertEquals("ほげ", state.text)
    }

    fun testChangeCw() {
        var state = NoteEditingState()
        state = state.changeCw("cw")
        assertEquals("cw", state.cw)
    }

    fun testAddFileWhenAddRemoteFile() {
        var state = NoteEditingState()
        val file = AppFile.Remote(FileProperty.Id(0L, "rFile"))
        state = state.addFile(file)
        assertEquals(1, state.files.size)
        assertEquals(file, state.files[0])
    }

    fun testAddFileWhenAddLocalFile() {
        var state = NoteEditingState()
        val file = AppFile.Local(
            "local", "path", "", "", false, "",
        )
        state = state.addFile(file)
        assertEquals(1, state.files.size)
        assertEquals(file, state.files.first())
    }

    fun testAddFileWhenAddLocalAndRemoteFile() {
        var state = NoteEditingState()
        val local = AppFile.Local(
            "local", "path", "", "", false, "",
        )
        val remote = AppFile.Remote(FileProperty.Id(0L, "rFile"))

        state = state.addFile(local)
        state = state.addFile(remote)
        assertEquals(2, state.files.size)
        assertEquals(local, state.files.first())
        assertEquals(remote, state.files.last())
    }

    fun testRemoveFile() {
        val local = AppFile.Local(
            "local", "path", "", "", false, "",
        )
        val remote = AppFile.Remote(FileProperty.Id(0L, "rFile"))
        var state = NoteEditingState(
            files = listOf(local, remote)
        )

        state = state.removeFile(local)
        assertEquals(1, state.files.size)
        assertEquals(remote, state.files.first())


    }


    fun testSetAccount() {
        val account = Account("", "", "", "", emptyList(), Account.InstanceType.MISSKEY, 1)
        val state = NoteEditingState()
            .setAccount(account)
        assertEquals(account, state.author)
    }


    fun testRemovePollChoice() {
        var state = NoteEditingState()
        state = state.togglePoll()
        state = state.addPollChoice()
            .addPollChoice()
            .addPollChoice()
            .addPollChoice()
        val id = state.poll!!.choices.first().id
        state = state.removePollChoice(id)
        assertFalse(state.poll!!.choices.any { it.id == id })
        assertEquals(3, state.poll?.choices?.size)
    }

    fun testAddPollChoice() {
        var state = NoteEditingState()
        state = state.togglePoll()
        state = state.addPollChoice()
        assertEquals(1, state.poll?.choices?.size)
    }

    fun testUpdatePollChoice() {
        var state = NoteEditingState()
            .togglePoll()
            .addPollChoice()
            .addPollChoice()
        val targetChoice = state.poll?.choices?.first()!!
        state = state.updatePollChoice(targetChoice.id, "ほげ")
        assertEquals("ほげ", state.poll!!.choices.first().text)
    }

    fun testToggleCw() {
        var state = NoteEditingState(
            cw = "ほげ"
        )
        assertEquals("ほげ", state.cw)

        state = state.toggleCw()
        assertNull(state.cw)

        state = state.toggleCw()
        assertNotNull(state.cw)
    }

    fun testTogglePoll() {
        var state = NoteEditingState()
        assertNull(state.poll)
        state = state.togglePoll()
        assertNotNull(state.poll)

        state = state.togglePoll()
        assertNull(state.poll)
    }

    fun testClear() {
        val initialState = NoteEditingState()
        assertEquals(
            initialState, initialState
                .changeText("ほげ")
                .changeCw("ぴよ")
                .togglePoll()
                .addFile(AppFile.Remote(FileProperty.Id(0, "")))
                .clear()
        )
    }

    fun testAddMentionWhenOneUserName() {
        val initialState = NoteEditingState()
        val userNames = listOf("@Panta")
        val result: AddMentionResult = initialState.addMentionUserNames(userNames, 0)
        assertEquals((userNames[0]).length + 1, result.cursorPos)
        assertEquals("@Panta ", result.state.text)
    }

    fun testAddMentionWhenTwoUserNames() {
        val initialState = NoteEditingState()
        val userNames = listOf("@Panta", "@Panta@example.com")
        val result: AddMentionResult = initialState.addMentionUserNames(userNames, 0)
        assertEquals((userNames[0] + userNames[1]).length + 2 + 1, result.cursorPos)
        assertEquals("@Panta \n@Panta@example.com ", result.state.text)
    }

    fun testAddMentionWhenInsertOneUserName() {
        val initialState = NoteEditingState(text = "Hello")
        val userNames = listOf("@Panta")
        val result: AddMentionResult = initialState.addMentionUserNames(userNames, 0)
        assertEquals(userNames[0].length + 1 + 0, result.cursorPos)
    }

    fun testToggleFilesSensitive() {
        val targetFile = AppFile.Local("test", "/test/test2", "image/jpeg", null, false, null, 0)
        val state = NoteEditingState(
            text = "hello",
            files = listOf(
                AppFile.Remote(FileProperty.Id(0, "id1")),
                AppFile.Remote(FileProperty.Id(0, "id2")),
                AppFile.Local("test", "/test/test1", "image/jpeg", null, false, null, 0),
                AppFile.Local("test", "/test/test2", "image/jpeg", null, false, null, 0),
                AppFile.Local("test", "/test/test3", "image/jpeg", null, false, null, 0)
            )
        )
        val updatedState = state.toggleFileSensitiveStatus(targetFile)
        val expectedFile = targetFile.copy(isSensitive = true)
        assertEquals(expectedFile, updatedState.files[3])
        assertNotSame(expectedFile, updatedState.files[0])
        assertNotSame(expectedFile, updatedState.files[1])
        assertNotSame(expectedFile, updatedState.files[2])
        assertNotSame(expectedFile, updatedState.files[4])
    }

    fun testSetChannelId() {
        var state = NoteEditingState(
            visibility = Visibility.Followers(false)
        )

        val channelId = Channel.Id(0, "test1")
        state = state.setChannelId(channelId)
        assertEquals(channelId, state.channelId)

        assertEquals(Visibility.Public(true), state.visibility)
    }


    fun testCheckValidateWhenHasChannelId() {
        val channelId = Channel.Id(0, "test1")
        var state = NoteEditingState(visibility = Visibility.Public(true), channelId = channelId, text = "hoge")
        assertTrue(state.checkValidate())

        state = state.copy(visibility = Visibility.Public(false))
        assertFalse(state.checkValidate())

        state = state.copy(visibility = Visibility.Followers(true))
        assertFalse(state.checkValidate())
    }

    fun testSetVisibilityWhenSatChannelId() {
        val channelId = Channel.Id(0, "test1")
        var state = NoteEditingState(channelId = channelId)
        state = state.setVisibility(
            visibility = Visibility.Public(false)
        )
        assertEquals(Visibility.Public(true), state.visibility)
        state = state.setVisibility(Visibility.Followers(true))
        assertEquals(Visibility.Public(true), state.visibility)
    }

    fun testSetVisibility() {
        var state = NoteEditingState()
        state = state.setVisibility(Visibility.Public(false))
        assertEquals(Visibility.Public(false), state.visibility)
        state = state.setVisibility(Visibility.Home(true))
        assertEquals(Visibility.Home(true), state.visibility)
    }
}
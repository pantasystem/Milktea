package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.file.AppFile
import junit.framework.TestCase

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

    fun testCheckValidationWhenOverFileCountsOfFailure() {
        val state = NoteEditingState(files = listOf("a", "b", "c", "d", "e").map {
            AppFile.Remote(FileProperty.Id(0L, it))
        })
        assertFalse(state.checkValidate())
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

    fun testAddFile() {}

    fun testRemoveFile() {}

    fun testChangePollExpiresAt() {}

    fun testSetAccount() {}

    fun testRemovePollChoice() {}

    fun testAddPollChoice() {}

    fun testUpdatePollChoice() {}

    fun testToggleCw() {}

    fun testTogglePoll() {}

    fun testClear() {}
}
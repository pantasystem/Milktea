package net.pantasystem.milktea.data.infrastructure.settings

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.ReactionPickerType
import net.pantasystem.milktea.model.setting.RememberVisibility


class SettingStore(
    private val sharedPreferences: SharedPreferences,
    private val localConfigRepository: LocalConfigRepository,
    coroutineScope: CoroutineScope
) {

    val configState = localConfigRepository.observe()
        .stateIn(coroutineScope, SharingStarted.Eagerly, DefaultConfig.config)

    val isSimpleEditorEnabled: Boolean
        get() {
            return configState.value.isSimpleEditorEnabled
        }


    var reactionPickerType: ReactionPickerType
        get() {
            return configState.value.reactionPickerType
        }
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(Keys.ReactionPickerType.str(), value.ordinal)
            editor.apply()
        }

    var backgroundImagePath: String?
        get() {
            return configState.value.backgroundImagePath
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putString(Keys.BackgroundImage.str(), value)
            edit.apply()
        }

    val isClassicUI: Boolean
        get() {
            return configState.value.isClassicUI
        }


    val isUserNameDefault: Boolean
        get() {
            return configState.value.isUserNameDefault
        }


    val isPostButtonAtTheBottom: Boolean
        get() {
            return configState.value.isPostButtonAtTheBottom
        }


    val urlPreviewSetting = UrlPreviewSourceSetting(sharedPreferences)

    val noteExpandedHeightSize: Int
        get() {
            return configState.value.noteExpandedHeightSize
        }


    suspend fun setNoteVisibility(createNote: CreateNote) {
        if (!(createNote.renoteId == null && createNote.replyId == null)) {
            return
        }
        val nowConfig =
            (localConfigRepository.getRememberVisibility(createNote.author.accountId).getOrThrow())

        when (nowConfig) {
            is RememberVisibility.None -> return
            is RememberVisibility.Remember -> localConfigRepository.save(
                nowConfig.copy(visibility = createNote.visibility)
            )
        }

    }

    fun getNoteVisibility(accountId: Long): Visibility {
        return when (val config =
            localConfigRepository.getRememberVisibility(accountId).getOrThrow()) {
            is RememberVisibility.None -> Visibility.Public(false)
            is RememberVisibility.Remember -> config.visibility
        }
    }

}
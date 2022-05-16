package net.pantasystem.milktea.data.infrastructure.settings

import android.content.SharedPreferences
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.ReactionPickerType
import net.pantasystem.milktea.model.setting.RememberVisibility


class SettingStore(
    private val sharedPreferences: SharedPreferences,
    private val localConfigRepository: LocalConfigRepository
) {


    val isSimpleEditorEnabled: Boolean
        get() {
            return localConfigRepository.get().getOrThrow().isSimpleEditorEnabled
        }


    var reactionPickerType: ReactionPickerType
        get() {
            return localConfigRepository.get().getOrThrow().reactionPickerType
        }
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(Keys.ReactionPickerType.str(), value.ordinal)
            editor.apply()
        }

    var backgroundImagePath: String?
        get() {
            return localConfigRepository.get().getOrThrow().backgroundImagePath
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putString(Keys.BackgroundImage.str(), value)
            edit.apply()
        }

    var isClassicUI: Boolean
        get() {
            return localConfigRepository.get().getOrThrow().isClassicUI
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putBoolean(Keys.ClassicUI.str(), value)
            edit.apply()
        }

    var isUserNameDefault: Boolean
        get() {
            return localConfigRepository.get().getOrThrow().isUserNameDefault
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putBoolean(Keys.IsUserNameDefault.str(), value)
            edit.apply()
        }

    var isPostButtonAtTheBottom: Boolean
        get() {
            return localConfigRepository.get().getOrThrow().isPostButtonAtTheBottom
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putBoolean(Keys.IsPostButtonToBottom.str(), value)
            edit.apply()
        }


    val urlPreviewSetting = UrlPreviewSourceSetting(sharedPreferences)

    val noteExpandedHeightSize: Int
        get() {
            return localConfigRepository.get().getOrThrow().noteExpandedHeightSize
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
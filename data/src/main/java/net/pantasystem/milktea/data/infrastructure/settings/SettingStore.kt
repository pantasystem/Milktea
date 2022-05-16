package net.pantasystem.milktea.data.infrastructure.settings

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import net.pantasystem.milktea.data.infrastructure.KeyStore
import net.pantasystem.milktea.model.notes.CanLocalOnly
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.ReactionPickerType


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
            editor.putInt("ReactionPickerType", value.ordinal)
            editor.apply()
        }

    var backgroundImagePath: String?
        get() {
            return localConfigRepository.get().getOrThrow().backgroundImagePath
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putString("BackgroundImage", value)
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

    private var isLearnVisibility: Boolean
        set(value) {
            sharedPreferences.edit {
                putBoolean(KeyStore.BooleanKey.IS_LEARN_NOTE_VISIBILITY.name, value)
            }
        }
        get() {
            return sharedPreferences.getBoolean(
                KeyStore.BooleanKey.IS_LEARN_NOTE_VISIBILITY.name,
                true
            )
        }


    fun setNoteVisibility(createNote: CreateNote) {
        if (!isLearnVisibility) {
            return
        }
        if (!(createNote.renoteId == null && createNote.replyId == null)) {
            return
        }
        val localOnly = (createNote.visibility as? CanLocalOnly)?.isLocalOnly ?: false
        val str = when (createNote.visibility) {
            is Visibility.Public -> "public"
            is Visibility.Home -> "home"
            is Visibility.Followers -> "followers"
            is Visibility.Specified -> "specified"
        }
        Log.d("SettingStore", "visibility: $str")
        sharedPreferences.edit {
            putString("accountId:${createNote.author.accountId}:NOTE_VISIBILITY", str)
            putBoolean("accountId:${createNote.author.accountId}:IS_LOCAL_ONLY", localOnly)
        }
    }

    fun getNoteVisibility(accountId: Long): Visibility {
        if (!isLearnVisibility) {
            return Visibility.Public(false)
        }
        val localOnly = sharedPreferences.getBoolean("accountId:${accountId}:IS_LOCAL_ONLY", false)
        return when (sharedPreferences.getString(
            "accountId:${accountId}:NOTE_VISIBILITY",
            "public"
        )) {
            "home" -> Visibility.Home(localOnly)
            "followers" -> Visibility.Followers(localOnly)
            "specified" -> Visibility.Specified(emptyList())
            else -> Visibility.Public(localOnly)
        }
    }

}
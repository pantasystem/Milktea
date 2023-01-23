package net.pantasystem.milktea.app_store.setting

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.setting.*


class SettingStore(
    private val sharedPreferences: SharedPreferences,
    private val localConfigRepository: LocalConfigRepository,
    coroutineScope: CoroutineScope
): NoteExpandedHeightSize {

    val configState = localConfigRepository.observe()
        .catch { e ->
            Log.e("SettingStore", "設定取得エラー", e)
        }
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



    override fun getNoteExpandedHeightSize(): Int {
        return configState.value.noteExpandedHeightSize
    }




    fun getNoteVisibility(accountId: Long): Visibility {
        return when (val config =
            localConfigRepository.getRememberVisibility(accountId).getOrThrow()) {
            is RememberVisibility.None -> Visibility.Public(false)
            is RememberVisibility.Remember -> config.visibility
        }
    }

}
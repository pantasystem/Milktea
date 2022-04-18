package net.pantasystem.milktea.data.model.settings

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import net.pantasystem.milktea.data.model.KeyStore
import net.pantasystem.milktea.model.notes.CanLocalOnly
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Visibility

class SettingStore(private val sharedPreferences: SharedPreferences) {


    val isSimpleEditorEnabled: Boolean
        get(){
            return fromBooleanEnum(KeyStore.BooleanKey.IS_SIMPLE_EDITOR_ENABLED)
        }


    var reactionPickerType: ReactionPickerType
        get(){
            return ReactionPickerType.values()[sharedPreferences.getInt("ReactionPickerType", 0)]
        }
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt("ReactionPickerType", value.ordinal)
            editor.apply()
        }

    var backgroundImagePath: String?
        get(){
            return sharedPreferences.getString("BackgroundImage", null)
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putString("BackgroundImage", value)
            edit.apply()
        }

    var isClassicUI: Boolean
        get(){
            return sharedPreferences.getBoolean(KeyStore.BooleanKey.HIDE_BOTTOM_NAVIGATION.name, false)
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putBoolean(KeyStore.BooleanKey.HIDE_BOTTOM_NAVIGATION.name, value)
            edit.apply()
        }

    var isUserNameDefault: Boolean
        get(){
            return sharedPreferences.getBoolean(KeyStore.BooleanKey.IS_USER_NAME_DEFAULT.name, true)
        }
        set(value){
            val edit = sharedPreferences.edit()
            edit.putBoolean(KeyStore.BooleanKey.IS_USER_NAME_DEFAULT.name, value)
            edit.apply()
        }

    var isPostButtonAtTheBottom: Boolean
        get(){
            return sharedPreferences.getBoolean(KeyStore.BooleanKey.IS_POST_BUTTON_TO_BOTTOM.name, KeyStore.BooleanKey.IS_POST_BUTTON_TO_BOTTOM.default)
        }
        set(value) {
            val edit = sharedPreferences.edit()
            edit.putBoolean(KeyStore.BooleanKey.IS_POST_BUTTON_TO_BOTTOM.name, value)
            edit.apply()
        }
    private fun fromBooleanEnum(key: KeyStore.BooleanKey): Boolean{
        return sharedPreferences.getBoolean(key.name, key.default)
    }

    val urlPreviewSetting = UrlPreviewSourceSetting(sharedPreferences)

    val noteExpandedHeightSize: Int
        get() {
            return sharedPreferences.getInt(KeyStore.AutoNoteExpandedContentSize.HEIGHT.name, KeyStore.AutoNoteExpandedContentSize.HEIGHT.default)
        }

    private var isLearnVisibility: Boolean
        set(value) {
            sharedPreferences.edit {
                putBoolean(KeyStore.BooleanKey.IS_LEARN_NOTE_VISIBILITY.name, value)
            }
        }
        get() {
            return sharedPreferences.getBoolean(KeyStore.BooleanKey.IS_LEARN_NOTE_VISIBILITY.name, true)
        }


    fun setNoteVisibility(createNote: CreateNote) {
        if(!isLearnVisibility) {
            return
        }
        if(!(createNote.renoteId == null && createNote.replyId == null)) {
            return
        }
        val localOnly = (createNote.visibility as? CanLocalOnly)?.isLocalOnly?: false
        val str = when(createNote.visibility) {
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
        if(!isLearnVisibility) {
            return Visibility.Public(false)
        }
        val localOnly = sharedPreferences.getBoolean("accountId:${accountId}:IS_LOCAL_ONLY", false)
        return when(sharedPreferences.getString("accountId:${accountId}:NOTE_VISIBILITY", "public")) {
            "home"-> Visibility.Home(localOnly)
            "followers" -> Visibility.Followers(localOnly)
            "specified" -> Visibility.Specified(emptyList())
            else -> Visibility.Public(localOnly)
        }
    }

}
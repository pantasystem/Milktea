package jp.panta.misskeyandroidclient.model.settings

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.model.notes.CanLocalOnly
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.notes.Visibility

class SettingStore(private val sharedPreferences: SharedPreferences) {



    val isUpdateTimelineInBackground: Boolean
        get(){
            return fromBooleanEnum(KeyStore.BooleanKey.UPDATE_TIMELINE_IN_BACKGROUND)
        }

    val isAutoLoadTimeline: Boolean
    get() {
        return fromBooleanEnum(KeyStore.BooleanKey.AUTO_LOAD_TIMELINE)
    }

    val isSimpleEditorEnabled: Boolean
        get(){
            return fromBooleanEnum(KeyStore.BooleanKey.IS_SIMPLE_EDITOR_ENABLED)
        }


    val isHideRemovedNote: Boolean
        get(){
            return fromBooleanEnum(KeyStore.BooleanKey.HIDE_REMOVED_NOTE)
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

    val foldingTextLengthLimit: Int
        get(){
            return sharedPreferences.getInt(KeyStore.AutoTextFoldingCount.LENGTH.name,  KeyStore.AutoTextFoldingCount.LENGTH.default)
        }

    val foldingTextReturnsLimit: Int
        get(){
            return sharedPreferences.getInt(KeyStore.AutoTextFoldingCount.RETURNS.name, KeyStore.AutoTextFoldingCount.RETURNS.default)
        }

    var isLearnVisibility: Boolean
        set(value) {
            sharedPreferences.edit {
                putBoolean("IS_LEARN_VISIBILITY", value)
            }
        }
        get() {
            return sharedPreferences.getBoolean("IS_LEAR_VISIBILITY", true)
        }

    private var isVisibleLocalOnly: Boolean
        set(value) {
            sharedPreferences.edit {
                putBoolean("IS_VISIBLE_LOCAL_ONLY", value)
            }
        }
        get() {
            return sharedPreferences.getBoolean("IS_VISIBLE_LOCAL_ONLY", false)
        }

    fun setNoteVisibility(createNote: CreateNote) {
        if(!isLearnVisibility) {
            return
        }
        if(!(createNote.renoteId == null && createNote.replyId == null)) {
            return
        }
        isVisibleLocalOnly = (createNote.visibility as? CanLocalOnly)?.isLocalOnly?: false
        val str = when(createNote.visibility) {
            is Visibility.Public -> "public"
            is Visibility.Home -> "home"
            is Visibility.Followers -> "followers"
            is Visibility.Specified -> "specified"
        }
        Log.d("SettingStore", "visibility: $str")
        sharedPreferences.edit { putString("accountId:${createNote.author.accountId}:NOTE_VISIBILITY", str) }
    }

    fun getNoteVisibility(accountId: Long): Visibility {
        return when(sharedPreferences.getString("accountId:${accountId}:NOTE_VISIBILITY", "public")) {
            "home"-> Visibility.Home(isVisibleLocalOnly)
            "followers" -> Visibility.Followers(isVisibleLocalOnly)
            "specified" -> Visibility.Specified(emptyList())
            else -> Visibility.Public(isVisibleLocalOnly)
        }
    }
}
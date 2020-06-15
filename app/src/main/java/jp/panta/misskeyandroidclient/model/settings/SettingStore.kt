package jp.panta.misskeyandroidclient.model.settings

import android.content.SharedPreferences
import jp.panta.misskeyandroidclient.KeyStore

class SettingStore(private val sharedPreferences: SharedPreferences) {

    val isUpdateTimelineInBackground: Boolean
        get(){
            return fromBooleanEnum(KeyStore.BooleanKey.UPDATE_TIMELINE_IN_BACKGROUND)
        }

    val isAutoLoadTimeline: Boolean
    get() {
        return fromBooleanEnum(KeyStore.BooleanKey.AUTO_LOAD_TIMELINE)
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
}
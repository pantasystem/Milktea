package jp.panta.misskeyandroidclient.model.settings

import android.content.SharedPreferences
import jp.panta.misskeyandroidclient.KeyStore

class SettingStore(private val sharedPreferences: SharedPreferences) {

    val isCaptureNoteWhenStopped: Boolean
        get() {
            return fromBooleanEnum(KeyStore.BooleanKey.CAPTURE_NOTE_WHEN_STOPPED)
        }

    val isAutoLoadTimeline: Boolean
    get() {
        return fromBooleanEnum(KeyStore.BooleanKey.AUTO_LOAD_TIMELINE)
    }

    val isAutoLoadTimelineWhenStopped: Boolean
        get(){
            return fromBooleanEnum(KeyStore.BooleanKey.AUTO_LOAD_TIMELINE_WHEN_STOPPED)
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

    private fun fromBooleanEnum(key: KeyStore.BooleanKey): Boolean{
        return sharedPreferences.getBoolean(key.name, key.default)
    }
}
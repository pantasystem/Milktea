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

    private fun fromBooleanEnum(key: KeyStore.BooleanKey): Boolean{
        return sharedPreferences.getBoolean(key.name, key.default)
    }
}
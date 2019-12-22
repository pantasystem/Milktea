package jp.panta.misskeyandroidclient.viewmodel.setting.tab

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType

sealed class SettingTab{
    abstract val title: MutableLiveData<String>
    abstract fun toSetting() : NoteRequest.Setting?
    abstract val type: NoteType
    class FromType(override val type: NoteType) : SettingTab(){
        override val title: MutableLiveData<String> = MutableLiveData(type.defaultName)
        override fun toSetting(): NoteRequest.Setting? {
            val s = NoteRequest.Setting(type = type)
            s.title = this.title.value?: type.defaultName

            return s
        }
    }

    class FromSetting(val setting: NoteRequest.Setting) : SettingTab(){
        override val type: NoteType = setting.type
        override val title: MutableLiveData<String> = MutableLiveData(setting.title)
        override fun toSetting(): NoteRequest.Setting? {
            val t = title.value
            if(t != null){
                setting.title = t
            }

            return setting
        }
    }
}
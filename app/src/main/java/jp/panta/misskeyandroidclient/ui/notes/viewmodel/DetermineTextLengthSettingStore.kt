package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import jp.panta.misskeyandroidclient.model.settings.SettingStore
import java.util.regex.Pattern

class DetermineTextLengthSettingStore(
    private val settingStore: SettingStore
)  : DetermineTextLength {
    private var mText: String? = null

    override fun isLong(): Boolean {

        val text = mText?: return false

        val length = settingStore.foldingTextLengthLimit
        val lineBreaks = settingStore.foldingTextReturnsLimit

        if(text.codePointCount(0, text.length) >= length){
            return true
        }

        var lineBreakCount = 0
        val pattern = Pattern.compile("""\r\n|\n|\r """)
        val matcher = pattern.matcher(text)
        while(matcher.find()){
            lineBreakCount++
            if(lineBreakCount >= lineBreaks){
                return true
            }
        }
        return false
    }

    override fun setText(text: String?) {
        mText = text
    }

    override fun clone(): DetermineTextLength {
        return DetermineTextLengthSettingStore(settingStore)
    }
}
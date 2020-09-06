package jp.panta.misskeyandroidclient.viewmodel.notes

import java.util.regex.Pattern

class DetermineTextLengthImpl(val length: Int, val lineBreaks: Int) : DetermineTextLength{

     private var mText: String? = null

    override fun isLong(): Boolean {
        val text = mText?: return false

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
}
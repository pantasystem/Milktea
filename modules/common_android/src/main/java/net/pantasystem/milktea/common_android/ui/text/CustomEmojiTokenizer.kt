package net.pantasystem.milktea.common_android.ui.text

import android.util.Log
import android.widget.MultiAutoCompleteTextView

class CustomEmojiTokenizer : MultiAutoCompleteTextView.Tokenizer{
    var start: Int? = null
        private set

    override fun findTokenStart(text: CharSequence?, cursor: Int): Int {
        Log.d("CustomEmojiTokenizer", "findToTokenStart text: $text, cursor:$cursor")
        var i = cursor
        val textLength = text?.length?: 0

        while(i > 0){
            if(text?.get(i - 1) == ':'){
                start = i
                return i
            }else{
                i--
            }
        }
        return textLength
    }

    override fun findTokenEnd(text: CharSequence?, cursor: Int): Int {
        Log.d("CustomEmojiTokenizer", "findTokenEnd text: $text, cursor:$cursor")

        var i = cursor
        if(i > 0 && text?.get(i) == ':'){
            return i
        }

        while(i > 0 && text?.get(i - 1) != ':'){
            i --
        }

        return i
    }



    override fun terminateToken(text: CharSequence?): CharSequence {
        Log.d("CustomEmojiTokenizer", "terminateToken text: $text")

        return text?.substring(1, text.length)?: String()
    }
}
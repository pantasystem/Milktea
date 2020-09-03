package jp.panta.misskeyandroidclient.viewmodel.notes

interface DetermineTextLength {

    fun isLong(): Boolean

    fun setText(text: String?)
}
package jp.panta.misskeyandroidclient.ui.notes.viewmodel

interface DetermineTextLength : Cloneable {

    fun isLong(): Boolean

    fun setText(text: String?)

    public override fun clone(): DetermineTextLength
}
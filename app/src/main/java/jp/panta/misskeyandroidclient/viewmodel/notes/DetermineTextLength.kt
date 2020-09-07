package jp.panta.misskeyandroidclient.viewmodel.notes

interface DetermineTextLength : Cloneable {

    fun isLong(): Boolean

    fun setText(text: String?)

    public override fun clone(): DetermineTextLength
}
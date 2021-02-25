package jp.panta.misskeyandroidclient.streaming.notes


interface NoteCaptureAPIProvider {

    fun get(accountId: Long) : NoteCaptureAPI?
}
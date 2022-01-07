package jp.panta.misskeyandroidclient.model.core

@Deprecated("model.accountへ移行")
enum class ConnectionStatus(val flag: Int){
    ACCOUNT_ERROR(1),
    NETWORK_ERROR(2),
    SUCCESS(8)
}
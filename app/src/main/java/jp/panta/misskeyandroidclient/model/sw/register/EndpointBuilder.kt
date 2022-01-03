package jp.panta.misskeyandroidclient.model.sw.register

import jp.panta.misskeyandroidclient.BuildConfig

data class EndpointBuilder(
    var deviceToken: String,
    var accountId: Long,
    var lang: String,
    val endpointBase: String = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
    val auth: String = BuildConfig.PUSH_TO_FCM_AUTH,
    val publicKey: String = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY
) {

    fun build(): String {
        return "$endpointBase/webpushcallback?deviceToken=${deviceToken}&accountId=${accountId}&lang=${lang}"
    }
}
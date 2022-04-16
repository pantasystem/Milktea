package net.pantasystem.milktea.data.model.sw.register

import net.pantasystem.milktea.data.BuildConfig

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
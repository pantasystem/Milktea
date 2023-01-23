package net.pantasystem.milktea.data.infrastructure.sw.register


data class EndpointBuilder(
    var deviceToken: String,
    var accountId: Long,
    var lang: String,
    val endpointBase: String,
    val auth: String,
    val publicKey: String,
) {

    fun build(): String {
        return "$endpointBase/webpushcallback?deviceToken=${deviceToken}&accountId=${accountId}&lang=${lang}"
    }
}
package net.pantasystem.milktea.model.account


import net.pantasystem.milktea.model.account.page.Page
import java.io.Serializable
import java.net.URL

data class Account(
    val remoteId: String,
    val instanceDomain: String,
    val userName: String,
    val token: String,
    val pages: List<Page>,
    val instanceType: InstanceType,
    var accountId: Long = 0
) : Serializable {

    enum class InstanceType {
        MISSKEY, MASTODON
    }

    constructor(
        remoteId: String,
        instanceDomain: String,
        userName: String,
        instanceType: InstanceType,
        token: String
    ) :
            this(
                remoteId,
                instanceDomain,
                userName,
                token,
                emptyList(),
                instanceType
            )

    val normalizedInstanceDomain: String by lazy {
        val url = URL(instanceDomain)
        var str = "${url.protocol}://${url.host}"
        if (url.port != -1) {
            if (url.port != 80 || url.port != 443) {
                str += ":${url.port}"
            }
        }
        str
    }

    fun getHost(): String {
        if (instanceDomain.startsWith("https://")) {
            return URL(instanceDomain).host

        } else if (instanceDomain.startsWith("http://")) {
            return URL(instanceDomain).host
        }
        return instanceDomain
    }


}

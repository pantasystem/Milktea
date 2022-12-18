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

        val protocol = getProtocol().ifBlank {
            "https"
        }
        var str = "${protocol}://${getProtocolLess()}"
        val url = URL(str)

        str = "${protocol}://${getHost()}"
        if (url.port != -1) {
            str += ":${url.port}"
        }
        str
    }

    fun getHost(): String {
        val protocol = getProtocol()
        val instanceDomain = instanceDomain.trim()
        val protocolLess = getProtocolLess()
        if (instanceDomain.startsWith("https://")) {
            return URL("$protocol://$protocolLess").host

        } else if (instanceDomain.startsWith("http://")) {
            return URL("$protocol://$protocolLess").host
        } else if (instanceDomain.indexOf("://") > 0) {
            return URL("$protocol://$protocolLess").host
        }
        return instanceDomain
    }

    private fun getProtocol(): String {
        val instanceDomain = instanceDomain.trim()
        if (instanceDomain.startsWith("https://")) {
            return URL(instanceDomain).protocol
        } else if (instanceDomain.startsWith("http://")) {
            return URL(instanceDomain).protocol
        } else if (instanceDomain.indexOf("://") > 0) {
            return URL(instanceDomain).protocol
        }
        return ""
    }

    private fun getProtocolLess(): String {
        val protocol = getProtocol()
        val instanceDomain = instanceDomain.trim()
        var protocolLess = instanceDomain.substring(protocol.length, instanceDomain.length)
        while(protocolLess.startsWith(":")) {
            protocolLess = protocolLess.substring(":".length, protocolLess.length)
        }
        while(protocolLess.startsWith("/")) {
            protocolLess = protocolLess.substring("/".length, protocolLess.length)
        }
        return protocolLess
    }

}

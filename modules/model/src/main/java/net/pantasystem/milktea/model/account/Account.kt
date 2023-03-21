package net.pantasystem.milktea.model.account


import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.user.Acct
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

    val normalizedInstanceDomain: String = getNormalizedDomain()


    private var _host: String? = null

    private fun getNormalizedDomain(): String {
        val protocol = getProtocol().ifBlank {
            "https"
        }
        var str = "${protocol}://${getProtocolLess()}"
        val url = URL(str)

        str = "${protocol}://${getHost()}"
        if (url.port != -1) {
            str += ":${url.port}"
        }
        return str
    }

    fun getHost(): String {
        when(val h = _host) {
            null -> Unit
            else -> return h
        }

        val protocol = getProtocol()
        val instanceDomain = instanceDomain.trim()
        val protocolLess = getProtocolLess()

        val host = if (instanceDomain.startsWith("https://")) {
            URL("$protocol://$protocolLess").host

        } else if (instanceDomain.startsWith("http://")) {
            URL("$protocol://$protocolLess").host
        } else if (instanceDomain.indexOf("://") > 0) {
            URL("$protocol://$protocolLess").host
        } else {
            ""
        }
        if (host.isBlank()) {
            if (protocolLess.startsWith("@")){
                val acct = Acct(protocolLess)
                if (!acct.host.isNullOrBlank()) {
                    _host = acct.host
                    return acct.host
                }
            }
        } else {
            _host = host
            return host
        }
        _host = protocolLess
        return protocolLess
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

package net.pantasystem.milktea.model.account


import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.user.Acct
import java.io.Serializable


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

    val normalizedInstanceUri: String = getNormalizedDomain()


    private var _host: String? = null

    private fun getNormalizedDomain(): String {
        val protocol = getProtocol().ifBlank {
            "https"
        }

        val str = "${protocol}://${getHost()}"

        return when(val port = getPort()) {
            null -> str
            else -> "$str:$port"
        }
    }

    fun getHost(): String {
        when(val h = _host) {
            null -> Unit
            else -> return h
        }

        val instanceDomain = instanceDomain.trim()
        val protocolLess = getProtocolLess().trim()

        val host = if (
            instanceDomain.startsWith("https://")
            || instanceDomain.startsWith("http://")
            || instanceDomain.indexOf("://") > 0
        ) {
            var url = protocolLess.lowercase()
            while(url.lastOrNull() == '/') {
                url = url.substring(0, url.lastIndex)
            }

            // check ipv6 address
            if (!url.startsWith("[") || !url.endsWith("]")) {
                val portColon = url.indexOf(":")
                if (portColon != -1) {
                    url = url.substring(0, portColon)
                }
            }

            // check domain pattern
            if (
                url.contains("@")
                || url.startsWith("\\d")
            ) {
                ""
            } else {
                url
            }
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
            return "https"
        } else if (instanceDomain.startsWith("http://")) {
            return "http"
        } else if (instanceDomain.indexOf("://") > 0) {
            var protocol = instanceDomain.substring(0, instanceDomain.indexOf("://"))
            while(protocol.lastOrNull() == ':') {
                protocol = protocol.substring(0, protocol.lastIndex)
            }
            return protocol.lowercase()
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

        while(protocolLess.lastOrNull() == '/') {
            protocolLess = protocolLess.substring(0, protocolLess.lastIndex)
        }
        return protocolLess
    }

    private fun getPort(): String? {
        val protocolLess = getProtocolLess().trim()

        // is ipv6 address
        if (protocolLess.startsWith("[") && protocolLess.endsWith("]")) {
            return null
        }

        val portColon = protocolLess.indexOf(":")
        if (portColon != -1 && portColon < protocolLess.length - 1) {
             return protocolLess.substring(portColon + 1, protocolLess.length)
        }
        return null
    }
}

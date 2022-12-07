package net.pantasystem.milktea.model.account


import net.pantasystem.milktea.model.account.page.Page
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



    fun getHost(): String {
        if (instanceDomain.startsWith("https://")) {
            return instanceDomain.substring("https://".length, instanceDomain.length)
        } else if (instanceDomain.startsWith("http://")) {
            return instanceDomain.substring("http://".length, instanceDomain.length)
        }
        return instanceDomain
    }


}

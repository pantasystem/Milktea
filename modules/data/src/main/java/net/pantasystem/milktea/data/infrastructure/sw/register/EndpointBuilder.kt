package net.pantasystem.milktea.data.infrastructure.sw.register

import net.pantasystem.milktea.model.account.Account


data class EndpointBuilder(
    var deviceToken: String,
    var accountId: Long,
    var lang: String,
    val endpointBase: String,
    val auth: String,
    val publicKey: String,
    val instanceType: Account.InstanceType,
) {

    fun build(): String {
        return when(instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                "$endpointBase/webpushcallback?deviceToken=${deviceToken}&accountId=${accountId}&lang=${lang}"
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                "$endpointBase/webpushcallback-4-mastodon?deviceToken=${deviceToken}&accountId=${accountId}&lang=${lang}"
            }
        }
    }
}
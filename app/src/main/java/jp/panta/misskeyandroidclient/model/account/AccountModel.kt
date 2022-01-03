package jp.panta.misskeyandroidclient.model.account

interface AccountModel {

    @Throws(AccountNotFoundException::class)
    suspend fun get(accountId: Long): Account

    @Throws(AccountNotFoundException::class)
    suspend fun switch(accountId: Long): Account

    suspend fun accounts(): List<Account>

}
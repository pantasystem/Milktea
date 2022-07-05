package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository

interface MiCore {



    fun getUrlPreviewStore(account: Account): UrlPreviewStore?


    fun getUserDataSource(): UserDataSource

    fun getUserRepository(): UserRepository


    fun getSubscriptionRegistration(): SubscriptionRegistration


    fun getEncryption(): Encryption


    fun getCurrentInstanceMeta(): Meta?


    fun getSettingStore(): SettingStore


    fun getMisskeyAPIProvider(): MisskeyAPIProvider


    fun getMetaRepository(): MetaRepository

    fun getReactionHistoryDataSource(): ReactionHistoryDataSource


    fun getAccountStore(): AccountStore
}
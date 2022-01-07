package jp.panta.misskeyandroidclient.di.module.messaging

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import jp.panta.misskeyandroidclient.model.messaging.impl.InMemoryMessageDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MessagingModule {

    @Provides
    @Singleton
    fun inMemoryMessageDataSource(accountRepository: AccountRepository): InMemoryMessageDataSource {
        return InMemoryMessageDataSource(accountRepository)
    }

    @Provides
    @Singleton
    fun unreadMessages(inMem: InMemoryMessageDataSource): UnReadMessages {
        return inMem
    }

    @Provides
    @Singleton
    fun messageDataSource(inMem: InMemoryMessageDataSource): MessageDataSource {
        return inMem
    }
}
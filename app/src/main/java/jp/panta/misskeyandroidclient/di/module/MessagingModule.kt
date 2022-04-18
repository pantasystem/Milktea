package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.messaging.MessageRepository
import net.pantasystem.milktea.model.messaging.UnReadMessages
import net.pantasystem.milktea.data.infrastructure.messaging.impl.InMemoryMessageDataSource
import net.pantasystem.milktea.data.infrastructure.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.infrastructure.messaging.impl.MessageRepositoryImpl
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

@Module
@InstallIn(SingletonComponent::class)
abstract class MessagingBindsModule {

    @Binds
    @Singleton
    abstract fun messageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ) : MessageRepository

}
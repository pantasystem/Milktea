package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.db.AccountDAO
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.usercustom.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO
import jp.panta.misskeyandroidclient.model.users.impl.UserNicknameDAO
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun database(@ApplicationContext context: Context): DataBase {
        return Room.databaseBuilder(context, DataBase::class.java, "milk_database")
            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .addMigrations(MIGRATION_7_8)
            .addMigrations(MIGRATION_8_10)
            .addMigrations(MIGRATION_10_11)
            .build()
    }

    @Provides
    @Singleton
    fun accountDAO(db: DataBase): AccountDAO {
        return db.accountDAO()
    }

    @Provides
    @Singleton
    fun reactionUserSettingDAO(db: DataBase): ReactionUserSettingDao {
        return db.reactionUserSettingDao()
    }

    @Provides
    @Singleton
    fun reactionHistoryDao(db: DataBase): ReactionHistoryDao {
        return db.reactionHistoryDao()
    }

    @Provides
    @Singleton
    fun unreadNotificationDAO(db: DataBase): UnreadNotificationDAO = db.unreadNotificationDAO()

    @Provides
    @Singleton
    fun draftNoteDAO(db: DataBase): DraftNoteDao = db.draftNoteDao()

    @Provides
    @Singleton
    fun urlPreviewDAO(db: DataBase): UrlPreviewDAO = db.urlPreviewDAO()

    @Provides
    @Singleton
    fun userNicknameDAO(db: DataBase): UserNicknameDAO = db.userNicknameDAO()
}
package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.model.*
import net.pantasystem.milktea.data.model.account.db.AccountDAO
import net.pantasystem.milktea.data.model.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.model.url.db.UrlPreviewDAO
import net.pantasystem.milktea.data.model.user.impl.UserNicknameDAO
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
    fun reactionUserSettingDAO(db: DataBase): net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao {
        return db.reactionUserSettingDao()
    }

    @Provides
    @Singleton
    fun reactionHistoryDao(db: DataBase): net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao {
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
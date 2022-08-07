package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.*
import net.pantasystem.milktea.data.infrastructure.account.db.AccountDAO
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecordDao
import net.pantasystem.milktea.data.infrastructure.group.GroupDao
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.data.infrastructure.user.UserNicknameDAO
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao
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

    @Provides
    @Singleton
    fun driveFileDAO(db: DataBase): DriveFileRecordDao = db.driveFileRecordDAO()

    @Provides
    @Singleton
    fun groupDAO(db: DataBase): GroupDao = db.groupDao()
}
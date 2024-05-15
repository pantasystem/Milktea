package net.pantasystem.milktea.data.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.MIGRATION_10_11
import net.pantasystem.milktea.data.infrastructure.MIGRATION_1_2
import net.pantasystem.milktea.data.infrastructure.MIGRATION_2_3
import net.pantasystem.milktea.data.infrastructure.MIGRATION_3_4
import net.pantasystem.milktea.data.infrastructure.MIGRATION_4_5
import net.pantasystem.milktea.data.infrastructure.MIGRATION_51_52
import net.pantasystem.milktea.data.infrastructure.MIGRATION_57_58
import net.pantasystem.milktea.data.infrastructure.MIGRATION_5_6
import net.pantasystem.milktea.data.infrastructure.MIGRATION_6_7
import net.pantasystem.milktea.data.infrastructure.MIGRATION_7_8
import net.pantasystem.milktea.data.infrastructure.MIGRATION_8_10
import net.pantasystem.milktea.data.infrastructure.account.db.AccountDAO
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecordDao
import net.pantasystem.milktea.data.infrastructure.group.GroupDao
import net.pantasystem.milktea.data.infrastructure.instance.db.InstanceInfoDao
import net.pantasystem.milktea.data.infrastructure.instance.db.MastodonInstanceInfoDAO
import net.pantasystem.milktea.data.infrastructure.list.UserListDao
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoDao
import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.history.ReactionHistoryDao
import net.pantasystem.milktea.data.infrastructure.note.reaction.impl.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.data.infrastructure.note.wordmute.WordFilterConfigDao
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.search.SearchHistoryDao
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.data.infrastructure.user.UserNicknameDAO
import net.pantasystem.milktea.data.infrastructure.user.db.UserDao
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
            .addMigrations(MIGRATION_51_52)
            .addMigrations(MIGRATION_57_58)
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

    @Provides
    @Singleton
    fun provideUserDao(db: DataBase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideWordFilterDao(db: DataBase): WordFilterConfigDao = db.wordFilterConfigDao()

    @Provides
    @Singleton
    fun provideUserListDao(db: DataBase): UserListDao = db.userListDao()

    @Provides
    @Singleton
    fun provideInstanceInfoDao(db: DataBase): InstanceInfoDao = db.instanceInfoDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(db: DataBase): SearchHistoryDao = db.searchHistoryDao()

    @Provides
    @Singleton
    fun provideNodeInfoDao(db: DataBase): NodeInfoDao = db.nodeInfoDao()

    @Provides
    @Singleton
    fun provideMastodonInfoDao(db: DataBase): MastodonInstanceInfoDAO = db.mastodonInstanceInfoDao()

    @Provides
    @Singleton
    fun provideNotificationJsonCacheDao(db: DataBase) = db.notificationJsonCacheRecordDAO()

    @Provides
    @Singleton
    fun provideMastodonWordFilterDao(db: DataBase) = db.mastodonFilterDao()

    @Provides
    @Singleton
    fun provideRenoteMuteDao(db: DataBase) = db.renoteMuteDao()

    @Provides
    @Singleton
    fun provideCustomEmojiDao(db: DataBase) = db.customEmojiDao()

    @Provides
    @Singleton
    fun provideInstanceTickerDao(db: DataBase) = db.instanceTickerDAO()

    @Provides
    @Singleton
    fun provideNoteDao(db: DataBase) = db.noteDAO()

    @Provides
    @Singleton
    fun provideNoteThreadDao(db: DataBase) = db.noteThreadDAO()
}
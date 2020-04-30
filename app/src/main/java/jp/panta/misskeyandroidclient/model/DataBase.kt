package jp.panta.misskeyandroidclient.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.panta.misskeyandroidclient.model.core.*
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.reaction.*

@Database(entities = [EncryptedConnectionInformation::class, ReactionHistory::class, Account::class, ReactionUserSetting::class, Page::class], version = 1)
@TypeConverters(PageTypeConverter::class, DateConverter::class)
abstract class DataBase : RoomDatabase(){
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao
    abstract fun connectionInformationDao(): ConnectionInformationDao
    abstract fun accountDao(): AccountDao
    abstract fun reactionHistoryDao(): ReactionHistoryDao
    abstract fun reactionUserSettingDao(): ReactionUserSettingDao
    abstract fun pageDao(): PageDao
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao
}
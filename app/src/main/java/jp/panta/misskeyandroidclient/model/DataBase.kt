package jp.panta.misskeyandroidclient.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountDao
import jp.panta.misskeyandroidclient.model.core.ConnectionInformationDao
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao
import jp.panta.misskeyandroidclient.model.notes.reaction.*

@Database(entities = [EncryptedConnectionInformation::class, NoteRequest.Setting::class, ReactionHistory::class, Account::class, ReactionUserSetting::class], version = 37)
@TypeConverters(NoteRequest.NoteTypeConverter::class, DateConverter::class)
abstract class DataBase : RoomDatabase(){
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao
    abstract fun connectionInformationDao(): ConnectionInformationDao
    abstract fun accountDao(): AccountDao
    abstract fun noteSettingDao(): NoteRequestSettingDao
    abstract fun reactionHistoryDao(): ReactionHistoryDao
    abstract fun reactionUserSettingDao(): ReactionUserSettingDao
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao
}
package jp.panta.misskeyandroidclient.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryCount
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDao

@Database(entities = [ConnectionInstance::class, NoteRequest.Setting::class, ReactionHistory::class], version = 27)
@TypeConverters(NoteRequest.NoteTypeConverter::class)
abstract class DataBase : RoomDatabase(){
    abstract fun connectionInstanceDao(): ConnectionInstanceDao
    abstract fun noteSettingDao(): NoteRequestSettingDao
    abstract fun reactionHistoryDao(): ReactionHistoryDao
}
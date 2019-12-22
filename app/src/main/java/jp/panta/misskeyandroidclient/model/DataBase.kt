package jp.panta.misskeyandroidclient.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao

@Database(entities = [ConnectionInstance::class, NoteRequest.Setting::class], version = 22)
@TypeConverters(NoteRequest.NoteTypeConverter::class)
abstract class DataBase : RoomDatabase(){
    abstract fun connectionInstanceDao(): ConnectionInstanceDao
    abstract fun noteSettingDao(): NoteRequestSettingDao
}
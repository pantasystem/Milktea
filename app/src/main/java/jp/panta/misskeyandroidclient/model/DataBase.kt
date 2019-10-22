package jp.panta.misskeyandroidclient.model

import androidx.room.Database
import androidx.room.RoomDatabase
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao

@Database(entities = [ConnectionInstance::class], version = 3)
abstract class DataBase : RoomDatabase(){
    abstract fun connectionInstanceDao(): ConnectionInstanceDao
}
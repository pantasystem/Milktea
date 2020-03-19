package jp.panta.misskeyandroidclient.model

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_33_34 = object : Migration(33, 34){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table setting add antennaId TEXT")
    }
}

val MIGRATION_34_35 = object : Migration(34, 35){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table setting add listId TEXT")
    }
}
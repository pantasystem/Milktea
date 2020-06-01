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

val MIGRATION_35_36 = object : Migration(35, 36){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("create table 'reaction_user_setting'('reaction' TEXT not null, 'instance_domain' TEXT not null, 'weight' INTEGER not null, primary key('reaction', 'instance_domain'))")
    }
}

val MIGRATION_36_37 = object : Migration(36, 37){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table setting add weight INTEGER")
    }
}

val MIGRATION_1_2 = object : Migration(1, 2){
    override fun migrate(database: SupportSQLiteDatabase) {
        //TODO migrationを書く
    }
}
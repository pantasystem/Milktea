package jp.panta.misskeyandroidclient.model

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.db.AccountDAO
import jp.panta.misskeyandroidclient.model.account.newAccount
import jp.panta.misskeyandroidclient.model.account.page.db.PageDAO
import jp.panta.misskeyandroidclient.model.core.AccountDao


val MIGRATION_1_2 = object : Migration(1, 2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("create table 'draft_note'('draft_note_id' INTEGER primary key autoincrement,'accountId' TEXT not null, 'visibility' TEXT not null, 'text' TEXT, 'cw' TEXT, 'viaMobile' INTEGER, 'localOnly' INTEGER, 'noExtractMentions' INTEGER, 'noExtractHashtags' INTEGER, 'noExtractEmojis' INTEGER, 'replyId' TEXT, 'renoteId' TEXT, 'expiresAt' INTEGER, 'multiple' INTEGER, foreign key('accountId') references 'Account'('id') on update cascade on delete cascade)")
        database.execSQL("create table 'user_id'('userId' TEXT not null, 'draft_note_id' INTEGER not null, foreign key('draft_note_id') references'draft_note'('draft_note_id') on delete cascade on update cascade, primary key('userId', 'draft_note_id'))")
        database.execSQL("create table 'poll_choice'('choice' TEXT not null, 'weight' INTEGER not null, 'draft_note_id' INTEGER not null, foreign key('draft_note_id') references 'draft_note'('draft_note_id') on delete cascade on update cascade, primary key('choice', 'weight', 'draft_note_id'))")
        database.execSQL("create table 'draft_file'('remote_file_id' TEXT, 'file_path' TEXT, 'draft_note_id' INTEGER not null, 'file_id' INTEGER primary key autoincrement, foreign key('draft_note_id') references 'draft_note'('draft_note_id') on delete cascade on update cascade)")

        database.execSQL("create index 'index_draft_note_accountId_text' on 'draft_note'('accountId', 'text')")
        database.execSQL("create index 'index_user_id' on 'user_id'('draft_note_id')")
        database.execSQL("create index 'index_poll_choice_draft_note_id_choice' on 'poll_choice'('draft_note_id', 'choice')")
        database.execSQL("create index 'index_draft_file' on 'draft_file'('draft_note_id')")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3){
    override fun migrate(database: SupportSQLiteDatabase) {
        // change draftFileDTO


        database.execSQL("alter table 'draft_file' add column 'is_sensitive' INTEGER")
        database.execSQL("alter table 'draft_file' add column 'type' TEXT")
        database.execSQL("alter table 'draft_file' add column 'thumbnailUrl' TEXT")
        database.execSQL("alter table 'draft_file' add column 'name' TEXT not null default 'name none'")
        database.execSQL("alter table 'draft_file' add column 'folder_id' TEXT ")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS 'url_preview'")
        database.execSQL("CREATE TABLE IF NOT EXISTS 'url_preview'('url' TEXT NOT NULL, 'title' TEXT NOT NULL, 'icon' TEXT, 'description' TEXT, 'thumbnail' TEXT, 'siteName' TEXT, PRIMARY KEY('url'))")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS 'draft_note'")

        database.execSQL("CREATE TABLE IF NOT EXISTS 'account_table' ('remoteId' TEXT NOT NULL, 'instanceDomain' TEXT NOT NULL, 'userName' TEXT NOT NULL, 'encryptedToken' TEXT NOT NULL, 'accountId' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_account_table_remoteId' ON 'account_table'('remoteId')")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_account_table_instanceDomain' ON 'account_table' ('instanceDomain')")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_account_table_userName' ON 'account_table' ('userName')")
        
        database.execSQL("CREATE TABLE IF NOT EXISTS 'page_table' ('accountId' INTEGER NOT NULL, 'title' TEXT NOT NULL, 'weight' INTEGER NOT NULL, 'pageId' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'type' TEXT NOT NULL, 'withFiles' INTEGER, 'excludeNsfw' INTEGER, 'includeLocalRenotes' INTEGER, 'includeMyRenotes' INTEGER, 'includeRenotedMyRenotes' INTEGER, 'listId' TEXT, 'following' INTEGER, 'visibility' TEXT, 'noteId' TEXT, 'tag' TEXT, 'reply' INTEGER, 'renote' INTEGER, 'poll' INTEGER, 'offset' INTEGER, 'markAsRead' INTEGER, 'userId' TEXT, 'includeReplies' INTEGER, 'query' TEXT, 'host' TEXT, 'antennaId' TEXT)")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_page_table_weight' ON 'page_table' ('weight')")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_page_table_accountId' ON 'page_table' ('accountId')")

    }
}

class AccountMigration(private val accountDao: AccountDao, private val accountRepository: AccountRepository){

    suspend fun executeMigrate(){

        try{
            val oldAccounts = accountDao.findAllSetting()

            val generated = oldAccounts.mapNotNull{ ar ->
                ar.newAccount(null)
            }
            generated.forEach{
                accountRepository.add(it, true)
            }
            accountDao.dropPageTable()
            accountDao.dropTable()
        }catch(e: Exception){
            Log.d("AccountMigration", "エラー発生", e)
        }


    }
}
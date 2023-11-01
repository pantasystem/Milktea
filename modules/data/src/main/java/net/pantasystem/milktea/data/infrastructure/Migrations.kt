@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.data.infrastructure

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


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
        database.execSQL("DROP TABLE IF EXISTS 'draft_file_table'")
        database.execSQL("DROP TABLE IF EXISTS 'poll_choice_table'")
        database.execSQL("DROP TABLE IF EXISTS 'user_id'")
        database.execSQL("DROP TABLE IF EXISTS 'draft_note'")



        database.execSQL("CREATE TABLE IF NOT EXISTS 'account_table' ('remoteId' TEXT NOT NULL, 'instanceDomain' TEXT NOT NULL, 'userName' TEXT NOT NULL, 'encryptedToken' TEXT NOT NULL, 'accountId' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_account_table_remoteId' ON 'account_table'('remoteId')")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_account_table_instanceDomain' ON 'account_table' ('instanceDomain')")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_account_table_userName' ON 'account_table' ('userName')")
        
        database.execSQL("CREATE TABLE IF NOT EXISTS 'page_table' ('accountId' INTEGER NOT NULL, 'title' TEXT NOT NULL, 'weight' INTEGER NOT NULL, 'pageId' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'type' TEXT NOT NULL, 'withFiles' INTEGER, 'excludeNsfw' INTEGER, 'includeLocalRenotes' INTEGER, 'includeMyRenotes' INTEGER, 'includeRenotedMyRenotes' INTEGER, 'listId' TEXT, 'following' INTEGER, 'visibility' TEXT, 'noteId' TEXT, 'tag' TEXT, 'reply' INTEGER, 'renote' INTEGER, 'poll' INTEGER, 'offset' INTEGER, 'markAsRead' INTEGER, 'userId' TEXT, 'includeReplies' INTEGER, 'query' TEXT, 'host' TEXT, 'antennaId' TEXT)")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_page_table_weight' ON 'page_table' ('weight')")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_page_table_accountId' ON 'page_table' ('accountId')")
        
        database.execSQL("CREATE TABLE IF NOT EXISTS 'draft_note_table' ('draft_note_id' INTEGER PRIMARY KEY AUTOINCREMENT, 'accountId' INTEGER NOT NULL, 'visibility' TEXT NOT NULL, 'text' TEXT, 'cw' TEXT, 'viaMobile' INTEGER, 'localOnly' INTEGER, 'noExtractMentions' INTEGER, 'noExtractHashtags' INTEGER, 'noExtractEmojis' INTEGER, 'replyId' TEXT, 'renoteId' TEXT, 'multiple' INTEGER, 'expiresAt' INTEGER, FOREIGN KEY('accountId') REFERENCES 'account_table'('accountId') ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_draft_note_table_accountId_text' ON 'draft_note_table' ('accountId', 'text')")
        database.execSQL("CREATE TABLE IF NOT EXISTS 'draft_file_table' ('file_id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT NOT NULL DEFAULT 'name none', 'remote_file_id' TEXT, 'file_path' TEXT, 'is_sensitive' INTEGER, 'type' TEXT, 'thumbnailUrl' TEXT, 'draft_note_id' INTEGER NOT NULL, 'folder_id' TEXT, FOREIGN KEY('draft_note_id') REFERENCES 'draft_note_table'('draft_note_id') ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_draft_file_table_draft_note_id' ON 'draft_file_table' ('draft_note_id')")
        database.execSQL("CREATE TABLE IF NOT EXISTS 'poll_choice_table' ('choice' TEXT NOT NULL, 'draft_note_id' INTEGER NOT NULL, 'weight' INTEGER NOT NULL, PRIMARY KEY('choice', 'weight', 'draft_note_id'), FOREIGN KEY('draft_note_id') REFERENCES 'draft_note_table'('draft_note_id') ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_poll_choice_table_draft_note_id_choice' ON 'poll_choice_table'('draft_note_id', 'choice')")
        database.execSQL("CREATE TABLE IF NOT EXISTS 'user_id' ('userId' TEXT NOT NULL, 'draft_note_id' INTEGER NOT NULL, PRIMARY KEY('userId', 'draft_note_id'), FOREIGN KEY('draft_note_id') REFERENCES 'draft_note_table'('draft_note_id') ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_user_id_draft_note_id' ON 'user_id' ('draft_note_id')")
        
    }
}


val MIGRATION_5_6 = object : Migration(5, 6){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS 'meta_table' ('uri' TEXT NOT NULL, 'bannerUrl' TEXT, 'cacheRemoteFiles' INTEGER, 'description' TEXT, 'disableGlobalTimeline' INTEGER, 'disableLocalTimeline' INTEGER, 'disableRegistration' INTEGER, 'driveCapacityPerLocalUserMb' INTEGER, 'driveCapacityPerRemoteUserMb' INTEGER, 'enableDiscordIntegration' INTEGER, 'enableEmail' INTEGER, 'enableEmojiReaction' INTEGER, 'enableGithubIntegration' INTEGER, 'enableRecaptcha' INTEGER, 'enableServiceWorker' INTEGER, 'enableTwitterIntegration' INTEGER, 'errorImageUrl' TEXT, 'feedbackUrl' TEXT, 'iconUrl' TEXT, 'maintainerEmail' TEXT, 'maintainerName' TEXT, 'mascotImageUrl' TEXT, 'maxNoteTextLength' INTEGER, 'name' TEXT, 'recaptchaSiteKey' TEXT, 'secure' INTEGER, 'swPublicKey' TEXT, 'toSUrl' TEXT, 'version' TEXT NOT NULL, PRIMARY KEY('uri'))")
        database.execSQL("CREATE TABLE IF NOT EXISTS 'emoji_table' ('name' TEXT NOT NULL, 'instanceDomain' TEXT NOT NULL, 'host' TEXT, 'url' TEXT, 'uri' TEXT, 'type' TEXT, 'category' TEXT, 'id' TEXT, PRIMARY KEY('name', 'instanceDomain'), FOREIGN KEY('instanceDomain') REFERENCES 'meta_table'('uri') ON UPDATE CASCADE ON DELETE CASCADE )")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_emoji_table_instanceDomain' ON 'emoji_table' ('instanceDomain')")
        database.execSQL("CREATE INDEX IF NOT EXISTS 'index_emoji_table_name' ON 'emoji_table' ('name')")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS 'emoji_alias_table' ('alias' TEXT NOT NULL, 'name' TEXT NOT NULL, 'instanceDomain' TEXT NOT NULL, PRIMARY KEY('alias', 'name', 'instanceDomain'), FOREIGN KEY('name', 'instanceDomain') REFERENCES 'emoji_table'('name', 'instanceDomain') ON UPDATE CASCADE ON DELETE CASCADE )")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS 'unread_notifications_table' ('accountId' INTEGER NOT NULL, 'notificationId' TEXT NOT NULL, PRIMARY KEY('accountId', 'notificationId'), FOREIGN KEY('accountId') REFERENCES 'account_table'('accountId') ON UPDATE CASCADE ON DELETE CASCADE )")
    }
}

val MIGRATION_8_10 = object : Migration(8, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS 'nicknames' ('nickname' TEXT NOT NULL, 'username' TEXT NOT NULL, 'host' TEXT NOT NULL, 'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS 'index_nicknames_username_host' ON 'nicknames' ('username', 'host')")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table 'account_table' add column 'instanceType' TEXT NOT NULL default 'misskey'")
    }
}

val MIGRATION_51_52 = object : Migration(51, 52) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS  'draft_file_table'")
        database.execSQL("DROP TABLE IF EXISTS 'utf8_emojis_by_amio'")
        database.execSQL("DROP TABLE IF EXISTS 'custom_emoji_aliases'")
        database.execSQL("DROP TABLE IF EXISTS 'custom_emojis'")
        database.execSQL("DROP TABLE IF EXISTS 'emoji_alias_table'")
        database.execSQL("DROP TABLE IF EXISTS 'emoji_table'")
    }
}

package jp.panta.misskeyandroidclient.model.core

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class AccountDao{

    @Query("select * from account left join connection_information on accountId = account.id")
    abstract fun findAllSetting(): List<AccountRelation>
    
    @Insert
    abstract fun insert(account: Account): Long

    @Delete
    abstract fun delete(account: Account)

    @Query("select * from account left join connection_information on accountId = account.id and accountId = :account")
    abstract fun findSettingByAccountId(account: String): List<AccountRelation>

}
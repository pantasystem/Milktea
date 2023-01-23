package net.pantasystem.milktea.data.infrastructure.account.db

import androidx.room.*

@Dao
abstract class AccountDAO{

    @Query("select * from account_table where accountId = :accountId")
    abstract fun get(accountId: Long): AccountRecord?

    @Transaction
    @Query("select * from account_table where accountId = :accountId")
    abstract fun getAccountRelation(accountId: Long): AccountRelation?

    @Transaction
    @Query("select * from account_table where remoteId = :remoteId and instanceDomain = :instanceDomain")
    abstract fun findByRemoteIdAndInstanceDomain(remoteId: String, instanceDomain: String): AccountRelation?

    @Transaction
    @Query("select * from account_table where userName = :userName and instanceDomain = :instanceDomain")
    abstract fun findByUserNameAndInstanceDomain(userName: String, instanceDomain: String): AccountRelation?

    @Transaction
    @Query("select * from account_table where userName = :userName")
    abstract fun findAllByUserName(userName: String): List<AccountRelation>

    @Transaction
    @Query("select * from account_table")
    abstract fun findAll(): List<AccountRelation>



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(account: AccountRecord): Long

    @Delete
    abstract fun delete(account: AccountRecord)

    @Update
    abstract fun update(account: AccountRecord)




}
package jp.panta.misskeyandroidclient.model.notes

import androidx.lifecycle.LiveData
import androidx.room.*
import retrofit2.http.DELETE

@Dao
interface NoteRequestSettingDao {

    @Query("select * from setting")
    fun findAll(): LiveData<List<NoteRequest.Setting>?>

    @Query("select * from setting where id = :id limit 1")
    fun findById(id: Long): LiveData<NoteRequest.Setting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(setting: NoteRequest.Setting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(settings: List<NoteRequest.Setting>)

    @Query("delete from setting where id=:id")
    fun delete(id: Long)

    @Query("delete from setting")
    fun deleteAll()

    @Delete
    fun deleteAll(list: List<NoteRequest.Setting>)

    @Query("delete from setting where accountId = :accountId")
    fun clearByAccount(accountId: String)

}
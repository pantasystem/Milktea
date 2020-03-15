package jp.panta.misskeyandroidclient.model.notes

import androidx.room.*
import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import java.io.Serializable

data class NoteRequest(
    @SerializedName("i") val i: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("withFiles") val withFiles: Boolean? = null,
    @SerializedName("fileType") val fileType: String? = null,
    @SerializedName("excludeNsfw") val excludeNsfw: Boolean? = null,
    @SerializedName("limit") val limit: Int? = 20,
    @SerializedName("sinceId") val sinceId: String? = null,
    @SerializedName("untilId") val untilId: String? = null,
    @SerializedName("sinceDate") val sinceDate: Long? = null,
    @SerializedName("untilDate") val untilDate: Long? = null,
    @SerializedName("query") val query: String? = null,
    @SerializedName("tag") val tag: String? = null,
    @SerializedName("includeLocalRenotes") val includeLocalRenotes: Boolean? = null,
    @SerializedName("includeMyRenotes") val includeMyRenotes: Boolean? = null,
    @SerializedName("includeRenotedMyNotes") val includeRenotedMyNotes: Boolean? = null,
    @SerializedName("noteId") val noteId: String? = null

): Serializable{

    @Entity(tableName = "setting")
    data class Setting(
        @TypeConverters(NoteTypeConverter::class) val type: NoteType,
        val userId: String? = null,
        val limit: Int? = null,
        val withFiles: Boolean? = null,
        val fileType: String? = null,
        val excludeNsfw: Boolean? = null,
        val query: String? = null,
        val tag: String? = null,
        val noteId: String? = null,
        var accountId: String? = null
    ): Serializable{
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null

        //SharedPreferencesで設定し後付けする
        @Ignore
        var includeLocalRenotes: Boolean? = null

        @Ignore
        var includeMyRenotes: Boolean? = null

        @Ignore
        var includeRenotedMyNotes: Boolean? = null

        var title: String = type.defaultName

        fun buildRequest(connectionInformation: EncryptedConnectionInformation, conditions: Conditions, encryption: Encryption): NoteRequest{
            return NoteRequest(
                i = connectionInformation.getI(encryption)!!,
                userId = userId,
                withFiles = withFiles,
                fileType = fileType,
                excludeNsfw = excludeNsfw,
                limit = limit?: 20,
                sinceId = conditions.sinceId,
                untilId = conditions.untilId,
                sinceDate = conditions.sinceDate,
                untilDate = conditions.untilDate,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes,
                includeRenotedMyNotes = includeRenotedMyNotes,
                noteId = noteId,
                //tag= tag,
                query = if(type == NoteType.SEARCH){
                    query?: title
                }else{
                    null
                },
                tag = if(type == NoteType.SEARCH_HASH){
                    (tag ?: title).parseTag()
                }else{
                    null
                }


            )
        }
        private fun String.parseTag() : String{
            return if(this.startsWith("#")){
                this.substring(1, this.length)
            }else{
                this
            }
        }

        @Ignore
        fun setAccount(account: Account){
            accountId = account.id
        }
    }

    data class Conditions(
        @SerializedName("sinceId") val sinceId: String? = null,
        @SerializedName("untilId") val untilId: String? = null,
        @SerializedName("sinceDate") val sinceDate: Long? = null,
        @SerializedName("untilDate") val untilDate: Long? = null
    )

    fun makeSinceId(id: String): NoteRequest{
        return this.copy(sinceId = id, untilId = null, untilDate = null, sinceDate = null)
    }
    fun makeUntilId(id: String): NoteRequest{
        return this.copy(sinceId = null, untilId = id, untilDate = null, sinceDate = null)
    }

    class NoteTypeConverter{

        @TypeConverter
        fun fromNoteType(type: NoteType): String{
            return type.name
        }

        @TypeConverter
        fun fromString(type: String): NoteType{
            return NoteType.valueOf(type)
        }
    }

}
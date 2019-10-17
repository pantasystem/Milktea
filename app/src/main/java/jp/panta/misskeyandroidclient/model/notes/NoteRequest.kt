package jp.panta.misskeyandroidclient.model.notes

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
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
    @SerializedName("query") val query: String? = null
): Serializable{

    @Entity
    data class Setting(
        val i: String,
        val type: NoteType,
        val userId: String? = null,
        val limit: Int? = null,
        val withFiles: Boolean? = null,
        val fileType: String? = null,
        val excludeNsfw: Boolean? = null,
        val query: String? = null
    ): Serializable{
        @PrimaryKey(autoGenerate = true)
        val id: Long? = null

        fun buildRequest(conditions: Conditions): NoteRequest{
            return NoteRequest(
                i = i,
                userId = userId,
                withFiles = withFiles,
                fileType = fileType,
                excludeNsfw = excludeNsfw,
                limit = limit,
                sinceId = conditions.sinceId,
                untilId = conditions.untilId,
                sinceDate = conditions.sinceDate,
                untilDate = conditions.untilDate

            )
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

}
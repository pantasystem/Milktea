package jp.panta.misskeyandroidclient.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

object PageableRequest{


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
        @SerializedName("noteId") val noteId: String? = null,
        @SerializedName("antennaId") val antennaId: String? = null,
        @SerializedName("listId") val listId: String? = null

    ): Serializable
}
package jp.panta.misskeyandroidclient.model.notes

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TimelineRequest(
    @SerializedName("i") val i: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("withFiles") val withFiles: Boolean? = null,
    @SerializedName("fileType") val fileType: String? = null,
    @SerializedName("excludeNsfw") val excludeNsfw: Boolean? = null,
    @SerializedName("limit") val limit: Int? = 10,
    @SerializedName("sinceId") val sinceId: String? = null,
    @SerializedName("untilId") val untilId: String? = null,
    @SerializedName("sinceDate") val sinceDate: Long? = null,
    @SerializedName("untilDate") val untilDate: Long? = null,
    @SerializedName("query") val query: String? = null
): Serializable{
    fun makeSinceId(id: String): TimelineRequest{
        return this.copy(sinceId = id, untilId = null, untilDate = null, sinceDate = null)
    }
    fun makeUntilId(id: String): TimelineRequest{
        return this.copy(sinceId = null, untilId = id, untilDate = null, sinceDate = null)
    }

}
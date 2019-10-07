package jp.panta.misskeyandroidclient.model.notes

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TimelineRequest(
    @SerializedName("i") var i: String? = null,
    @SerializedName("userId") var userId: String? = null,
    @SerializedName("withFiles") var withFiles: Boolean? = null,
    @SerializedName("fileType") var fileType: String? = null,
    @SerializedName("excludeNsfw") var excludeNsfw: Boolean? = null,
    @SerializedName("limit") var limit: Int? = 10,
    @SerializedName("sinceId") var sinceId: String? = null,
    @SerializedName("untilId") var untilId: String? = null,
    @SerializedName("sinceDate") var sinceDate: Long? = null,
    @SerializedName("untilDate") var untilDate: Long? = null,
    @SerializedName("query") var query: String? = null
): Serializable
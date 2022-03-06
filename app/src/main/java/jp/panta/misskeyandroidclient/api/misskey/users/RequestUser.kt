package jp.panta.misskeyandroidclient.api.misskey.users

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestUser(
    val i: String?,
    val userId: String? = null,
    @SerialName("username") @SerializedName("username") val userName: String? = null,
    val host: String? = null,
    val sort: String? = null,
    val state: String? = null,
    val origin: String? = null,
    val userIds: List<String>? = null,
    val sinceId: String? = null,
    val untilId: String? = null,
    val limit: Int? = null,
    val query: String? = null,
    val detail: Boolean? = null
){

    class Sort{
        fun follower(): OrderBy {
            return OrderBy("follower")
        }

        fun createdAt(): OrderBy {
            return OrderBy("createdAt")
        }

        fun updatedAt(): OrderBy {
            return OrderBy("updatedAt")
        }
    }

    class OrderBy(private val sortBy: String){
        fun asc(): String{
            return "+$sortBy"
        }

        fun desc(): String{
            return "-$sortBy"
        }

    }


    enum class State(val state: String){
        ALL("all"), ADMIN("admin"), MODERATOR("moderator"), ADMIN_OR_MODERATOR("adminOrModerator"), ALIVE("alive")
    }

    enum class Origin(val origin: String){
        LOCAL("local"),
        COMBINED("combined"),
        REMOTE("remote")

    }


}
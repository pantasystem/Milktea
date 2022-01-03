package jp.panta.misskeyandroidclient.model.hashtag

import kotlinx.serialization.Serializable

@Serializable
class RequestHashTagList (
    val i: String?,
    val sort: String,
    val limit: Int = 30,
    val attachedToUserOnly: Boolean? = null,
    val attachedToLocalUserOnly: Boolean? = null,
    val attachedToRemoteUserOnly: Boolean? = null
){

    class Sort {
        fun mentionedUsers() = OrderBy("mentionedUsers")
        fun mentionedLocalUsers() = OrderBy("mentionedLocalUsers")
        fun mentionedRemoteUsers() = OrderBy("mentionedRemoteUsers")
        fun attachedUsers() = OrderBy("attachedUsers")
        fun attachedLocalUsers() = OrderBy("attachedLocalUsers")
        fun attachedRemoteUsers() = OrderBy("attachedRemoteUsers")
    }

    class OrderBy(private val sort: String){
        fun asc(): String{
            return "+$sort"
        }

        fun desc(): String{
            return "-$sort"
        }
    }
}
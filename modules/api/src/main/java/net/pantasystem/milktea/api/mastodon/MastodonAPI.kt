package net.pantasystem.milktea.api.mastodon

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.mastodon.accounts.MuteAccountRequest
import net.pantasystem.milktea.api.mastodon.apps.AccessToken
import net.pantasystem.milktea.api.mastodon.apps.App
import net.pantasystem.milktea.api.mastodon.apps.CreateApp
import net.pantasystem.milktea.api.mastodon.apps.ObtainToken
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO
import net.pantasystem.milktea.api.mastodon.instance.Instance
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO
import net.pantasystem.milktea.api.mastodon.status.CreateStatus
import net.pantasystem.milktea.api.mastodon.status.ScheduledStatus
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import retrofit2.Response
import retrofit2.http.*

interface MastodonAPI {

    @GET("api/v1/instance")
    suspend fun getInstance(): Instance

    @GET("api/v1/custom_emojis")
    suspend fun getCustomEmojis(): Response<List<TootEmojiDTO>>

    @POST("api/v1/apps")
    suspend fun createApp(@Body body: CreateApp): Response<App>

    @POST("oauth/token")
    suspend fun obtainToken(@Body body: ObtainToken): Response<AccessToken>


    @GET("api/v1/accounts/verify_credentials")
    suspend fun verifyCredentials(): Response<MastodonAccountDTO>

    /**
     * @param visibilities fedibirdの独自パラメータ
     */
    @GET("api/v1/timelines/public")
    suspend fun getPublicTimeline(
        @Query("local") local: Boolean = false,
        @Query("remote") remote: Boolean = false,
        @Query("only_media") onlyMedia: Boolean = false,
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("min_id") minId: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("visibilities[]", encoded = true) visibilities: List<String>? = null,
    ): Response<List<TootStatusDTO>>

    @GET("api/v1/timelines/tag/{tag}")
    suspend fun getHashtagTimeline(
        @Path("tag") tag: String,
        @Query("min_id") minId: String? = null,
        @Query("max_id") maxId: String? = null
    ): Response<List<TootStatusDTO>>

    /**
     * @param visibilities fedibirdの独自パラメータ
     */
    @GET("api/v1/timelines/home")
    suspend fun getHomeTimeline(
        @Query("min_id") minId: String? = null,
        @Query("max_id") maxId: String? = null,
        @Query("visibilities[]", encoded = true) visibilities: List<String>? = null
    ): Response<List<TootStatusDTO>>

    @GET("api/v1/timelines/list/{listId}")
    suspend fun getListTimeline(
        @Path("listId") listId: String,
        @Query("min_id") minId: String? = null,
        @Query("max_id") maxId: String? = null,
    ): Response<List<TootStatusDTO>>

    @POST("api/v1/statuses/{statusId}/reblog")
    suspend fun reblog(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @POST("api/v1/statuses/{statusId}/unreblog")
    suspend fun unreblog(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @GET("api/v1/accounts/{accountId}/followers")
    suspend fun getFollowers(
        @Path("accountId") accountId: String,
        @Query("min_id") minId: String? = null,
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int = 40
    ): Response<List<MastodonAccountDTO>>

    @GET("api/v1/accounts/{accountId}/following")
    suspend fun getFollowing(
        @Path("accountId") accountId: String,
        @Query("min_id") minId: String? = null,
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int = 40
    ): Response<List<MastodonAccountDTO>>

    @GET("api/v1/accounts/{accountId}")
    suspend fun getAccount(@Path("accountId") accountId: String): Response<MastodonAccountDTO>

    @GET("api/v1/accounts/relationships")
    suspend fun getAccountRelationships(
        @Query(
            "id[]",
            encoded = true
        ) ids: List<String>
    ): Response<List<MastodonAccountRelationshipDTO>>

    @POST("api/v1/accounts/{accountId}/follow")
    suspend fun follow(@Path("accountId") accountId: String): Response<MastodonAccountRelationshipDTO>

    @POST("api/v1/accounts/{accountId}/unfollow")
    suspend fun unfollow(@Path("accountId") accountId: String): Response<MastodonAccountRelationshipDTO>

    @PUT("api/v1/statuses/{statusId}/emoji_reactions/{emoji}")
    suspend fun reaction(
        @Path("statusId") statusId: String,
        @Path("emoji") emoji: String
    ): Response<TootStatusDTO>

    @POST("api/v1/statuses/{statusId}/emoji_unreaction")
    suspend fun unreaction(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @POST("api/v1/statuses/{statusId}/favourite")
    suspend fun favouriteStatus(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @POST("api/v1/statuses/{statusId}/unfavourite")
    suspend fun unfavouriteStatus(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @POST("api/v1/statuses/{statusId}/bookmark")
    suspend fun bookmarkStatus(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @POST("api/v1/statuses/{statusId}/unbookmark")
    suspend fun unbookmarkStatus(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @GET("api/v1/favourites")
    suspend fun getFavouriteStatuses(
        @Query("min_id") minId: String? = null,
        @Query("max_id") maxId: String? = null
    ): Response<List<TootStatusDTO>>

    @POST("api/v1/accounts/{accountId}/mute")
    suspend fun muteAccount(
        @Path("accountId") accountId: String,
        @Body body: MuteAccountRequest
    ): Response<MastodonAccountRelationshipDTO>

    @POST("api/v1/accounts/{accountId}/unmute")
    suspend fun unmuteAccount(@Path("accountId") accountId: String): Response<MastodonAccountRelationshipDTO>

    @POST("api/v1/accounts/{accountId}/block")
    suspend fun blockAccount(@Path("accountId") accountId: String): Response<MastodonAccountRelationshipDTO>

    @POST("api/v1/accounts/{accountId}/unblock")
    suspend fun unblockAccount(@Path("accountId") accountId: String): Response<MastodonAccountRelationshipDTO>


    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("min_id") minId: String? = null,
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("types[]", encoded = true) types: List<String>? = null,
        @Query("exclude_types[]", encoded = true) excludeTypes: List<String>? = null,
        @Query("account_id") accountId: String? = null,
    ): Response<List<MstNotificationDTO>>

    @POST("api/v1/statuses")
    suspend fun createStatus(
        @Body body: CreateStatus
    ): Response<TootStatusDTO>

    @POST("api/v1/status")
    suspend fun createScheduledStatus(
        @Body body: CreateStatus,
    ): Response<ScheduledStatus>


    @GET("api/v1/statuses/{statusId}")
    suspend fun getStatus(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @DELETE("api/v1/statuses/{statusId}")
    suspend fun deleteStatus(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @POST("api/v1/polls/{pollId}/votes")
    suspend fun voteOnPoll(
        @Path("pollId") pollId: String,
        @Field("choices[]", encoded = true) choices: List<Int>
    ): Response<TootPollDTO>

    @POST("api/v1/statuses/{statusId}/mute")
    suspend fun muteConversation(@Path("statusId") statusId: String): Response<TootStatusDTO>

    @POST("api/v1/statuses/{statusId}/unmute")
    suspend fun unmuteConversation(@Path("statusId") statusId: String): Response<TootStatusDTO>


    @GET("api/v1/accounts/{accountId}/statuses")
    suspend fun getAccountTimeline(
        @Path("accountId") accountId: String,
        @Query("only_media") onlyMedia: Boolean? = false,
        @Query("max_id") maxId: String? = null,
        @Query("min_id") minId: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("exclude_reblogs") excludeReblogs: Boolean? = null,
        @Query("exclude_replies") excludeReplies: Boolean? = null,
    ): Response<List<TootStatusDTO>>
}
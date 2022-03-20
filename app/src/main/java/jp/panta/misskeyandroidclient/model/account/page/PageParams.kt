package jp.panta.misskeyandroidclient.model.account.page


import jp.panta.misskeyandroidclient.api.misskey.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.account.page.PageType.*
import java.io.Serializable

data class PageParams (
    val type: PageType = HOME,
    val withFiles: Boolean? = null,
    var excludeNsfw: Boolean? = null,
    var includeLocalRenotes: Boolean? = null,
    var includeMyRenotes: Boolean? = null,
    var includeRenotedMyRenotes: Boolean? = null,
    val listId: String? = null,
    val following: Boolean? = null,
    val visibility: String? = null,
    val noteId: String? = null,
    val tag: String? = null,
    var reply: Boolean? = null,
    var renote: Boolean? = null,
    var poll: Boolean? = null,
    val offset: Int? = null,
    var markAsRead: Boolean? = null,
    val userId: String? = null,
    var includeReplies: Boolean? = null,
    var query: String? = null,
    var host: String? = null,
    val antennaId: String? = null,
    val channelId: String? = null,
) : Serializable{

    fun toNoteRequest(i: String?) : NoteRequest {

        return NoteRequest(
            i = i,
            withFiles = withFiles,
            excludeNsfw = excludeNsfw,
            includeMyRenotes = includeMyRenotes,
            includeLocalRenotes = includeLocalRenotes,
            includeRenotedMyNotes = includeRenotedMyRenotes,
            listId = listId,
            following = following,
            visibility = visibility,
            noteId = noteId,
            tag = tag,
            reply = reply,
            renote = renote,
            poll = poll,
            offset = offset,
            markAsRead = markAsRead,
            channelId = channelId,
        )
    }

    // * Global, Local, Hybrid, Home, UserList, Mention, Show, SearchByTag, Featured, Notification, UserTimeline, Search, Antenna
    @Throws(IllegalStateException::class)
    fun toPageable(): Pageable {
        try{
            return when(this.type){
                HOME->{
                    Pageable.HomeTimeline(
                        withFiles = withFiles,
                        includeLocalRenotes = includeLocalRenotes,
                        includeMyRenotes = includeMyRenotes,
                        includeRenotedMyRenotes = includeRenotedMyRenotes
                    )
                }
                LOCAL->{
                    Pageable.LocalTimeline(
                        withFiles = withFiles,
                        excludeNsfw = excludeNsfw
                    )
                }
                SOCIAL->{
                    Pageable.HybridTimeline(
                        withFiles = withFiles,
                        includeRenotedMyRenotes = includeRenotedMyRenotes,
                        includeMyRenotes = includeMyRenotes,
                        includeLocalRenotes = includeLocalRenotes
                    )
                }
                GLOBAL->{
                    Pageable.GlobalTimeline(
                        withFiles = withFiles
                    )
                }
                SEARCH->{
                    Pageable.Search(
                        query = query!!,
                        host = host,
                        userId = userId
                    )
                }
                SEARCH_HASH->{
                    Pageable.SearchByTag(
                        tag = tag!!,
                        reply = reply,
                        renote = renote,
                        withFiles = withFiles,
                        poll = poll
                    )
                }
                USER->{
                    Pageable.UserTimeline(
                        userId = userId!!,
                        includeMyRenotes = includeMyRenotes,
                        includeReplies = includeReplies,
                        withFiles = withFiles
                    )
                }
                FAVORITE->{
                    Pageable.Favorite
                }
                FEATURED->{
                    Pageable.Featured(offset = offset)
                }
                DETAIL->{
                    Pageable.Show(
                        noteId = noteId!!
                    )
                }
                USER_LIST->{
                    Pageable.UserListTimeline(
                        listId = listId!!,
                        withFiles = withFiles,
                        includeMyRenotes = includeMyRenotes,
                        includeLocalRenotes = includeLocalRenotes,
                        includeRenotedMyRenotes = includeRenotedMyRenotes
                    )
                }
                MENTION->{
                    Pageable.Mention(
                        following = following,
                        visibility = visibility
                    )
                }
                ANTENNA->{
                    Pageable.Antenna(
                        antennaId = antennaId!!
                    )
                }
                NOTIFICATION->{
                    Pageable.Notification(
                        following = following,
                        markAsRead = markAsRead
                    )

                }
                GALLERY_POPULAR -> {
                    Pageable.Gallery.Popular
                }
                GALLERY_FEATURED -> {
                    Pageable.Gallery.Featured
                }
                GALLERY_POSTS -> {
                    Pageable.Gallery.Posts
                }
                USERS_GALLERY_POSTS -> {
                    Pageable.Gallery.User(userId!!)
                }
                I_LIKED_GALLERY_POSTS -> {
                    Pageable.Gallery.ILikedPosts
                }
                MY_GALLERY_POSTS -> {
                    Pageable.Gallery.MyPosts
                }
                CHANNEL_TIMELINE -> {
                    Pageable.ChannelTimeline(channelId!!)
                }
            }
        }catch(e: NullPointerException){
            throw IllegalStateException("パラメーターに問題があります: $this")
        }

    }

}
package jp.panta.misskeyandroidclient.model.account.page

/**
 * Global, Local, Hybrid, Home, UserList, Mention, Show, SearchByTag, Featured, Notification, UserTimeline, Search, Antenna
 */
sealed class Pageable{

    data class GlobalTimeline(

        var withFiles: Boolean? = null

    ):  Pageable(), UntilPaginate, SincePaginate{
        override fun toParams(): PageParams {
            return PageParams(withFiles = withFiles, type = PageType.GLOBAL)
        }
    }

    data class LocalTimeline(

        var withFiles: Boolean? = null,
        var excludeNsfw: Boolean? = null

    ) : Pageable(),  UntilPaginate, SincePaginate{
        override fun toParams(): PageParams {
            return PageParams(PageType.LOCAL, withFiles = withFiles, excludeNsfw = excludeNsfw)
        }
    }

    data class HybridTimeline(

        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null

    ) : Pageable(),  UntilPaginate, SincePaginate{
        override fun toParams(): PageParams {
            return PageParams(
                type = PageType.SOCIAL,
                withFiles = withFiles,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes,
                includeRenotedMyRenotes = includeRenotedMyRenotes)
        }
    }

    data class HomeTimeline(

        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null

    ) : Pageable(),  UntilPaginate, SincePaginate{

        override fun toParams(): PageParams {
            return PageParams(
                PageType.HOME,
                withFiles = withFiles,
                includeRenotedMyRenotes = includeRenotedMyRenotes,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes
            )
        }
    }

    data class UserListTimeline(

        val listId: String,
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null

    ) : Pageable(),  UntilPaginate, SincePaginate{

        override fun toParams(): PageParams {
            return PageParams(
                PageType.USER_LIST,
                listId = listId,
                withFiles = withFiles,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes,
                includeRenotedMyRenotes = includeRenotedMyRenotes
            )
        }
    }

    data class Mention(

        val following: Boolean?,
        val visibility: String? = null

    ) : Pageable(),  UntilPaginate, SincePaginate{
        override fun toParams(): PageParams {
            return PageParams(
                PageType.MENTION,
                following = following,
                visibility = visibility
            )
        }
    }

    data class Show(

        val noteId: String

    ) :  Pageable(){
        override fun toParams(): PageParams {
            return PageParams(
                PageType.DETAIL,
                noteId = noteId
            )
        }
    }

    data class SearchByTag(

        val tag: String, var reply: Boolean? = null,
        var renote: Boolean? = null,
        var withFiles: Boolean? = null,
        var poll: Boolean? = null

    ) : Pageable(),  UntilPaginate, SincePaginate{
        override fun toParams(): PageParams {
            return PageParams(
                PageType.SEARCH_HASH,
                tag = tag,
                reply = reply,
                renote = renote,
                withFiles = withFiles,
                poll = poll
            )
        }
    }

    data class Featured(

        val offset: Int?

    ) : Pageable(){
        override fun toParams(): PageParams {
            return PageParams(
                PageType.FEATURED,
                offset = offset
            )
        }
    }

    data class Notification(

        var following: Boolean? = null,
        var markAsRead: Boolean? = null

    ) :  Pageable(), SincePaginate, UntilPaginate{
        override fun toParams(): PageParams {
            return PageParams(
                PageType.NOTIFICATION,
                following = following,
                markAsRead = markAsRead
            )
        }
    }

    data class UserTimeline(

        val userId: String,
        var includeReplies: Boolean? = true,
        var includeMyRenotes: Boolean? = true,
        var withFiles: Boolean? = null

    ) : Pageable(),  SincePaginate, UntilPaginate{
        override fun toParams(): PageParams {
            return PageParams(
                PageType.USER,
                includeReplies = includeReplies,
                includeMyRenotes = includeMyRenotes,
                withFiles = withFiles
            )
        }
    }

    data class Search(

        var query: String,
        var host: String? = null,
        var userId: String? = null

    ) : Pageable(),  SincePaginate, UntilPaginate{
        override fun toParams(): PageParams {
            return PageParams(
                PageType.SEARCH,
                query = query,
                host = host,
                userId = userId
            )
        }
    }

    data class Antenna(

        val antennaId: String

    ) : Pageable(),  UntilPaginate, SincePaginate{
        override fun toParams(): PageParams {
            return PageParams(
                PageType.ANTENNA,
                antennaId = antennaId
            )
        }
    }

    object Favorite : Pageable(),  UntilPaginate, SincePaginate{
        override fun toParams(): PageParams {
            return PageParams(PageType.FAVORITE)
        }
    }


    abstract fun toParams() : PageParams


}
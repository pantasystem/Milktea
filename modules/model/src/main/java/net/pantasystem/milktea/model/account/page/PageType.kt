package net.pantasystem.milktea.model.account.page


enum class PageType(val defaultName: String){
    HOME("Home"),
    LOCAL("Local"),
    SOCIAL("Social"),
    GLOBAL("Global"),
    SEARCH("Search"),
    SEARCH_HASH("Hash"),
    USER("User"),
    FAVORITE("Favorite"),
    FEATURED("Featured"),
    DETAIL("Detail"),
    USER_LIST("List"),
    MENTION("Mention"),
    ANTENNA("Antenna"),
    NOTIFICATION("Notification"),
    GALLERY_FEATURED("GalleryFeatured"),
    GALLERY_POPULAR("GalleryPopular"),
    GALLERY_POSTS("GalleryPosts"),
    USERS_GALLERY_POSTS("UsersGalleryPosts"),
    MY_GALLERY_POSTS("MyGalleryPosts"),
    I_LIKED_GALLERY_POSTS("ILikedGalleryPosts"),
    CHANNEL_TIMELINE("Channel"),
    MASTODON_LOCAL_TIMELINE("MastodonLocalTimeline"),
    MASTODON_PUBLIC_TIMELINE("MastodonPublicTimeline"),
    MASTODON_HOME_TIMELINE("MastodonHomeTimeline"),
    MASTODON_HASHTAG_TIMELINE("MastodonHashtagTimeline"),
    MASTODON_LIST_TIMELINE("MastodonListTimeline"),
    //USER_PINは別
}



val galleryTypes = setOf(
    PageType.GALLERY_FEATURED,
    PageType.GALLERY_POSTS,
    PageType.GALLERY_POPULAR,
    PageType.USERS_GALLERY_POSTS,
    PageType.I_LIKED_GALLERY_POSTS,
    PageType.MY_GALLERY_POSTS
)
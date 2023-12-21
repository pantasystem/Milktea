package net.pantasystem.milktea.model.account.page


enum class PageType(val defaultName: String, val label: String){
    HOME("Home", "HOME"),
    LOCAL("Local", "LOCAL"),
    SOCIAL("Social", "SOCIAL"),
    GLOBAL("Global", "GLOBAL"),
    SEARCH("Search", "SEARCH"),
    SEARCH_HASH("Hash", "SEARCH_HASH"),
    USER("User", "USER"),
    FAVORITE("Favorite", "FAVORITE"),
    FEATURED("Featured", "FEATURED"),
    DETAIL("Detail", "DETAIL"),
    USER_LIST("List", "USER_LIST"),
    MENTION("Mention", "MENTION"),
    ANTENNA("Antenna", "ANTENNA"),
    NOTIFICATION("Notification", "NOTIFICATION"),
    GALLERY_FEATURED("GalleryFeatured", "GALLERY_FEATURED"),
    GALLERY_POPULAR("GalleryPopular", "GALLERY_POPULAR"),
    GALLERY_POSTS("GalleryPosts", "GALLERY_POSTS"),
    USERS_GALLERY_POSTS("UsersGalleryPosts", "USERS_GALLERY_POSTS"),
    MY_GALLERY_POSTS("MyGalleryPosts", "MY_GALLERY_POSTS"),
    I_LIKED_GALLERY_POSTS("ILikedGalleryPosts", "I_LIKED_GALLERY_POSTS"),
    CHANNEL_TIMELINE("Channel", "CHANNEL_TIMELINE"),
    MASTODON_LOCAL_TIMELINE("MastodonLocalTimeline", "MASTODON_LOCAL_TIMELINE"),
    MASTODON_PUBLIC_TIMELINE("MastodonPublicTimeline", "MASTODON_PUBLIC_TIMELINE"),
    MASTODON_HOME_TIMELINE("MastodonHomeTimeline", "MASTODON_HOME_TIMELINE"),
    MASTODON_LIST_TIMELINE("MastodonListTimeline", "MASTODON_LIST_TIMELINE"),
    MASTODON_USER_TIMELINE("MastodonUserTimeline", "MASTODON_USER_TIMELINE"),
    MASTODON_BOOKMARK_TIMELINE("MastodonBookmarkTimeline", "MASTODON_BOOKMARK_TIMELINE"),
    MASTODON_SEARCH_TIMELINE("MastodonSearchTimeline", "MASTODON_SEARCH_TIMELINE"),
    MASTODON_TAG_TIMELINE("MastodonTagTimeline", "MASTODON_TAG_TIMELINE"),
    MASTODON_TREND_TIMELINE("MastodonTrendTimeline", "MASTODON_TREND_TIMELINE"),
    CALCKEY_RECOMMENDED_TIMELINE("CalckeyRecommendedTimeline", "CALCKEY_RECOMMENDED_TIMELINE"),

    CLIP_NOTES("ClipNotes", "CLIP_NOTES"),

    MASTODON_MENTION_TIMELINE("MastodonMentionTimeline", "MASTODON_MENTION_TIMELINE"),

    //USER_PINは別
}

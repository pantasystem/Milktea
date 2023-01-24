package net.pantasystem.milktea.common

/**
 * Mastodonの場合次のページネーションのIdがLinkヘッダーに含まれる形で帰ってくるので
 * そのヘッダーから正規表現でmax_idとmin_idを取得するようにしている
 */
class MastodonLinkHeaderDecoder(private val headerText: String?) {

    private val maxIdPattern = "max_id=(\\d+)".toRegex()
    private val minIdPattern = "min_id=(\\d+)".toRegex()

    fun getMaxId(): String? {
        headerText ?: return null
        return maxIdPattern.find(headerText)?.groupValues?.getOrNull(1)
    }

    fun getMinId(): String? {
        headerText ?: return null
        return minIdPattern.find(headerText)?.groupValues?.getOrNull(1)
    }

}
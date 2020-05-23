package jp.panta.misskeyandroidclient.model.url

import org.jsoup.Jsoup
import java.util.regex.Pattern

class JSoupUrlPreviewStore : UrlPreviewStore{
    private val ogPattern = Pattern.compile("""og:(.*)""")
    private val urlPattern = Pattern.compile("""(https)(://)([-_.!~*'()a-zA-Z0-9;?:@&=+${'$'},%#]+)(/[-_.!~*'()a-zA-Z0-9;/?:@&=+${'$'},%#]+)?""")
    //private val urlPattern = Pattern.compile("""(https?)(://)([-_.!~*'()a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")
    override fun get(url: String): UrlPreview? {
        try{
            val urlMatcher = urlPattern.matcher(url)
            if(!urlMatcher.find()){
                println("urlではないため終了:$url")
                return null
            }
            val baseUrl = urlMatcher.group(1)!! + urlMatcher.group(2) + urlMatcher.group(3)
            println("baseUrl:$baseUrl")
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .timeout(500).get()
            val head = document.head()
            val icon = head.select("link[rel~=(shortcut+?)?icon]").first()?.attr("href")
            val ogpElements = document.select("meta[property~=og:*]")


            var title: String? = null
            var description: String? = null
            var siteName: String? = null
            var thumbnailImage: String? = null


            ogpElements?.forEach{
                val property = it.attr("property")
                val content = it.attr("content")
                val ogMatcher = ogPattern.matcher(property)
                if(ogMatcher.find()){
                    when(ogMatcher.group(1)){
                        "title" -> title = content
                        "description" -> description = content
                        "site_name" -> siteName = content
                        "image" -> thumbnailImage = content
                    }
                }

            }
            val iconUrl = when {
                icon == null -> {
                    "$baseUrl/favicon.ico"
                }
                urlPattern.matcher(icon).find() -> {
                    icon
                }
                else -> {
                    baseUrl + icon
                }
            }

            title = title?: urlMatcher.group(3)

            //println("title:$title, siteName:$siteName, description:$description, \nicon:$iconUrl, \ndescription:$description")

            if(title.isNullOrEmpty() || description.isNullOrEmpty() || thumbnailImage.isNullOrEmpty() || icon.isNullOrEmpty()){
                return null
            }
            //val iconUrl =
            return UrlPreview(
                title = title!!,
                description = description!!,
                siteName = siteName?: urlMatcher.group(3)!!,
                thumbnail = thumbnailImage!!,
                icon = iconUrl
            )
        }catch(e: Exception){
            println(e)
            return null
        }

    }
}
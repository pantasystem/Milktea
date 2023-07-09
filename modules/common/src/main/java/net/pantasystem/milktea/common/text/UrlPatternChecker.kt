package net.pantasystem.milktea.common.text

object UrlPatternChecker {
    private val urlPattern = Regex("""(https?)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")
    fun isMatch(text: String): Boolean {
        return urlPattern.matches(text)
    }

}
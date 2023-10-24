package net.pantasystem.milktea.common_android.nyaize

fun nyaize(text: String): String {
    // 日本語の変換
    var result = text.replace("な", "にゃ").replace("ナ", "ニャ").replace("ﾅ", "ﾆｬ")

    // 英語の変換
    result = result.replace(Regex("(?<=n)a", RegexOption.IGNORE_CASE)) {
        if (it.value == "A") "YA" else "ya"
    }
    result = result.replace(Regex("(?<=morn)ing", RegexOption.IGNORE_CASE)) {
        if (it.value == "ING") "YAN" else "yan"
    }
    result = result.replace(Regex("(?<=every)one", RegexOption.IGNORE_CASE)) {
        if (it.value == "ONE") "NYAN" else "nyan"
    }

    // 韓国語の変換
    result = result.replace(Regex("[나-낳]")) {
        val offset = '냐'.code - '나'.code
        it.value[0].plus(offset).toChar().toString()
    }
    result = result.replace(Regex("(다$)|(다(?=[.]))|(다(?= ))|(다(?=!))|(다(?=\\?))", RegexOption.MULTILINE)) {
        "다냥"
    }
    result = result.replace(Regex("(야(?=\\?))|(야$)|(야(?= ))", RegexOption.MULTILINE)) {
        "냥"
    }

    return result
}
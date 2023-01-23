package net.pantasystem.milktea.common.text

import kotlin.math.min

object LevenshteinDistance {
    operator fun invoke(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = min(
                        dp[i - 1][j - 1] + (if (s1[i - 1] == s2[j - 1]) 0 else 1),
                        min(dp[i - 1][j], dp[i][j - 1]) + 1
                    )
                }
            }
        }
        return dp[s1.length][s2.length]
    }
}

package jp.panta.misskeyandroidclient.review

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.tasks.await
import net.pantasystem.milktea.common.Logger

class InAppReviewWrapper(
    private val inAppReviewManager: ReviewManager,
    private val loggerFactory: Logger.Factory,
) {

    private val logger by lazy {
        loggerFactory.create("InAppReviewWrapper")
    }

    suspend fun showReview(activity: Activity) {
        try {
            val request = inAppReviewManager.requestReviewFlow()
            val reviewInfo: ReviewInfo? = request.await()
            if (reviewInfo != null) {
                inAppReviewManager.launchReviewFlow(activity, reviewInfo).await()
            }
        } catch (e: Exception) {
            logger.error("Failed to show in-app review", e)
        }
    }
}
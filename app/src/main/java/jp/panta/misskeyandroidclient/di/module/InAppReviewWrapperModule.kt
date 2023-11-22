package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.BuildConfig
import jp.panta.misskeyandroidclient.review.InAppReviewWrapper
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.statistics.LastlyInAppReviewShownRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InAppReviewWrapperModule {

    @Provides
    @Singleton
    fun provideInAppReviewWrapper(
        @ApplicationContext context: Context,
        loggerFactory: Logger.Factory,
        lastlyInAppReviewShownRepository: LastlyInAppReviewShownRepository
    ): InAppReviewWrapper {
        return if (BuildConfig.DEBUG) {
            InAppReviewWrapper(
                FakeReviewManager(context),
                loggerFactory,
                lastlyInAppReviewShownRepository,
            )
        } else {
            InAppReviewWrapper(
                ReviewManagerFactory.create(context),
                loggerFactory,
                lastlyInAppReviewShownRepository,

                )
        }
    }
}
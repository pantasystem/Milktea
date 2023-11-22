package net.pantasystem.milktea.model.review

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.statistics.InAppPostCounterRepository
import net.pantasystem.milktea.model.statistics.LastlyInAppReviewShownRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class CheckNeedShowInAppReviewUseCase @Inject constructor(
    private val inAppPostCounterRepository: InAppPostCounterRepository,
    private val lastlyInAppReviewShownRepository: LastlyInAppReviewShownRepository,
) {

    operator fun invoke(): Flow<Boolean> {
        return combine(inAppPostCounterRepository.observe(), lastlyInAppReviewShownRepository.observe()) { count, dateTime ->
            count > 30 && (dateTime == null || dateTime.plus(365.days) < Clock.System.now())
        }
    }
}
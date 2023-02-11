package net.pantasystem.milktea.model.emoji

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject

class AddEmojiToUserConfigUseCase @Inject constructor(
    val userEmojiConfigRepository: UserEmojiConfigRepository,
    val accountRepository: AccountRepository,
) : UseCase {

    suspend operator fun invoke(emoji: String): Result<Unit> = runCancellableCatching{
        val account = accountRepository.getCurrentAccount().getOrThrow()
        userEmojiConfigRepository.save(
            UserEmojiConfig(
                reaction = emoji,
                instanceDomain = account.normalizedInstanceDomain,
                weight = userEmojiConfigRepository.findByInstanceDomain(account.normalizedInstanceDomain).size
            )
        ).getOrThrow()
    }
}
package net.pantasystem.milktea.model.account

import net.pantasystem.milktea.model.UseCase

interface SignOutUseCase : UseCase {
    suspend operator fun invoke(account: Account): Result<Unit>
}
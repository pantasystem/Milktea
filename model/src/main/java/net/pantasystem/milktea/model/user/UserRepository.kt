package net.pantasystem.milktea.model.user

import net.pantasystem.milktea.model.user.query.FindUsersQuery
import net.pantasystem.milktea.model.user.report.Report

interface UserRepository {

    suspend fun find(userId: User.Id, detail: Boolean = true): User

    suspend fun findByUserName(accountId: Long, userName: String, host: String?, detail: Boolean = true): User

    suspend fun searchByName(accountId: Long, name: String): List<User>

    suspend fun searchByUserName(accountId: Long, userName: String, host: String?): List<User>

    suspend fun follow(userId: User.Id): Boolean

    suspend fun unfollow(userId: User.Id): Boolean

    suspend fun mute(userId: User.Id): Boolean

    suspend fun unmute(userId: User.Id): Boolean

    suspend fun block(userId: User.Id): Boolean

    suspend fun unblock(userId: User.Id): Boolean

    suspend fun acceptFollowRequest(userId: User.Id) : Boolean

    suspend fun rejectFollowRequest(userId: User.Id) : Boolean

    suspend fun report(report: Report) : Boolean

    suspend fun findUsers(accountId: Long, query: FindUsersQuery): List<User>
}
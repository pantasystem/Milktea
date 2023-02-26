package net.pantasystem.milktea.model.user

interface FollowRequestRepository {

    suspend fun accept(userId: User.Id) : Boolean

    suspend fun reject(userId: User.Id) : Boolean
}
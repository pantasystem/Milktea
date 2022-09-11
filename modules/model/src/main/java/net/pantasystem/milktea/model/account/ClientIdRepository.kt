package net.pantasystem.milktea.model.account

interface ClientIdRepository {

    fun getOrCreate(): ClientId
}
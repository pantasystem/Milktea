package net.pantasystem.milktea.model.antenna

interface AntennaRepository {
    suspend fun findByAccountId(accountId: Long): Result<List<Antenna>>
    suspend fun delete(antennaId: Antenna.Id): Result<Unit>
    suspend fun update(antennaId: Antenna.Id, params: SaveAntennaParam): Result<Antenna>
    suspend fun create(accountId: Long, params: SaveAntennaParam): Result<Antenna>
    suspend fun find(antennaId: Antenna.Id): Result<Antenna>
}
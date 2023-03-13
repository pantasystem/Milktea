package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.*
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days

internal class SyncRenoteMuteDelegateImplTest {
    // キャッシュが存在している　
    // キャッシュは一度もPushしたことがない
    // APIはRenoteMuteに対応している
    // リモートには一つもRenoteMuteは存在しない
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun existsInCacheAndNotPushedAndSupportApiAndNotExistsInRemote() = runTest {
        val cacheCreatedAt = Clock.System.now() - 1.days
        val postedAt = Clock.System.now()

        val currentAccount = Account(
            remoteId = "",
            instanceDomain = "",
            userName = "",
            instanceType = Account.InstanceType.MISSKEY,
            token = ""
        )
        val unPushedLocalData = listOf(
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-1",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-2",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-3",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-4",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-5",
                createdAt = cacheCreatedAt,
                postedAt = null,
            )
        )

        var insertAllActualData: List<RenoteMuteRecord>? = null
        var callPushArgsActualData: List<User.Id> = emptyList()
        var isDeleteByAccountCalled = false

        val delegate = SyncRenoteMuteDelegateImpl(
            getAccount = {
                currentAccount
            },
            cache = RenoteMuteCache(),
            renoteMuteDao = object : RenoteMuteDao {
                override suspend fun insert(renoteMuteRecord: RenoteMuteRecord): Long = 1L
                override suspend fun insertAll(records: List<RenoteMuteRecord>): List<Long> {
                    insertAllActualData = records
                    return emptyList()
                }
                override suspend fun update(renoteMuteRecord: RenoteMuteRecord) = Unit
                override suspend fun findByAccount(accountId: Long): List<RenoteMuteRecord> {
                    return unPushedLocalData
                }
                override suspend fun findByUser(
                    accountId: Long,
                    userId: String,
                ): RenoteMuteRecord? = null

                override fun observeByUser(
                    accountId: Long,
                    userId: String,
                ): Flow<RenoteMuteRecord?> = emptyFlow()

                override suspend fun delete(accountId: Long, userId: String) = Unit

                override suspend fun deleteBy(accountId: Long) {
                    isDeleteByAccountCalled = true
                }

                override fun observeBy(accountId: Long): Flow<List<RenoteMuteRecord>> = emptyFlow()

                override suspend fun findByUnPushed(accountId: Long): List<RenoteMuteRecord> {
                    return unPushedLocalData
                }

            },
            createAndPushToRemote = object : CreateRenoteMuteAndPushToRemoteDelegate {
                override suspend fun invoke(userId: User.Id): Result<RenoteMute> {
                    callPushArgsActualData = callPushArgsActualData + userId
                    return Result.success(
                        unPushedLocalData.first {
                            it.userId == userId.id
                        }.toModel().copy(
                            postedAt = postedAt
                        )
                    )
                }
            },
            unPushedRenoteMutesDiffFilter = UnPushedRenoteMutesDiffFilter(),
            isSupportRenoteMuteInstance = object : IsSupportRenoteMuteInstance {
                override suspend fun invoke(accountId: Long): Boolean {
                    return true
                }
            },
            findAllRemoteRenoteMutesDelegate = object : FindAllRemoteRenoteMutesDelegate {
                override suspend fun invoke(account: Account): List<RenoteMuteDTO> {
                    return emptyList()
                }
                                                                                         },
            coroutineDispatcher = Dispatchers.Default
        )

        delegate.invoke(currentAccount.accountId).getOrThrow()

        Assertions.assertEquals(
            unPushedLocalData.map {
                it.toModel().userId
            }.toSet(),
            callPushArgsActualData.toSet()
        )

        Assertions.assertEquals(
            unPushedLocalData.map {
                it.toModel().userId
            }.toSet(),
            insertAllActualData?.map {
                it.toModel().userId
            }?.toSet()
        )

        Assertions.assertEquals(
            unPushedLocalData.map {
                it.toModel().copy(postedAt = postedAt)
            }.sortedBy {
                it.userId.id
            },
            insertAllActualData?.map {
                it.toModel()
            }?.sortedBy {
                it.userId.id
            }
        )

        Assertions.assertTrue(isDeleteByAccountCalled)

    }

    // キャッシュは存在している
    // キャッシュは一度もPushしたことがない
    // APIはRenoteMuteに対応している
    // リモートにはキャッシュに存在しないRenoteMuteが存在している
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun existsInCacheAndNotPushedAndSupportApiAndExistsInRemote() = runTest {
        val cacheCreatedAt = Clock.System.now() - 1.days
        val postedAt = Clock.System.now()
        val remoteCreatedAt = Clock.System.now()

        val currentAccount = Account(
            remoteId = "",
            instanceDomain = "",
            userName = "",
            instanceType = Account.InstanceType.MISSKEY,
            token = ""
        )
        val unPushedLocalData = listOf(
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-1",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-2",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-3",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-4",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-5",
                createdAt = cacheCreatedAt,
                postedAt = null,
            )
        )

        val alreadyExistsRemoteData = listOf(
            RenoteMuteDTO(
                "mute-1",
                muteeId = "remote-mute-user-1",
                mutee = UserDTO("remote-mute-user-1", ""),
                createdAt = remoteCreatedAt
            ),
            RenoteMuteDTO(
                "mute-2",
                muteeId = "remote-mute-user-2",
                mutee = UserDTO("remote-mute-user-2", ""),
                createdAt = remoteCreatedAt
            )
        )

        var insertAllActualData: List<RenoteMuteRecord>? = null
        var callPushArgsActualData: List<User.Id> = emptyList()
        var isDeleteByAccountCalled = false

        val delegate = SyncRenoteMuteDelegateImpl(
            getAccount = {
                currentAccount
            },
            cache = RenoteMuteCache(),
            renoteMuteDao = object : RenoteMuteDao {
                override suspend fun insert(renoteMuteRecord: RenoteMuteRecord): Long = 1L
                override suspend fun insertAll(records: List<RenoteMuteRecord>): List<Long> {
                    insertAllActualData = records
                    return emptyList()
                }
                override suspend fun update(renoteMuteRecord: RenoteMuteRecord) = Unit
                override suspend fun findByAccount(accountId: Long): List<RenoteMuteRecord> {
                    return unPushedLocalData
                }
                override suspend fun findByUser(
                    accountId: Long,
                    userId: String,
                ): RenoteMuteRecord? = null

                override fun observeByUser(
                    accountId: Long,
                    userId: String,
                ): Flow<RenoteMuteRecord?> = emptyFlow()

                override suspend fun delete(accountId: Long, userId: String) = Unit

                override suspend fun deleteBy(accountId: Long) {
                    isDeleteByAccountCalled = true
                }

                override fun observeBy(accountId: Long): Flow<List<RenoteMuteRecord>> = emptyFlow()

                override suspend fun findByUnPushed(accountId: Long): List<RenoteMuteRecord> {
                    return unPushedLocalData
                }

            },
            createAndPushToRemote = object : CreateRenoteMuteAndPushToRemoteDelegate {
                override suspend fun invoke(userId: User.Id): Result<RenoteMute> {
                    callPushArgsActualData = callPushArgsActualData + userId
                    return Result.success(
                        unPushedLocalData.first {
                            it.userId == userId.id
                        }.toModel().copy(
                            postedAt = postedAt
                        )
                    )
                }
            },
            unPushedRenoteMutesDiffFilter = UnPushedRenoteMutesDiffFilter(),
            isSupportRenoteMuteInstance = object : IsSupportRenoteMuteInstance {
                override suspend fun invoke(accountId: Long): Boolean {
                    return true
                }
            },
            findAllRemoteRenoteMutesDelegate = object : FindAllRemoteRenoteMutesDelegate {
                override suspend fun invoke(account: Account): List<RenoteMuteDTO> {
                    return alreadyExistsRemoteData
                }
            },
            coroutineDispatcher = Dispatchers.Default
        )

        delegate.invoke(currentAccount.accountId).getOrThrow()

        Assertions.assertEquals(
            unPushedLocalData.map {
                it.toModel().userId
            }.toSet(),
            callPushArgsActualData.toSet()
        )

        Assertions.assertEquals(
            (alreadyExistsRemoteData.map {
                it.toModel(currentAccount.accountId).userId
            } + unPushedLocalData.map {
                it.toModel().userId
            }).toSet(),
            insertAllActualData?.map {
                it.toModel().userId
            }?.toSet()
        )

        Assertions.assertEquals(
            alreadyExistsRemoteData.map {
                it.toModel(currentAccount.accountId)
            } + unPushedLocalData.map {
                it.toModel().copy(postedAt = postedAt)
            }.sortedBy {
                it.userId.id
            },
            insertAllActualData?.map {
                it.toModel()
            }?.sortedBy {
                it.userId.id
            }
        )

        Assertions.assertTrue(isDeleteByAccountCalled)
    }

    // キャッシュは存在している
    // キャッシュは一度もPushしたことがない
    // APIはRenoteMuteに対応していない
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun existsInCacheAndNotPushedNotSupportApi() = runTest {
        val cacheCreatedAt = Clock.System.now() - 1.days
        val postedAt = Clock.System.now()

        val currentAccount = Account(
            remoteId = "",
            instanceDomain = "",
            userName = "",
            instanceType = Account.InstanceType.MISSKEY,
            token = ""
        )
        val unPushedLocalData = listOf(
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-1",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-2",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-3",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-4",
                createdAt = cacheCreatedAt,
                postedAt = null,
            ),
            RenoteMuteRecord(
                currentAccount.accountId,
                userId = "user-5",
                createdAt = cacheCreatedAt,
                postedAt = null,
            )
        )

        var insertAllActualData: List<RenoteMuteRecord>? = null
        var callPushArgsActualData: List<User.Id> = emptyList()
        var isDeleteByAccountCalled = false

        val delegate = SyncRenoteMuteDelegateImpl(
            getAccount = {
                currentAccount
            },
            cache = RenoteMuteCache(),
            renoteMuteDao = object : RenoteMuteDao {
                override suspend fun insert(renoteMuteRecord: RenoteMuteRecord): Long = 1L
                override suspend fun insertAll(records: List<RenoteMuteRecord>): List<Long> {
                    insertAllActualData = records
                    return emptyList()
                }
                override suspend fun update(renoteMuteRecord: RenoteMuteRecord) = Unit
                override suspend fun findByAccount(accountId: Long): List<RenoteMuteRecord> {
                    return unPushedLocalData
                }
                override suspend fun findByUser(
                    accountId: Long,
                    userId: String,
                ): RenoteMuteRecord? = null

                override fun observeByUser(
                    accountId: Long,
                    userId: String,
                ): Flow<RenoteMuteRecord?> = emptyFlow()

                override suspend fun delete(accountId: Long, userId: String) = Unit

                override suspend fun deleteBy(accountId: Long) {
                    isDeleteByAccountCalled = true
                }

                override fun observeBy(accountId: Long): Flow<List<RenoteMuteRecord>> = emptyFlow()

                override suspend fun findByUnPushed(accountId: Long): List<RenoteMuteRecord> {
                    return unPushedLocalData
                }

            },
            createAndPushToRemote = object : CreateRenoteMuteAndPushToRemoteDelegate {
                override suspend fun invoke(userId: User.Id): Result<RenoteMute> {
                    callPushArgsActualData = callPushArgsActualData + userId
                    return Result.success(
                        unPushedLocalData.first {
                            it.userId == userId.id
                        }.toModel().copy(
                            postedAt = postedAt
                        )
                    )
                }
            },
            unPushedRenoteMutesDiffFilter = UnPushedRenoteMutesDiffFilter(),
            isSupportRenoteMuteInstance = object : IsSupportRenoteMuteInstance {
                override suspend fun invoke(accountId: Long): Boolean {
                    return false
                }
            },
            findAllRemoteRenoteMutesDelegate = object : FindAllRemoteRenoteMutesDelegate {
                override suspend fun invoke(account: Account): List<RenoteMuteDTO> {
                    return emptyList()
                }
            },
            coroutineDispatcher = Dispatchers.Default
        )

        delegate.invoke(currentAccount.accountId).getOrThrow()

        Assertions.assertEquals(0, callPushArgsActualData.size)

        Assertions.assertNull(insertAllActualData)

        Assertions.assertFalse(isDeleteByAccountCalled)

    }



}
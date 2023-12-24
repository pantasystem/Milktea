package net.pantasystem.milktea.model.account

import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.make
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class SyncAccountInfoUseCaseTest {

    @Test
    fun softwareTypeChanged() = runTest {
        val account = Account(
            accountId = 1,
            remoteId = "1",
            instanceDomain = "https://misskey.io",
            instanceType = Account.InstanceType.FIREFISH,
            userName = "panta",
            pages = emptyList(),
            token = "test",
        )
        val accountRepository: AccountRepository = mock() {
            onBlocking {
                add(any(), any())
            } doReturn Result.success(
                account
            )
        }
        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "misskey.io",
                    software = NodeInfo.Software(
                        name = "firefish",
                        version = "12.34.56"
                    ),
                    version = "12.34.56"
                )
            )
        }
        val useCase = SyncAccountInfoUseCase(
            accountRepository = accountRepository,
            nodeInfoRepository = nodeInfoRepository,
            userRepository = mock() {
                onBlocking {
                    find(any(), any())
                } doReturn User.Detail.make(
                    id = User.Id(1L, "1"),
                    userName = "panta",
                )
            }
        )
        useCase(
            account.copy(
                instanceType = Account.InstanceType.MISSKEY,
            )
        ).getOrThrow()

        verifyBlocking(accountRepository) {
            add(
                Account(
                    accountId = 1,
                    remoteId = "1",
                    instanceDomain = "https://misskey.io",
                    instanceType = Account.InstanceType.FIREFISH,
                    userName = "panta",
                    pages = emptyList(),
                    token = "test",
                ),
                false
            )
        }
    }

    @Test
    fun userNameChanged() = runTest {
        val account = Account(
            accountId = 1,
            remoteId = "1",
            instanceDomain = "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            userName = "panta",
            pages = emptyList(),
            token = "test",
        )
        val accountRepository: AccountRepository = mock() {
            onBlocking {
                add(any(), any())
            } doReturn Result.success(
                account,
            )
        }
        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "misskey.io",
                    software = NodeInfo.Software(
                        name = "misskey",
                        version = "12.34.56"
                    ),
                    version = "12.34.56"
                )
            )
        }
        val useCase = SyncAccountInfoUseCase(
            accountRepository = accountRepository,
            nodeInfoRepository = nodeInfoRepository,
            userRepository = mock() {
                onBlocking {
                    find(any(), any())
                } doReturn User.Detail.make(
                    id = User.Id(1L, "1"),
                    userName = "panta",
                )
            }
        )
        useCase(
            account.copy(
                userName = "panta2",
            )
        ).getOrThrow()

        verifyBlocking(accountRepository) {
            add(
                account,
                false
            )
        }
    }

    @Test
    fun userNameEmptyAndChanged() = runTest {
        val account = Account(
            accountId = 1,
            remoteId = "1",
            instanceDomain = "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            userName = "",
            pages = emptyList(),
            token = "test",
        )
        val accountRepository: AccountRepository = mock() {
            onBlocking {
                add(any(), any())
            } doReturn Result.success(
                account,
            )
        }
        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "misskey.io",
                    software = NodeInfo.Software(
                        name = "misskey",
                        version = "12.34.56"
                    ),
                    version = "12.34.56"
                )
            )
        }
        val useCase = SyncAccountInfoUseCase(
            accountRepository = accountRepository,
            nodeInfoRepository = nodeInfoRepository,
            userRepository = mock() {
                onBlocking {
                    find(any(), any())
                } doReturn User.Detail.make(
                    id = User.Id(1L, "1"),
                    userName = "panta",
                )
            }
        )
        useCase(
            account.copy(
                userName = "",
            )
        ).getOrThrow()

        verifyBlocking(accountRepository) {
            add(
                account.copy(
                    userName = "panta"
                ),
                false
            )
        }
    }

    @Test
    fun userNameAndInstanceTypeChanged() = runTest {
        val account = Account(
            accountId = 1,
            remoteId = "1",
            instanceDomain = "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            userName = "panta",
            pages = emptyList(),
            token = "test",
        )
        val accountRepository: AccountRepository = mock() {
            onBlocking {
                add(any(), any())
            } doReturn Result.success(
                account,
            )
        }
        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "misskey.io",
                    software = NodeInfo.Software(
                        name = "firefish",
                        version = "12.34.56"
                    ),
                    version = "12.34.56"
                )
            )
        }
        val useCase = SyncAccountInfoUseCase(
            accountRepository = accountRepository,
            nodeInfoRepository = nodeInfoRepository,
            userRepository = mock() {
                onBlocking {
                    find(any(), any())
                } doReturn User.Detail.make(
                    id = User.Id(1L, "1"),
                    userName = "panta",
                )
            }
        )
        useCase(
            account.copy(
                instanceType = Account.InstanceType.FIREFISH,
                userName = "panta2",
            )
        ).getOrThrow()

        verifyBlocking(accountRepository) {
            add(
                account.copy(
                    instanceType = Account.InstanceType.FIREFISH,
                    userName = "panta",
                ),
                false
            )
        }
    }

    @Test
    fun unchanged() = runTest {
        val account = Account(
            accountId = 1,
            remoteId = "1",
            instanceDomain = "https://misskey.io",
            instanceType = Account.InstanceType.MISSKEY,
            userName = "panta",
            pages = emptyList(),
            token = "test",
        )
        val accountRepository: AccountRepository = mock()
        val nodeInfoRepository: NodeInfoRepository = mock() {
            onBlocking {
                find(any())
            } doReturn Result.success(
                NodeInfo(
                    host = "misskey.io",
                    software = NodeInfo.Software(
                        name = "misskey",
                        version = "12.34.56"
                    ),
                    version = "12.34.56"
                )
            )
        }
        val useCase = SyncAccountInfoUseCase(
            accountRepository = accountRepository,
            nodeInfoRepository = nodeInfoRepository,
            userRepository = mock() {
                onBlocking {
                    find(any(), any())
                } doReturn User.Detail.make(
                    id = User.Id(1L, "1"),
                    userName = "panta",
                )
            }
        )
        useCase(
            account
        ).getOrThrow()

    }
}
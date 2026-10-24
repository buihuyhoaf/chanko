package com.hoabh.chanko.dev

import com.hoabh.chanko.data.local.dao.ItemDao
import com.hoabh.chanko.data.local.entity.CooldownItemEntity
import com.hoabh.chanko.domain.auth.AuthResult
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.presentation.screens.setup.components.SetupDraft
import com.hoabh.chanko.presentation.screens.setup.components.SetupStatus
import com.hoabh.chanko.presentation.screens.setup.components.SetupSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DevEnvironmentTest {

    @Test
    fun devFakeAuthenticationRepository_initialStateAndSignInSuccess() = runTest {
        val repo = DevFakeAuthenticationRepository()
        val initialSession = repo.session.value
        assertTrue(initialSession is SessionState.SignedOut)

        val signInResult = repo.signIn { "fake-token" }
        assertTrue(signInResult is AuthResult.Success)
        assertTrue(repo.session.value is SessionState.Available)
        assertEquals("dev-user", (repo.session.value as SessionState.Available).account.value)

        val restoreResult = repo.restore()
        assertTrue(restoreResult is AuthResult.Success)
        assertTrue(repo.session.value is SessionState.Available)

        val logoutResult = repo.logout()
        assertTrue(logoutResult is AuthResult.Success)
        assertTrue(repo.session.value is SessionState.SignedOut)
    }

    @Test
    fun devFakeGoogleAuthRepository_challengeAndSignInSuccess() = runTest {
        val repo = DevFakeGoogleAuthRepository()
        val challengeResult = repo.createChallenge()
        assertTrue(challengeResult is AuthResult.Success)

        val challenge = (challengeResult as AuthResult.Success).value
        val signInResult = repo.signIn(challenge, "fake-id-token")
        assertTrue(signInResult is AuthResult.Success)
    }

    @Test
    fun devSetupGateway_lifecycleAndReset() = runTest {
        DevSetupGateway.reset()
        assertEquals(SetupStatus.INCOMPLETE, DevSetupGateway.checkStatus())

        val submission = DevSetupGateway.submit(SetupDraft("5000000", 15, "Asia/Ho_Chi_Minh"))
        assertEquals(SetupSubmission.ACCEPTED, submission)
        assertEquals(SetupStatus.COMPLETE, DevSetupGateway.checkStatus())

        DevSetupGateway.reset()
        assertEquals(SetupStatus.INCOMPLETE, DevSetupGateway.checkStatus())
    }

    @Test
    fun devDataSeeder_seedsItemsWhenEmpty() = runTest {
        val fakeDao = FakeItemDao()
        val seeder = DevDataSeeder(fakeDao)

        seeder.seedIfEmpty()

        assertEquals(3, fakeDao.items.size)
        assertTrue(fakeDao.items.any { it.status == ItemStatus.COOLING_DOWN && it.price == 6_500_000L })
        assertTrue(fakeDao.items.any { it.status == ItemStatus.COOLING_DOWN && it.price == 2_800_000L })
        assertTrue(fakeDao.items.any { it.status == ItemStatus.PASSED && it.price == 4_500_000L })

        // Re-run should skip insertion
        seeder.seedIfEmpty()
        assertEquals(3, fakeDao.items.size)
    }

    private class FakeItemDao : ItemDao {
        val items = mutableListOf<CooldownItemEntity>()
        private val itemsFlow = MutableStateFlow<List<CooldownItemEntity>>(emptyList())

        override fun getActiveItems(): Flow<List<CooldownItemEntity>> = itemsFlow.asStateFlow()

        override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItemEntity>> =
            MutableStateFlow(items.filter { it.status == status })

        override fun getItemById(id: Int): Flow<CooldownItemEntity?> =
            MutableStateFlow(items.find { it.id == id })

        override fun getArchivedItems(): Flow<List<CooldownItemEntity>> =
            MutableStateFlow(items.filter { it.isArchived })

        override suspend fun setArchived(id: Int, isArchived: Boolean): Int {
            val idx = items.indexOfFirst { it.id == id }
            if (idx >= 0) {
                items[idx] = items[idx].copy(isArchived = isArchived)
                return 1
            }
            return 0
        }

        override suspend fun insertItem(item: CooldownItemEntity) {
            items.add(item)
            itemsFlow.value = items.filter { it.status == ItemStatus.COOLING_DOWN }
        }

        override suspend fun updateItem(item: CooldownItemEntity) {
            val idx = items.indexOfFirst { it.id == item.id }
            if (idx >= 0) items[idx] = item
            itemsFlow.value = items.filter { it.status == ItemStatus.COOLING_DOWN }
        }

        override suspend fun updateItemStatus(id: Int, status: ItemStatus, now: Long): Int = 1

        override suspend fun deleteItem(item: CooldownItemEntity) {
            items.remove(item)
            itemsFlow.value = items.filter { it.status == ItemStatus.COOLING_DOWN }
        }

        override fun getTotalSavedAmount(): Flow<Long> =
            MutableStateFlow(items.filter { it.status == ItemStatus.PASSED }.sumOf { it.price })
    }
}

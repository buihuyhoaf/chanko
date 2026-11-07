package com.hoabh.chanko.presentation.features.chat.newchat

import com.hoabh.chanko.data.chat.fake.FakeChatRepository
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeChatRepository: FakeChatRepository
    private lateinit var viewModel: NewChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeChatRepository = FakeChatRepository()
        viewModel = NewChatViewModel(fakeChatRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsEligibleProducts() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.products.isNotEmpty())
        assertEquals(4, state.products.size) // 4 products seeded
        assertFalse(state.canStartChat) // Cannot start until product selected (NC-GAP-02)
    }

    @Test
    fun selectProduct_enablesStartChat() = runTest {
        advanceUntilIdle()

        val product = viewModel.uiState.value.products.first()
        viewModel.selectProduct(product)

        val state = viewModel.uiState.value
        assertEquals(product.productId, state.selectedProduct?.productId)
        assertTrue(state.canStartChat)
    }

    @Test
    fun searchQuery_filtersProductList() = runTest {
        advanceUntilIdle()

        viewModel.updateSearchQuery("bàn phím")
        val state = viewModel.uiState.value

        assertEquals(1, state.filteredProducts.size)
        assertEquals("Bàn phím mẫu C", state.filteredProducts.first().name)
    }

    @Test
    fun startChat_whenActiveThreadExists_showsHistoryDialog() = runTest {
        advanceUntilIdle()

        // p_headphones already has thread t_01 seeded
        val headphones = viewModel.uiState.value.products.first { it.productId == "p_headphones" }
        viewModel.selectProduct(headphones)

        viewModel.startChat()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showHistoryDialog)
        assertNotNull(state.existingActiveThread)
        assertEquals("t_01", state.existingActiveThread?.id)
    }

    @Test
    fun startChat_whenNoActiveThread_emitsDraftChatEffect() = runTest {
        advanceUntilIdle()

        // p_watch has no thread seeded
        val watch = viewModel.uiState.value.products.first { it.productId == "p_watch" }
        viewModel.selectProduct(watch)

        val effects = mutableListOf<NewChatUiEffect>()
        val job = launch {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.startChat()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showHistoryDialog)
        assertTrue(effects.isNotEmpty())
        val effect = effects.first() as NewChatUiEffect.NavigateToNewDraftChat
        assertEquals("p_watch", effect.productId)

        job.cancel()
    }
}

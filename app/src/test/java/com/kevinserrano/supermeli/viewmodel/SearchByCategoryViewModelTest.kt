package com.kevinserrano.supermeli.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kevinserrano.supermeli.apiSearchListResponse
import com.kevinserrano.supermeli.categoryId
import com.kevinserrano.supermeli.lib.definitions.Either
import com.kevinserrano.supermeli.search.model.Article
import com.kevinserrano.supermeli.searchbycategory.ui.SearchByCategoryViewModel
import com.kevinserrano.supermeli.searchbycategory.usecase.GetResultsByCategory
import com.kevinserrano.supermeli.sellerLink
import com.kevinserrano.supermeli.sellerName
import com.kevinserrano.supermeli.util.MainDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import java.io.InvalidObjectException
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchByCategoryViewModelTest {

    @get:Rule
    val rule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainDispatcherRule()

    private val getResultsByCategory: GetResultsByCategory = mockk(relaxed = true)
    private val firebaseCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private lateinit var searchResultsViewModel: SearchByCategoryViewModel
    private val observerStates: Observer<SearchByCategoryViewModel.State> = mockk(relaxed = true)
    private val observerEvents: Observer<SearchByCategoryViewModel.Event> = mockk(relaxed = true)
    private val articles =
        apiSearchListResponse.results.map { it.toArticle() }

    @Before
    fun setUp() {
        searchResultsViewModel = SearchByCategoryViewModel(
            getResultsByCategory = getResultsByCategory,
            firebaseAnalytics = firebaseAnalytics,
            firebaseCrashlytics = firebaseCrashlytics,
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `when searchArticlesByCategory then should return an articles`() = runTest {

        val slots = mutableListOf<SearchByCategoryViewModel.State>()

        coEvery {
            getResultsByCategory(
                categoryId = categoryId
            )
        }.answers { Either.Right(articles) }
        searchResultsViewModel.searchArticlesByCategory(categoryId = categoryId)
        searchResultsViewModel.state.observeForever(observerStates)
        runBlocking { delay(6000) }
        coVerify { observerStates.onChanged(capture(slots)) }
        Assert.assertFalse(slots.first().error)
        Assert.assertTrue(slots.first().loading)
        Assert.assertTrue(slots.first().articles == null)
        Assert.assertTrue(!slots.last().error)
    }

    @Test
    fun `when searchArticlesByCategory then should return an error`() = runTest {

        val slots = mutableListOf<SearchByCategoryViewModel.State>()

        coEvery {
            getResultsByCategory(
                categoryId = categoryId
            )
        }.answers { Either.Left(InvalidObjectException("Error...")) }
        searchResultsViewModel.searchArticlesByCategory(categoryId = categoryId)
        searchResultsViewModel.state.observeForever(observerStates)
        runBlocking { delay(6000) }
        coVerify { observerStates.onChanged(capture(slots)) }
        Assert.assertFalse(slots.first().error)
        Assert.assertTrue(slots.first().loading)
        Assert.assertTrue(slots.first().articles == null)
        Assert.assertTrue(!slots.last().error)
    }


    @Test
    fun `when onArticleItemClicked then should return an expected emit event NavigationToArticleDetail`() =
        runTest {

            val slots = mutableListOf<SearchByCategoryViewModel.Event>()


            searchResultsViewModel.onArticleItemClicked(
                article = Article(
                    id = "MCO1228577020",
                    title = "Samsung",
                    sellerLink = sellerLink,
                    sellerName = sellerName,
                    price = "$50.000",
                    condition = "new",
                    isFreeShipping = false,
                    meliChoiceCandidate = true,
                    thumbnail = "none",
                    dueText = "12x $30.000"
                )
            )
            searchResultsViewModel.event.observeForever(observerEvents)
            verify { observerEvents.onChanged(capture(slots)) }
            Assert.assertTrue(slots.first() is SearchByCategoryViewModel.Event.NavigationToArticleDetail)
        }
}
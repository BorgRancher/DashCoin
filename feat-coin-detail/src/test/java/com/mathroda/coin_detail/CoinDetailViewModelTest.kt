package com.mathroda.coin_detail

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.mathroda.coin_detail.components.TimeRange
import com.mathroda.coin_detail.util.MainDispatcherRule
import com.mathroda.core.state.IsFavoriteState
import com.mathroda.core.state.UserState
import com.mathroda.datasource.core.DashCoinRepository
import com.mathroda.datasource.firebase.FirebaseRepository
import com.mathroda.datasource.providers.ProvidersRepository
import com.mathroda.domain.ChartTimeSpan
import com.mathroda.domain.CoinById
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CoinDetailViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dashCoinRepository: DashCoinRepository
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var providersRepository: ProvidersRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: CoinDetailViewModel

    @Before
    fun setUp() {
        clearAllMocks()

        dashCoinRepository = mockk(relaxed = true)
        firebaseRepository = mockk(relaxed = true)
        providersRepository = mockk(relaxed = true)
        savedStateHandle = mockk()

        viewModel = CoinDetailViewModel(
            dashCoinRepository,
            firebaseRepository,
            providersRepository,
            savedStateHandle,
        )
    }

    /**
     * Time Range Tests
     */


    @Test
    fun `when TimeRage is 1day return ChartTimeSpan 1day`() = runTest {
        val timeRange = TimeRange.ONE_DAY

        val actual = viewModel.getTimeSpanByTimeRange(timeRange)
        val expected = ChartTimeSpan.TIMESPAN_1DAY

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when TimeRage is 1week return ChartTimeSpan 1week`() = runTest {
        val timeRange = TimeRange.ONE_WEEK

        val actual = viewModel.getTimeSpanByTimeRange(timeRange)
        val expected = ChartTimeSpan.TIMESPAN_1WEK

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when TimeRage is 1month return ChartTimeSpan 1month`() = runTest {
        val timeRange = TimeRange.ONE_MONTH

        val actual = viewModel.getTimeSpanByTimeRange(timeRange)
        val expected = ChartTimeSpan.TIMESPAN_1MONTH

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when TimeRage is 1year return ChartTimeSpan 1year`() = runTest {
        val timeRange = TimeRange.ONE_YEAR

        val actual = viewModel.getTimeSpanByTimeRange(timeRange)
        val expected = ChartTimeSpan.TIMESPAN_1YEAR

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when TimeRage is all return ChartTimeSpan all`() = runTest {
        val timeRange = TimeRange.ALL

        val actual = viewModel.getTimeSpanByTimeRange(timeRange)
        val expected = ChartTimeSpan.TIMESPAN_ALL

        assertThat(actual).isEqualTo(expected)
    }

    /**
     * Is Favorite tests
     */

    @Test
    fun `when passed coin id equals saved coin id returns Favorite state`() = runTest {
        val coinById = CoinById(id = "bitcoin")

        every { firebaseRepository.isFavoriteState(coinById) } returns flowOf(CoinById(id = "bitcoin"))
        viewModel.updateIsFavoriteState(coinById)

        val actual = viewModel.isFavoriteState.value
        val expected = IsFavoriteState.Favorite

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when passed coin id does not equal saved coin id returns NotFavorite state`() = runTest {
        val coinById = CoinById(id = "dogecoin")

        every { firebaseRepository.isFavoriteState(coinById) } returns flowOf(CoinById(id = "bitcoin"))
        viewModel.updateIsFavoriteState(coinById)

        val actual = viewModel.isFavoriteState.value
        val expected = IsFavoriteState.NotFavorite

        assertThat(actual).isEqualTo(expected)
    }

    /**
     * Updating the user state tests
     */

    @Test
    fun `when user state is UnauthedUser authState value is UnauthedUser`() = runTest {
        every { providersRepository.userStateProvider {} } returns flowOf(UserState.UnauthedUser)
        viewModel.updateUserState()

        val actual = viewModel.authState.value
        val expected = UserState.UnauthedUser

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when user state is AuthedUser authState value is AuthedUser`() = runTest {
        backgroundScope.launch(Dispatchers.IO) {
            every { providersRepository.userStateProvider {} } returns flowOf(UserState.AuthedUser)
            viewModel.updateUserState()

            val actual = viewModel.authState.value
            val expected = UserState.AuthedUser

            assertThat(actual).isEqualTo(expected)
        }
    }

    @Test
    fun `when user state is PremiumUser authState value is PremiumUser`() = runTest {
        every { providersRepository.userStateProvider {} } returns flowOf(UserState.PremiumUser)
        viewModel.updateUserState()

        val actual = viewModel.authState.value
        val expected = UserState.PremiumUser

        assertThat(actual).isEqualTo(expected)
    }
}
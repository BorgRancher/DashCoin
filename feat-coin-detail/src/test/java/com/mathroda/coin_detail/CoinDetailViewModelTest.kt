package com.mathroda.coin_detail

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.mathroda.coin_detail.components.TimeRange
import com.mathroda.coin_detail.util.MainDispatcherRule
import com.mathroda.common.events.FavoriteCoinEvents
import com.mathroda.common.state.DialogState
import com.mathroda.core.state.IsFavoriteState
import com.mathroda.core.state.UserState
import com.mathroda.core.util.Constants
import com.mathroda.core.util.Resource
import com.mathroda.datasource.core.DashCoinRepository
import com.mathroda.datasource.firebase.FirebaseRepository
import com.mathroda.datasource.providers.ProvidersRepository
import com.mathroda.domain.ChartTimeSpan
import com.mathroda.domain.Charts
import com.mathroda.domain.CoinById
import com.mathroda.domain.DashCoinUser
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
            savedStateHandle
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
        viewModel.getIsFavoriteState(coinById)

        val actual = viewModel.isFavoriteState.value
        val expected = IsFavoriteState.Favorite

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when passed coin id does not equal saved coin id returns NotFavorite state`() = runTest {
        val coinById = CoinById(id = "dogecoin")

        every { firebaseRepository.isFavoriteState(coinById) } returns flowOf(CoinById(id = "bitcoin"))
        viewModel.getIsFavoriteState(coinById)

        val actual = viewModel.isFavoriteState.value
        val expected = IsFavoriteState.NotFavorite

        assertThat(actual).isEqualTo(expected)
    }

    /**
     * Testing premium functionality
     */

    @Test
    fun `when user reached premium limit return dialog state open`() = runTest {
        // Not premium user can only add 3 coins to their favorite

        val user = DashCoinUser(favoriteCoinsCount = 4)
        val coin = CoinById()

        every { firebaseRepository.getUserCredentials() } returns flow { emit(Resource.Success(user)) }
        viewModel.premiumLimit(coin)

        val actual = viewModel.notPremiumDialog.value
        val expected = DialogState.Open

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when user did ot reach premium limit add coin return favorite state IsFavorite`() = runTest {
        // Not premium user can only add 3 coins to their favorite

        val user = DashCoinUser(favoriteCoinsCount = 2)
        val coin = CoinById()

        every { firebaseRepository.getUserCredentials() } returns flow { emit(Resource.Success(user)) }
        viewModel.premiumLimit(coin)

        viewModel.onEvent(FavoriteCoinEvents.AddCoin(coin))

        val actual = viewModel.notPremiumDialog.value
        val expected = DialogState.Close

        assertThat(actual).isEqualTo(expected)
    }


    /**
     * Testing onFavoriteClick Functionality
     */

    @Test
    fun `when favorite state is Favorite`() = runTest {
        val coin = CoinById()

        viewModel.updateIsFavoriteState(IsFavoriteState.Favorite)
        viewModel.onFavoriteClick(coin)

        val actual = viewModel.dialogState.value
        val expected = DialogState.Open

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when favorite state is NotFavorite and user is UnauthedUser`() = runTest {
        val coin = CoinById()

        viewModel.updateIsFavoriteState(IsFavoriteState.NotFavorite)
        viewModel.updateUserState(UserState.UnauthedUser)
        viewModel.onFavoriteClick(coin)

        val dialogState = viewModel.dialogState.value
        val sideEffect = viewModel.sideEffect.value


        assertThat(dialogState).isEqualTo(DialogState.Close)
        assertThat(sideEffect).isTrue()
    }

    @Test
    fun `when favorite state is NotFavorite and user is AuthedUser`() = runTest {
        val coin = CoinById()

        viewModel.updateIsFavoriteState(IsFavoriteState.NotFavorite)
        viewModel.updateUserState(UserState.AuthedUser)
        viewModel.onFavoriteClick(coin)

        val dialogState = viewModel.dialogState.value
        val sideEffect = viewModel.sideEffect.value


        assertThat(dialogState).isEqualTo(DialogState.Close)
        assertThat(sideEffect).isFalse()
    }

    @Test
    fun `when favorite state is NotFavorite and user is PremiumUser`() = runTest {
        val coin = CoinById()

        viewModel.updateIsFavoriteState(IsFavoriteState.NotFavorite)
        viewModel.updateUserState(UserState.PremiumUser)
        viewModel.onFavoriteClick(coin)

        val dialogState = viewModel.dialogState.value
        val sideEffect = viewModel.sideEffect.value


        assertThat(dialogState).isEqualTo(DialogState.Close)
        assertThat(sideEffect).isFalse()
    }

    /**
     * Testing onEvent Functionality
     */

    @Test
    fun `when event is AddCoin and result is Success`() = runTest {
        val coin = CoinById(name = "Bitcoin")
        every { firebaseRepository.addCoinFavorite(coin) } returns flowOf(Resource.Success(true))
        viewModel.onEvent(FavoriteCoinEvents.AddCoin(coin))

        val favoriteMsgState = viewModel.favoriteMsg.value
        val favoriteState = viewModel.isFavoriteState.value

        val expectedFavoriteMsg = IsFavoriteState.Messages(
            favoriteMessage = String.format(Constants.FAVORITE_MESSAGE, coin.name)
        )
        val expectedFavoriteState = IsFavoriteState.Favorite

        assertThat(favoriteMsgState).isEqualTo(expectedFavoriteMsg)
        assertThat(favoriteState).isEqualTo(expectedFavoriteState)
    }

    @Test
    fun `when event is DeleteCoin and result is Success`() = runTest {
        val coin = CoinById(name = "Bitcoin")
        every { firebaseRepository.deleteCoinFavorite(coin) } returns flowOf(Resource.Success(true))
        viewModel.onEvent(FavoriteCoinEvents.DeleteCoin(coin))

        val favoriteMsgState = viewModel.favoriteMsg.value
        val favoriteState = viewModel.isFavoriteState.value

        val expectedFavoriteMsg = IsFavoriteState.Messages(
            notFavoriteMessage = String.format(Constants.NOT_FAVORITE_MESSAGE, coin.name)
        )
        val expectedFavoriteState = IsFavoriteState.NotFavorite

        assertThat(favoriteMsgState).isEqualTo(expectedFavoriteMsg)
        assertThat(favoriteState).isEqualTo(expectedFavoriteState)
    }

    /**
     * testing getCoin Functionality
     */

    @Test
    fun `when getCoin Called and result is Success`() = runTest {
        val id = "bitcoin"
        val coin = CoinById(id = id)

        every { dashCoinRepository.getCoinById(id) } returns flowOf(Resource.Success(coin))
        viewModel.getCoin(id)


        val state = viewModel.coinState.value

        assertThat(state.coin).isNotNull()
        assertThat(state.error).isEmpty()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `when getCoin Called and result is Error`() = runTest {
        val id = "bitcoin"

        every { dashCoinRepository.getCoinById(id) } returns flowOf(Resource.Error("Error"))
        viewModel.getCoin(id)


        val state = viewModel.coinState.value

        assertThat(state.coin).isNull()
        assertThat(state.error).isNotEmpty()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `when getCoin Called and result is Loading`() = runTest {
        val id = "bitcoin"

        every { dashCoinRepository.getCoinById(id) } returns flowOf(Resource.Loading())
        viewModel.getCoin(id)


        val state = viewModel.coinState.value

        assertThat(state.coin).isNull()
        assertThat(state.error).isEmpty()
        assertThat(state.isLoading).isTrue()
    }

    /**
     * testing getChart Functionality
     */

    @Test
    fun `when getChart Called and result is Success`() = runTest {
        val id = "bitcoin"
        val period = ChartTimeSpan.TIMESPAN_1DAY
        val timeRange = TimeRange.ONE_DAY
        val data = listOf(
            listOf(0.1f, 0.2f, 0.3f, 0.4f),
            listOf(0.1f, 0.2f, 0.3f, 0.4f),
            listOf(0.1f, 0.2f, 0.3f, 0.4f),
            listOf(0.1f, 0.2f, 0.3f, 0.4f)
        )
        val charts = Charts(chart = data  )

        every { dashCoinRepository.getChartsData(id, period) } returns flowOf(Resource.Success(charts))
        viewModel.getChart(id, timeRange)


        val state = viewModel.chartState.value

        assertThat(state.chart).isNotEmpty()
        assertThat(state.error).isEmpty()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `when getChart Called and result is Error`() = runTest {
        val id = "bitcoin"
        val period = ChartTimeSpan.TIMESPAN_1DAY
        val timeRange = TimeRange.ONE_DAY

        every { dashCoinRepository.getChartsData(id, period) } returns flowOf(Resource.Error("Error"))
        viewModel.getChart(id, timeRange)


        val state = viewModel.chartState.value

        assertThat(state.chart).isEmpty()
        assertThat(state.error).isNotEmpty()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `when getChart Called and result is Loading`() = runTest {
        val id = "bitcoin"
        val period = ChartTimeSpan.TIMESPAN_1DAY
        val timeRange = TimeRange.ONE_DAY

        every { dashCoinRepository.getChartsData(id, period) } returns flowOf(Resource.Loading())
        viewModel.getChart(id, timeRange)


        val state = viewModel.chartState.value

        assertThat(state.chart).isEmpty()
        assertThat(state.error).isEmpty()
        assertThat(state.isLoading).isTrue()
    }
}
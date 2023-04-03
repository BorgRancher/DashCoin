package com.mathroda.datasource.providers

import com.google.common.truth.Truth.assertThat
import com.mathroda.core.state.UserState
import com.mathroda.core.util.Resource
import com.mathroda.datasource.firebase.FirebaseRepository
import com.mathroda.domain.DashCoinUser
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ProvidersRepositoryImplTest {

    private lateinit var firebase: FirebaseRepository
    private lateinit var repo: ProvidersRepository

    @Before
    fun setUp() {
        clearAllMocks()
        firebase = mockk(relaxed = true)
        repo = ProvidersRepositoryImpl(firebase)
    }

    @Test
    fun `when user does not exist return UnauthedUser state`() = runTest {
        every { firebase.isCurrentUserExist() } returns flowOf(false)

        val actual = repo.userStateProvider {  }.first()
        val expected = UserState.UnauthedUser

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when user does exist but not premium return AuthedUser state`() = runTest {
        val user = DashCoinUser(premium = false)
        every { firebase.getUserCredentials() } returns flowOf(Resource.Success(user))
        every { firebase.isCurrentUserExist() } returns flowOf(true)

        val actual = repo.userStateProvider {  }.first()
        val expected = UserState.AuthedUser

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when user does exist but is premium return PremiumUser state`() = runTest {
        val user = DashCoinUser(premium = true)
        every { firebase.getUserCredentials() } returns flowOf(Resource.Success(user))
        every { firebase.isCurrentUserExist() } returns flowOf(true)

        val actual = repo.userStateProvider {  }.first()
        val expected = UserState.PremiumUser

        assertThat(actual).isEqualTo(expected)
    }

}
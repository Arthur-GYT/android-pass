/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.securitycenter.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.fakes.TestAppDispatchers
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.securitycenter.api.SecurityAnalysis
import proton.android.pass.securitycenter.fakes.mother.BreachDataResultMother
import proton.android.pass.securitycenter.fakes.mother.InsecurePasswordsResultMother
import proton.android.pass.securitycenter.fakes.mother.Missing2faResultMother
import proton.android.pass.securitycenter.impl.fakes.TestBreachDataChecker
import proton.android.pass.securitycenter.impl.fakes.TestInsecurePasswordChecker
import proton.android.pass.securitycenter.impl.fakes.TestMissing2faChecker
import proton.android.pass.securitycenter.impl.fakes.TestRepeatedPasswordChecker

class ObserveSecurityAnalysisImplTest {

    private lateinit var instance: ObserveSecurityAnalysisImpl

    private lateinit var observeItems: TestObserveItems
    private lateinit var repeatedPasswordChecker: TestRepeatedPasswordChecker
    private lateinit var missing2faChecker: TestMissing2faChecker
    private lateinit var insecurePasswordChecker: TestInsecurePasswordChecker
    private lateinit var breachedDataChecker: TestBreachDataChecker

    @Before
    fun setup() {
        observeItems = TestObserveItems()
        repeatedPasswordChecker = TestRepeatedPasswordChecker()
        missing2faChecker = TestMissing2faChecker()
        insecurePasswordChecker = TestInsecurePasswordChecker()
        breachedDataChecker = TestBreachDataChecker()

        instance = ObserveSecurityAnalysisImpl(
            repeatedPasswordChecker = repeatedPasswordChecker,
            missing2faChecker = missing2faChecker,
            insecurePasswordChecker = insecurePasswordChecker,
            breachedDataChecker = breachedDataChecker,
            observeItems = observeItems,
            dispatchers = TestAppDispatchers()
        )
    }

    @Test
    fun `emits all Loading on start`() = runTest {
        instance().test {
            val item = awaitItem()
            assertThat(item).isEqualTo(
                SecurityAnalysis(
                    breachedData = LoadingResult.Loading,
                    reusedPasswords = LoadingResult.Loading,
                    insecurePasswords = LoadingResult.Loading,
                    missing2fa = LoadingResult.Loading
                )
            )
        }
    }

    @Test
    fun `can emit partial errors`() = runTest {
        val missing2fa = Missing2faResultMother.random()
        val breachData = BreachDataResultMother.random()
        val insecure = InsecurePasswordsResultMother.random()
        val error = IllegalStateException("test")
        repeatedPasswordChecker.setResult(Result.failure(error))
        missing2faChecker.setResult(missing2fa)
        breachedDataChecker.setResult(breachData)
        insecurePasswordChecker.setResult(insecure)

        observeItems.emitDefault()

        instance().test {
            skipItems(1) // Initial loading
            val item = awaitItem()
            assertThat(item).isEqualTo(
                SecurityAnalysis(
                    breachedData = LoadingResult.Success(breachData),
                    reusedPasswords = LoadingResult.Error(error),
                    insecurePasswords = LoadingResult.Success(insecure),
                    missing2fa = LoadingResult.Success(missing2fa)
                )
            )
        }
    }

    @Test
    fun `checkers are properly called`() = runTest {
        observeItems.emitDefault()
        instance().test {
            skipItems(1) // Initial loading
            awaitItem()
        }

        val expectedItems = listOf(TestObserveItems.defaultValues.asList())

        assertThat(repeatedPasswordChecker.memory()).isEqualTo(expectedItems)
        assertThat(missing2faChecker.memory()).isEqualTo(expectedItems)
        assertThat(insecurePasswordChecker.memory()).isEqualTo(expectedItems)
        assertThat(breachedDataChecker.memory()).isEqualTo(expectedItems)
    }

}
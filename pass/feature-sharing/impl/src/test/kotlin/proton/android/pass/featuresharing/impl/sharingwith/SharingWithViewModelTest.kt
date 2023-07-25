/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featuresharing.impl.sharingwith

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class SharingWithViewModelTest {

    private lateinit var viewModel: SharingWithViewModel
    private lateinit var getVaultById: TestGetVaultById
    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider

    @get:Rule
    val dispatcherRule = MainDispatcherRule()


    @Before
    fun setUp() {
        getVaultById = TestGetVaultById()
        savedStateHandleProvider = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = "my share id"
        }
        viewModel = SharingWithViewModel(
            getVaultById = getVaultById,
            savedStateHandleProvider = savedStateHandleProvider
        )
    }

    @Test
    fun `onEmailChange should update emailState correctly`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.state.test {
            assertThat(awaitItem().email).isEqualTo("test@example.com")
        }
    }

    @Test
    fun `onEmailSubmit with valid email should update isEmailNotValidState to false`() = runTest {
        viewModel.onEmailChange("test@example.com")
        viewModel.onEmailSubmit()
        viewModel.state.test {
            assertThat(awaitItem().isEmailNotValid).isFalse()
        }
    }

    @Test
    fun `onEmailSubmit with invalid email should update isEmailNotValidState to true`() = runTest {
        viewModel.onEmailChange("invalid-email")
        viewModel.onEmailSubmit()
        viewModel.state.test {
            assertThat(awaitItem().isEmailNotValid).isTrue()
        }
    }

    @Test
    fun `state should be updated correctly after combining flows`() = runTest {
        val testVault = Vault(
            shareId = ShareId(id = ""),
            name = "vault name",
            isPrimary = false
        )
        getVaultById.emitValue(testVault)
        viewModel.onEmailChange("test@example.com")
        viewModel.onEmailSubmit()

        viewModel.state.test {
            val currentState = awaitItem()
            assertThat(currentState.email).isEqualTo("test@example.com")
            assertThat(currentState.vaultName).isEqualTo(testVault.name)
            assertThat(currentState.isEmailNotValid).isFalse()
            assertThat(currentState.isVaultNotFound).isFalse()
        }
    }
}
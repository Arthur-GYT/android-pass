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

package proton.android.pass.autofill

import org.junit.Test
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.crypto.fakes.context.TestEncryptionContext

class CreditCardAutofillTest : BaseAutofillTest() {

    @Test
    fun `can autofill aliexpress com chrome`() {
        runCCAutofillTest("creditcard/chrome_aliexpress_credit_card.json")
    }

    private fun runCCAutofillTest(file: String) {
        runAutofillTest(
            file = file,
            item = AutofillItem.CreditCard(
                number = ExpectedAutofill.CC_NUMBER.value,
                cardHolder = ExpectedAutofill.CC_CARDHOLDER_NAME.value,
                expiration = CC_EXPIRATION,
                cvv = TestEncryptionContext.encrypt(ExpectedAutofill.CC_CVV.value),
                itemId = "itemID",
                shareId = "shareID"
            )
        )
    }

}
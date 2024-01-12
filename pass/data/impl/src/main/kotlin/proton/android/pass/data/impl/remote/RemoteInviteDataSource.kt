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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.requests.AcceptInviteRequest
import proton.android.pass.data.impl.responses.InviteRecommendationResponse
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.data.impl.responses.PendingInviteResponse
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ShareId

interface RemoteInviteDataSource {
    suspend fun sendInvites(
        userId: UserId,
        shareId: ShareId,
        existingUserRequests: CreateInvitesRequest,
        newUserRequests: CreateNewUserInvitesRequest
    )

    suspend fun fetchInvites(userId: UserId): List<PendingInviteResponse>
    suspend fun acceptInvite(
        userId: UserId,
        inviteToken: InviteToken,
        body: AcceptInviteRequest
    ): ShareResponse

    suspend fun rejectInvite(userId: UserId, token: InviteToken)

    suspend fun fetchInviteRecommendations(
        userId: UserId,
        shareId: ShareId,
        lastToken: String? = null,
        startsWith: String? = null
    ): InviteRecommendationResponse
}

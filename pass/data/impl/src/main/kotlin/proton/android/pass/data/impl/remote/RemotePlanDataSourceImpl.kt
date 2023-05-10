package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.responses.UserAccessResponse
import javax.inject.Inject

class RemotePlanDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemotePlanDataSource {
    override suspend fun sendUserAccessAndGetPlan(userId: UserId): UserAccessResponse =
        api.get<PasswordManagerApi>(userId)
            .invoke { userAccess() }
            .valueOrThrow
}
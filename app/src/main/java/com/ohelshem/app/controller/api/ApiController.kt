package com.ohelshem.app.controller.api

import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.controller.storage.Storage

interface ApiController {
    fun setAuthData(id: String, password: String)

    val storage: Storage

    val isBusy: Boolean

    operator fun set(id: Int, callback: Callback)
    operator fun minusAssign(id: Int)

    fun update(): Boolean

    fun login(): Boolean {
        storage.serverUpdateDate = 0
        return update()
    }

    fun setNetworkAvailabilityProvider(provider: () -> Boolean)

    enum class UpdatedApi {
        Changes, Tests, Timetable, UserData
    }

    interface Callback {
        fun onSuccess(apis: Set<UpdatedApi> = emptySet())
        fun onFail(error: UpdateError)
    }

}
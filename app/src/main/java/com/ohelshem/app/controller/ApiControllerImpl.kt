/*
 * Copyright 2016 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.controller

import com.ohelshem.api.controller.declaration.ApiParser
import com.ohelshem.api.controller.declaration.ApiProvider
import com.ohelshem.api.logger
import com.ohelshem.api.model.AuthData
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.controller.ApiController.Api
import java.util.*

class ApiControllerImpl(override val database: ApiDatabase, private val apiProvider: ApiProvider) : ApiController {
    private var networkProvider: () -> Boolean = { true }
    override lateinit var authData: AuthData
    override var isBusy: Boolean = false
        private set

    private val callbacks: MutableMap<Int, ApiController.Callback> = HashMap(2)

    override fun set(id: Int, callback: ApiController.Callback) {
        callbacks += id to callback
    }

    override fun minusAssign(id: Int) {
        callbacks.remove(id)
    }

    override fun setNetworkAvailabilityProvider(provider: () -> Boolean) {
        networkProvider = provider
    }

    override fun update(): Boolean {
        if (!isBusy) {
            if (networkProvider()) {
                isBusy = true
                apiProvider.update(authData, database.serverUpdateDate) {
                    it.fold(OnUpdateSuccess, OnUpdateFail)
                    isBusy = false
                }
                return true
            } else forEach { it.onFail(UpdateError.Connection) }
        }
        return false
    }

    private val OnUpdateSuccess = { data: ApiParser.ParsedData ->
        try {
            val apis: MutableList<Api> = LinkedList()
            data.apply {
                database.changesDate = changesDate
                database.serverUpdateDate = serverUpdateDate
                database.updateDate = System.currentTimeMillis()
                userData?.let { database.userData = it; apis += Api.UserData }
                changes?.let { database.changes = it; apis += Api.Changes }
                tests?.let { database.tests = it; apis += Api.Tests }
                messages?.let { database.messages = it; apis += Api.Messages }
                timetable?.let { database.timetable = it; apis += Api.Timetable }
            }
            if (apis.isEmpty()) forEach { it.onFail(UpdateError.NoData) }
            else forEach { it.onSuccess(apis) }
        } catch (e: Exception) {
            logger.log(e)
            forEach { it.onFail(UpdateError.Exception) }
        }
    }

    private val OnUpdateFail = { exception: Exception ->
        if (exception !is ApiParser.ApiException) {
            logger.log(exception)
            forEach { it.onFail(UpdateError.Exception) }
        } else {
            val code = exception.error
            logger.log("Failed to update. Code: $code")
            when (code) {
                1, 2 -> {
                    logger.log(IllegalStateException("Should not return those error codes"))
                    forEach { it.onFail(UpdateError.Exception) }
                }
                3 -> forEach { it.onFail(UpdateError.Login) }
                else -> forEach { it.onFail(UpdateError.Exception) }
            }
        }
    }

    private inline fun forEach(callback: (ApiController.Callback) -> Unit) {
        callbacks.values.forEach(callback)
    }
}
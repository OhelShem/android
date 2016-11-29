package com.ohelshem.app.controller.api

import com.ohelshem.api.Api
import com.ohelshem.api.controller.declaration.ApiEngine
import com.ohelshem.api.controller.declaration.ApiParser
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.controller.api.ApiController.UpdatedApi
import com.ohelshem.app.controller.storage.Storage
import mu.KLogging
import java.util.*

class ApiControllerImpl(override val storage: Storage, private val apiEngine: ApiEngine) : ApiController, Api.Callback {
    private var networkProvider: () -> Boolean = { true }

    private lateinit var id: String
    private lateinit var password: String

    override fun setAuthData(id: String, password: String) {
        this.id = id
        this.password = password
    }

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
                apiEngine.call(id, password, storage.serverUpdateDate, this)
                return true
            } else forEach { it.onFail(UpdateError.Connection) }
        }
        return false
    }

    override fun onFailure(exception: Exception) {
        isBusy = false
        if (exception !is ApiParser.ApiException) {
            logger.error(exception) { "Api Exception reached " }
            forEach { it.onFail(UpdateError.Exception) }
        } else {
            val code = exception.error
            logger.error { "Failed to update. Code: $code" }
            when (code) {
                1, 2 -> {
                    logger.error(IllegalStateException("Should not return those error codes")) { "Should not return those error codes" }
                    forEach { it.onFail(UpdateError.Exception) }
                }
                3 -> forEach { it.onFail(UpdateError.Login) }
                else -> forEach { it.onFail(UpdateError.Exception) }
            }
        }
    }

    override fun onSuccess(response: Api.Response) {
        isBusy = false
        try {
            val apis = mutableSetOf<UpdatedApi>()
            with(response) {
                storage.changesDate = changesDate
                storage.serverUpdateDate = serverUpdateDate
                storage.updateDate = System.currentTimeMillis()

                storage.userData = userData
                apis += UpdatedApi.UserData

                val timetable = timetable
                if (timetable != null) {
                    if (timetable.size == 6 && timetable[5].all(Hour::isEmpty)) {
                        storage.timetable = timetable.sliceArray(0..4)
                    } else {
                        storage.timetable = timetable
                    }
                    apis += UpdatedApi.Timetable
                }
                val data = data
                when (data) {
                    is Api.ExtraData.Student -> {
                        val changes = data.changes
                        if (changes != null) {
                            storage.changes = changes.filter { it.content.replace("\r\n", "").isNotBlank() }
                            apis += UpdatedApi.Changes
                        }
                        if (data.tests != null) {
                            storage.tests = data.tests
                            apis += UpdatedApi.Tests
                        }
                    }
                    is Api.ExtraData.Teacher -> {
                        if (data.schoolChanges != null) {
                            storage.setSchoolChanges(data.schoolChanges)
                            apis += UpdatedApi.Changes
                        }
                        if (data.schoolTests != null) {
                            storage.setSchoolTests(data.schoolTests)
                            apis += UpdatedApi.Tests
                        }
                        if (data.schoolTimetable != null) {
                            storage.setSchoolTimetable(data.schoolTimetable)
                            apis += UpdatedApi.Timetable
                        }

                        if (data.primaryClass != storage.primaryClass) {
                            storage.primaryClass = data.primaryClass
                            apis += UpdatedApi.UserData
                        }

                        if (data.classes != storage.classes) {
                            storage.classes = data.classes ?: emptyList()
                            apis += UpdatedApi.UserData
                        }
                    }
                }

            }
            if (apis.isEmpty()) forEach { it.onFail(UpdateError.NoData) }
            else forEach { it.onSuccess(apis) }
        } catch (e: Exception) {
            logger.error(e) { "Failed to update" }
            forEach { it.onFail(UpdateError.Exception) }
        }
    }

    private inline fun forEach(callback: (ApiController.Callback) -> Unit) {
        callbacks.values.forEach(callback)
    }

    companion object : KLogging()
}
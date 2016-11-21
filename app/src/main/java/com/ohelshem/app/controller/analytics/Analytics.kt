package com.ohelshem.app.controller.analytics

interface Analytics {
    fun onLogin()

    fun onLogout()

    fun subscribe()

    fun unsubscribe()

    fun unsubscribeMessages()

    fun logEvent(type: String, info: Map<String, Any>)
}
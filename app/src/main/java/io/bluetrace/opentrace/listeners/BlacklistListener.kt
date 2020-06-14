package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.streetpass.BlacklistEntry

interface BlacklistListener {
    fun addEntry(blackEntry: BlacklistEntry)
    fun handlerBlackList()
}
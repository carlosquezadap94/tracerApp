package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.streetpass.Work

interface StreetPassListener {
    fun addWork(work: Work):Boolean
    fun doWork()
    fun isCurrentlyWorkedOn(address:String):Boolean
}
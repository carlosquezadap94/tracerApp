package io.bluetrace.opentrace.listeners

import io.bluetrace.opentrace.streetpass.Work

interface WorkListener {

    fun finishWork(work: Work)
    fun getCurrrentWork(): Work
    fun currentWorkToNull()
    fun timeoutHandler(runnable: Runnable)
}
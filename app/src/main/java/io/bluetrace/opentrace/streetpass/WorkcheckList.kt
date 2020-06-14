package io.bluetrace.opentrace.streetpass

import com.google.gson.Gson

class WorkCheckList {
    var started = Check()
    var connected = Check()
    var mtuChanged = Check()
    var readCharacteristic = Check()
    var writeCharacteristic = Check()
    var disconnected = Check()
    var skipped = Check()

    override fun toString(): String {
        return Gson().toJson(this)
    }
}
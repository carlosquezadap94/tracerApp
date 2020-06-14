package io.bluetrace.opentrace.services.enums

enum class Command(val index: Int, val string: String) {
    INVALID(-1, "INVALID"),
    ACTION_START(0, "START"),
    ACTION_SCAN(1, "SCAN"),
    ACTION_STOP(2, "STOP"),
    ACTION_ADVERTISE(3, "ADVERTISE"),
    ACTION_SELF_CHECK(4, "SELF_CHECK"),
    ACTION_UPDATE_BM(5, "UPDATE_BM"),
    ACTION_PURGE(6, "PURGE");

    companion object {
        private val types = values().associate { it.index to it }
        fun findByValue(value: Int) = types[value]
    }
}
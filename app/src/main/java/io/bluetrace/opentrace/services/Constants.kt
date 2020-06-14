package io.bluetrace.opentrace.services

import io.bluetrace.opentrace.BuildConfig

class Constants {

    companion object {

        val NOTIFICATION_ID = BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID
        val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
        val CHANNEL_SERVICE = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME

        val PUSH_NOTIFICATION_ID = BuildConfig.PUSH_NOTIFICATION_ID

        val COMMAND_KEY = "${BuildConfig.APPLICATION_ID}_CMD"

        val PENDING_ACTIVITY = 5
        val PENDING_START = 6
        val PENDING_SCAN_REQ_CODE = 7
        val PENDING_ADVERTISE_REQ_CODE = 8
        val PENDING_HEALTH_CHECK_CODE = 9
        val PENDING_WIZARD_REQ_CODE = 10
        val PENDING_BM_UPDATE = 11
        val PENDING_PURGE_CODE = 12

        //should be more than advertising gap?
        val scanDuration: Long = BuildConfig.SCAN_DURATION
        val minScanInterval: Long = BuildConfig.MIN_SCAN_INTERVAL
        val maxScanInterval: Long = BuildConfig.MAX_SCAN_INTERVAL

        val advertisingDuration: Long = BuildConfig.ADVERTISING_DURATION
        val advertisingGap: Long = BuildConfig.ADVERTISING_INTERVAL
        val maxQueueTime: Long = BuildConfig.MAX_QUEUE_TIME
        val bmCheckInterval: Long = BuildConfig.BM_CHECK_INTERVAL
        val healthCheckInterval: Long = BuildConfig.HEALTH_CHECK_INTERVAL
        val purgeInterval: Long = BuildConfig.PURGE_INTERVAL
        val purgeTTL: Long = BuildConfig.PURGE_TTL
        val connectionTimeout: Long = BuildConfig.CONNECTION_TIMEOUT
        val blacklistDuration: Long = BuildConfig.BLACKLIST_DURATION
        val infiniteScanning = false
        val infiniteAdvertising = false
        val useBlacklist = true

    }
}
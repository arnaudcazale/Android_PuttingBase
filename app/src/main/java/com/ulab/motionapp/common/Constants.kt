package com.ulab.motionapp.common

class Constants {
    companion object {
        // guillaume : naming does not make any sense
        const val STATE_1: Int = 1
        const val FOUND_ALL_DEVICES: Int = 4 //2
        // guillaume : naming does not make any sense
        const val STATE_3: Int = 3
        // guillaume : naming does not make any sense
        const val STATE_4: Int = 4

        const val IS_DEBUG = false

        var MAX_DB_MODELS_SIZE = 500
        var MAX_DEVICE_VALUE_GATHERED = 10
        var MAX_DEVICE_VALUE_TO_DISPLAY = 50

        var UI_DISPLAY_DURATION = 1000L

        // guillaume :  this it not a constants
        var MAX_DEVICES_FOR_CONNECTION = 1
        var TITLE_ANIMATION = 400L
        var BLE_DELAY = 200L

        var SCAN_DURATION = 30 * 1000
        var CALIBRATION_DURATION = 1 * 1000L //30 * 1000L
    }
}
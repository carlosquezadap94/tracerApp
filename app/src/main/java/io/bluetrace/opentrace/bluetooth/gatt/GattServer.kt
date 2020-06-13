package io.bluetrace.opentrace.bluetooth.gatt

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_FAILURE
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.content.Context
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.protocol.BlueTrace
import java.util.*
import kotlin.properties.Delegates

class GattServer constructor(val context: Context, serviceUUIDString: String) {

    private val TAG = "GattServer"
    private var bluetoothManager: BluetoothManager by Delegates.notNull()

    private var serviceUUID: UUID by Delegates.notNull()
    var bluetoothGattServer: BluetoothGattServer? = null

    init {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.serviceUUID = UUID.fromString(serviceUUIDString)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        //this should be a table
        //in order to handle many connections from different mac addresses
        val writeDataPayload: MutableMap<String, ByteArray> = HashMap()
        val readPayloadMap: MutableMap<String, ByteArray> = HashMap()
        val deviceCharacteristicMap: MutableMap<String, UUID> = HashMap()

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                }

                else -> {
                }
            }
        }

        //acting as peripheral
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {

            if (device == null) {
            }

            device?.let {


                if (BlueTrace.supportsCharUUID(characteristic?.uuid)) {

                    characteristic?.uuid?.let { charUUID ->
                        val bluetraceImplementation = BlueTrace.getImplementation(charUUID)

                        val base = readPayloadMap.getOrPut(device.address, {
                            bluetraceImplementation.peripheral.prepareReadRequestData(
                                bluetraceImplementation.versionInt
                            )
                        })
                        val value = base.copyOfRange(offset, base.size)

                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            GATT_SUCCESS,
                            0,
                            value
                        )
                    }

                } else {
                    bluetoothGattServer?.sendResponse(device, requestId, GATT_FAILURE, 0, null)
                }
            }

        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {

            if (device == null) {
            }

            device?.let {


                if (BlueTrace.supportsCharUUID(characteristic.uuid)) {
                    deviceCharacteristicMap[device.address] = characteristic.uuid
                    var valuePassed = ""
                    value?.let {
                        valuePassed = String(value, Charsets.UTF_8)
                    }

                    if (value != null) {
                        var dataBuffer = writeDataPayload[device.address]

                        if (dataBuffer == null) {
                            dataBuffer = ByteArray(0)
                        }

                        dataBuffer = dataBuffer.plus(value)
                        writeDataPayload[device.address] = dataBuffer



                        if (preparedWrite && responseNeeded) {
                            bluetoothGattServer?.sendResponse(
                                device,
                                requestId,
                                GATT_SUCCESS,
                                dataBuffer.size,
                                value
                            )
                        }

                        //ios has this flag to false
                        if (!preparedWrite) {


                            saveDataReceived(device)

                            if (responseNeeded) {
                                bluetoothGattServer?.sendResponse(
                                    device,
                                    requestId,
                                    GATT_SUCCESS,
                                    0,
                                    null
                                )
                            }
                        }
                    }
                } else {


                    if (responseNeeded) {
                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            GATT_FAILURE,
                            0,
                            null
                        )
                    }
                }
            }
        }

        override fun onExecuteWrite(
            device: BluetoothDevice,
            requestId: Int,
            execute: Boolean
        ) {
            super.onExecuteWrite(device, requestId, execute)
            var data = writeDataPayload[device.address]

            data.let { dataBuffer ->

                if (dataBuffer != null) {

                    saveDataReceived(device)
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        GATT_SUCCESS,
                        0,
                        null
                    )

                } else {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        GATT_FAILURE,
                        0,
                        null
                    )
                }
            }
        }

        fun saveDataReceived(device: BluetoothDevice) {
            var data = writeDataPayload[device.address]
            var charUUID = deviceCharacteristicMap[device.address]

            charUUID?.let {
                data?.let {
                    try {
                        device.let {
                            val bluetraceImplementation = BlueTrace.getImplementation(charUUID)

                            val connectionRecord =
                                bluetraceImplementation.peripheral.processWriteRequestDataReceived(
                                    data,
                                    device.address
                                )
                            //connectionRecord will not be null if the deserializing was a success, save it
                            connectionRecord?.let {
                                Utils.broadcastStreetPassReceived(
                                    context,
                                    connectionRecord
                                )
                            }
                        }
                    } catch (e: Throwable) {

                    }

                    Utils.broadcastDeviceProcessed(context, device.address)
                    writeDataPayload.remove(device.address)
                    readPayloadMap.remove(device.address)
                    deviceCharacteristicMap.remove(device.address)
                }
            }
        }
    }

    fun startServer(): Boolean {

        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        bluetoothGattServer?.let {
            it.clearServices()
            return true
        }
        return false
    }

    fun addService(service: GattService) {
        bluetoothGattServer?.addService(service.gattService)
    }

    fun stop() {
        try {
            bluetoothGattServer?.clearServices()
            bluetoothGattServer?.close()
        } catch (e: Throwable) {

        }
    }

}

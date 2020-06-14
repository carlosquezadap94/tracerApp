package io.bluetrace.opentrace.streetpass.callback

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import io.bluetrace.opentrace.BuildConfig
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.bluetooth.protocol.BlueTrace
import io.bluetrace.opentrace.listeners.ContextListener
import io.bluetrace.opentrace.listeners.WorkListener
import io.bluetrace.opentrace.streetpass.Work
import java.util.*


class CentralGattCallback(
    val work: Work,
    val workListener: WorkListener,
    val contextListener: ContextListener
) : BluetoothGattCallback() {


    private val serviceUUID: UUID = UUID.fromString(BuildConfig.BLE_SSID)
    private val characteristicV2: UUID = UUID.fromString(BuildConfig.V2_CHARACTERISTIC_ID)

    fun endWorkConnection(gatt: BluetoothGatt) {
        gatt.disconnect()
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

        gatt?.let {

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {


                    //get a fast connection?
//                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED)
                    gatt.requestMtu(512)

                    work.checklist.connected.status = true
                    work.checklist.connected.timePerformed = System.currentTimeMillis()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    work.checklist.disconnected.status = true
                    work.checklist.disconnected.timePerformed = System.currentTimeMillis()

                    //remove timeout runnable if its still there
                    workListener.timeoutHandler(work.timeoutRunnable)

                    //remove job from list of current work - if it is the current work
                    if (work.device.address == workListener.getCurrrentWork().device.address) {
                        workListener.currentWorkToNull()
                    }
                    gatt.close()
                    workListener.finishWork(work)
                }

                else -> {

                    endWorkConnection(gatt)
                }
            }
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {

        if (!work.checklist.mtuChanged.status) {

            work.checklist.mtuChanged.status = true
            work.checklist.mtuChanged.timePerformed = System.currentTimeMillis()



            gatt?.let {
                val discoveryOn = gatt.discoverServices()

            }
        }
    }

    // New services discovered
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {


                var service = gatt.getService(serviceUUID)

                service?.let {

                    //select characteristicUUID to read from
                    val characteristic = service.getCharacteristic(characteristicV2)

                    if (characteristic != null) {
                        val readSuccess = gatt.readCharacteristic(characteristic)

                    } else {

                        endWorkConnection(gatt)
                    }
                }

                if (service == null) {

                    endWorkConnection(gatt)
                }
            }
            else -> {

                endWorkConnection(gatt)
            }
        }
    }

    // data read from a perhipheral
    //I am a central
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {

        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {


                if (BlueTrace.supportsCharUUID(characteristic.uuid)) {

                    try {
                        val bluetraceImplementation =
                            BlueTrace.getImplementation(characteristic.uuid)
                        val dataBytes = characteristic.value

                        val connectionRecord =
                            bluetraceImplementation
                                .central
                                .processReadRequestDataReceived(
                                    dataRead = dataBytes,
                                    peripheralAddress = work.device.address,
                                    rssi = work.connectable.rssi,
                                    txPower = work.connectable.transmissionPower
                                )

                        //if the deserializing was a success, connectionRecord will not be null, save it
                        connectionRecord?.let {
                            Utils.broadcastStreetPassReceived(
                                contextListener.getContext_(),
                                connectionRecord
                            )
                        }
                    } catch (e: Throwable) {
                    }

                }
                work.checklist.readCharacteristic.status = true
                work.checklist.readCharacteristic.timePerformed = System.currentTimeMillis()
            }

            else -> {

            }
        }

        //attempt to do a write
        if (BlueTrace.supportsCharUUID(characteristic.uuid)) {


            val bluetraceImplementation = BlueTrace.getImplementation(characteristic.uuid)

            var writedata = bluetraceImplementation.central.prepareWriteRequestData(
                bluetraceImplementation.versionInt,
                work.connectable.rssi,
                work.connectable.transmissionPower
            )
            characteristic.value = writedata
            val writeSuccess = gatt.writeCharacteristic(characteristic)


            endWorkConnection(gatt)


        } else {

            endWorkConnection(gatt)
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {

        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                work.checklist.writeCharacteristic.status = true
                work.checklist.writeCharacteristic.timePerformed =
                    System.currentTimeMillis()
            }
            else -> {
            }
        }

        endWorkConnection(gatt)
    }

}
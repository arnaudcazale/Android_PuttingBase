package com.ulab.motionapp.fragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ulab.motionapp.MotionApp
import com.ulab.motionapp.R
import com.ulab.motionapp.common.Constants
import com.ulab.motionapp.common.Constants.Companion.MAX_DEVICES_FOR_CONNECTION
import com.ulab.motionapp.common.DateUtils
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.db.DevicesModel
import com.ulab.motionapp.db.ThingyDevice
import com.ulab.motionapp.db.ThingyDeviceFSR
import kotlinx.android.synthetic.main.fragment_quaternions.*
import kotlinx.android.synthetic.main.header_back_with_setting.*
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager
import java.nio.ByteBuffer
import java.nio.ByteOrder




/**
 * Created by R.S. on 04/10/18
 */

// guillaume : There are no separation between the controller (the logic) and the view.
// The view should only be responsible to render the view, that's all.
class QuaternionsFragment : BaseBLEFragment() {
    @SuppressLint("SetTextI18n")
    override fun setValueToUI() {

//        if(chkGravity.isChecked) {
//            //Log.d("InitialConfiguration", "onUIGravityChanged = ")
//            /** UI Gravity datas **/
//            if (lastUlabModel1 != null) {
//                tvULab1!!.text = "${lastUlabModel1!!.X_gravity},${lastUlabModel1!!.Y_gravity},${lastUlabModel1!!.Z_gravity}"
//            }
//            if (lastUlabModel2 != null) {
//                tvULab2!!.text = "${lastUlabModel2!!.X_gravity},${lastUlabModel2!!.Y_gravity},${lastUlabModel2!!.Z_gravity}"
//            }
//            if (lastUlabModel3 != null) {
//                tvULab3!!.text = "${lastUlabModel3!!.X_gravity},${lastUlabModel3!!.Y_gravity},${lastUlabModel3!!.Z_gravity}"
//            }
//            if (lastUlabModel4 != null) {
//                tvULab4!!.text = "${lastUlabModel4!!.X_gravity},${lastUlabModel4!!.Y_gravity},${lastUlabModel4!!.Z_gravity}"
//            }
//        } else if (chkEuler.isChecked) {
//            /** UI Euler datas **/
//           /* if (lastUlabModel1 != null) {
//                tvULab1!!.text = "${lastUlabModel1!!.X_acc},${lastUlabModel1!!.Y_acc},${lastUlabModel1!!.Z_acc}"
//            }
//            if (lastUlabModel2 != null) {
//                tvULab2!!.text = "${lastUlabModel2!!.X_acc},${lastUlabModel2!!.Y_acc},${lastUlabModel2!!.Z_acc}"
//            }
//            if (lastUlabModel3 != null) {
//                tvULab3!!.text = "${lastUlabModel3!!.X_acc},${lastUlabModel3!!.Y_acc},${lastUlabModel3!!.Z_acc}"
//            }*/
//        }
//
//        /** UI FSR datas **/
//        if (m_string_argument != null) {
//            val string_argument = m_string_argument!!.substring(0, m_string_argument!!.indexOf(' '))
//            if (string_argument.compareTo("V") == 0) {
//                if (lastUlabModel1FSR != null) {
//                    tvULab1FSR!!.text = "${lastUlabModel1FSR!!.ValueFSR1_voltage},${lastUlabModel1FSR!!.ValueFSR2_voltage},${lastUlabModel1FSR!!.ValueFSR3_voltage},${lastUlabModel1FSR!!.ValueFSR4_voltage}"
//                }
//                if (lastUlabModel2FSR != null) {
//                    tvULab2FSR!!.text = "${lastUlabModel2FSR!!.ValueFSR1_voltage},${lastUlabModel2FSR!!.ValueFSR2_voltage},${lastUlabModel2FSR!!.ValueFSR3_voltage},${lastUlabModel2FSR!!.ValueFSR4_voltage}"
//                }
//                if (lastUlabModel3FSR != null) {
//                    tvULab3FSR!!.text = "${lastUlabModel3FSR!!.ValueFSR1_voltage},${lastUlabModel3FSR!!.ValueFSR2_voltage},${lastUlabModel3FSR!!.ValueFSR3_voltage},${lastUlabModel3FSR!!.ValueFSR4_voltage}"
//                }
//                if (lastUlabModel4FSR != null) {
//                    tvULab4FSR!!.text = "${lastUlabModel4FSR!!.ValueFSR1_voltage},${lastUlabModel4FSR!!.ValueFSR2_voltage},${lastUlabModel4FSR!!.ValueFSR3_voltage},${lastUlabModel4FSR!!.ValueFSR4_voltage}"
//                }
//            } else if (string_argument.compareTo("F") == 0) {
//                if (lastUlabModel1FSR != null) {
//                    tvULab1FSR!!.text = "${lastUlabModel1FSR!!.ValueFSR1_force},${lastUlabModel1FSR!!.ValueFSR2_force},${lastUlabModel1FSR!!.ValueFSR3_force},${lastUlabModel1FSR!!.ValueFSR4_force}"
//                }
//                if (lastUlabModel2FSR != null) {
//                    tvULab2FSR!!.text = "${lastUlabModel2FSR!!.ValueFSR1_force},${lastUlabModel2FSR!!.ValueFSR2_force},${lastUlabModel2FSR!!.ValueFSR3_force},${lastUlabModel2FSR!!.ValueFSR4_force}"
//                }
//                if (lastUlabModel3FSR != null) {
//                    tvULab3FSR!!.text = "${lastUlabModel3FSR!!.ValueFSR1_force},${lastUlabModel3FSR!!.ValueFSR2_force},${lastUlabModel3FSR!!.ValueFSR3_force},${lastUlabModel3FSR!!.ValueFSR4_force}"
//                }
//            } else if (string_argument.compareTo("FC") == 0) {
//                if (lastUlabModel1FSR != null) {
//                    tvULab1FSR!!.text = "${lastUlabModel1FSR!!.ValueFSR1_force_calculated},${lastUlabModel1FSR!!.ValueFSR2_force_calculated},${lastUlabModel1FSR!!.ValueFSR3_force_calculated},${lastUlabModel1FSR!!.ValueFSR4_force_calculated}"
//                }
//                if (lastUlabModel2FSR != null) {
//                    tvULab2FSR!!.text = "${lastUlabModel2FSR!!.ValueFSR1_force_calculated},${lastUlabModel2FSR!!.ValueFSR2_force_calculated},${lastUlabModel2FSR!!.ValueFSR3_force_calculated},${lastUlabModel2FSR!!.ValueFSR4_force_calculated}"
//                }
//                if (lastUlabModel3FSR != null) {
//                    tvULab3FSR!!.text = "${lastUlabModel3FSR!!.ValueFSR1_force_calculated},${lastUlabModel3FSR!!.ValueFSR2_force_calculated},${lastUlabModel3FSR!!.ValueFSR3_force_calculated},${lastUlabModel3FSR!!.ValueFSR4_force_calculated}"
//                }
//            }
//        } else {
//
//        }

    }

    // guillaume : should be an list
    private var address1 = ""
    private var address2 = ""
    private var address3 = ""
    private var address4 = ""

    // guillaume : should be an list
    private var lastUlabModel1: ThingyDevice? = null
    private var lastUlabModel2: ThingyDevice? = null
    private var lastUlabModel3: ThingyDevice? = null
    private var lastUlabModel4: ThingyDevice? = null

    private var lastUlabModel1FSR: ThingyDevice? = null
    private var lastUlabModel2FSR: ThingyDevice? = null
    private var lastUlabModel3FSR: ThingyDevice? = null
    private var lastUlabModel4FSR: ThingyDevice? = null

    private var quaternionModels: ArrayList<ThingyDevice> = ArrayList()
    private var FsrModels: ArrayList<ThingyDeviceFSR> = ArrayList()
    private var index = 0




    //var outputStream = FileOutputStream("c:/temp/samplefile.txt", true)

    override fun onDevicesDisconnected() {
        MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabDataAll(quaternionModels)
        quaternionModels = ArrayList()
//        MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabFsrDataAll(FsrModels)
//        FsrModels = ArrayList()
    }

    override fun onDevicesConnected() {

     // guillaume :    MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao() should be a deviceRepository

        // gullaume : can be a list
        if (!mThingySdkManager!!.connectedDevices.isEmpty()) {
            var indice = 0
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                if (mThingySdkManager!!.connectedDevices[i].name == "back"){
                    indice = i
                }
            }
            var deviceModel1 = MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().getDeviceModel(mThingySdkManager!!.connectedDevices[indice].address)
            if (deviceModel1 == null)
            {
                deviceModel1 = DevicesModel()
                deviceModel1.deviceAddress = mThingySdkManager!!.connectedDevices[indice].address
                deviceModel1.deviceName = mThingySdkManager!!.connectedDevices[indice].name
                address1 = mThingySdkManager!!.connectedDevices[indice].address
                MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().insertUlabData(deviceModel1)
            }
        }
        if (mThingySdkManager!!.connectedDevices.size >= 2) {
            var indice = 0
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                if (mThingySdkManager!!.connectedDevices[i].name == "stuprigh"){
                    indice = i
                }
            }
            var deviceModel2 = MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().getDeviceModel(mThingySdkManager!!.connectedDevices[indice].address)
            if (deviceModel2 == null) {
                deviceModel2 = DevicesModel()
                deviceModel2.deviceAddress = mThingySdkManager!!.connectedDevices[indice].address
                deviceModel2.deviceName = mThingySdkManager!!.connectedDevices[indice].name
                address2 = mThingySdkManager!!.connectedDevices[indice].address
                MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().insertUlabData(deviceModel2)
            }
        }

        if (mThingySdkManager!!.connectedDevices.size >= 3) {
            var indice = 0
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                if (mThingySdkManager!!.connectedDevices[i].name == "stupleft"){
                    indice = i
                }
            }
            var deviceModel3 = MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().getDeviceModel(mThingySdkManager!!.connectedDevices[indice].address)
            if (deviceModel3 == null) {
                deviceModel3 = DevicesModel()
                deviceModel3.deviceAddress = mThingySdkManager!!.connectedDevices[indice].address
                deviceModel3.deviceName = mThingySdkManager!!.connectedDevices[indice].name
                address3 = mThingySdkManager!!.connectedDevices[indice].address
                MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().insertUlabData(deviceModel3)
            }
        }

        if (mThingySdkManager!!.connectedDevices.size >= 4) {
            var indice = 0
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                if (mThingySdkManager!!.connectedDevices[i].name == "head"){
                    indice = i
                }
            }
            var deviceModel4 = MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().getDeviceModel(mThingySdkManager!!.connectedDevices[indice].address)
            if (deviceModel4 == null) {
                deviceModel4 = DevicesModel()
                deviceModel4.deviceAddress = mThingySdkManager!!.connectedDevices[indice].address
                deviceModel4.deviceName = mThingySdkManager!!.connectedDevices[indice].name
                address4 = mThingySdkManager!!.connectedDevices[indice].address
                MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().insertUlabData(deviceModel4)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        //MAX_DEVICES_FOR_CONNECTION = 4
    }

    private fun checkModelAndStoreInDB() {
        //Log.e("checkModelAndStoreInDB", "QUATERNIONS SIZE " + quaternionModels.size)
        if (quaternionModels.size == Constants.MAX_DB_MODELS_SIZE) {
            MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabDataAll(quaternionModels)
            quaternionModels = ArrayList()
        }
    }

    private fun manageFsrDB_voltage(bluetoothDevice: BluetoothDevice, FSR1: Short, FSR2: Short, FSR3: Short, FSR4: Short){
        //Log.d("InitialConfiguration", "onFSRValueChanged = " + bluetoothDevice.address + " FSR1 =" + FSR1 + " FSR2 = " + FSR2 + " FSR3 = " + FSR3 + " FSR4 = " + FSR4)
        val motion = ThingyDevice()
        motion.ValueFSR1_voltage = FSR1
        motion.ValueFSR2_voltage = FSR2
        motion.ValueFSR3_voltage = FSR3
        motion.ValueFSR4_voltage = FSR4
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp

        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Voltage"

        //MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabData(motion)

            activity!!.runOnUiThread {
                quaternionModels.add(motion)

//                if ("back" == bluetoothDevice.name) {
//                    lastUlabModel1FSR = motion
//                } else if ("stuprigh" == bluetoothDevice.name) {
//                    lastUlabModel2FSR = motion
//                } else if ("stupleft" == bluetoothDevice.name) {
//                    lastUlabModel3FSR = motion
//                } else if ("head" == bluetoothDevice.name) {
//                    lastUlabModel4FSR = motion
//                }

            }
            checkModelAndStoreInDB()
    }

    private fun manageFsrDB_force(bluetoothDevice: BluetoothDevice, FSR1: Float, FSR2: Float, FSR3: Float, FSR4: Float){
        val motion = ThingyDevice()
        motion.ValueFSR1_force = FSR1
        motion.ValueFSR2_force = FSR2
        motion.ValueFSR3_force = FSR3
        motion.ValueFSR4_force = FSR4
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp

        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Force"

        activity!!.runOnUiThread {
            quaternionModels.add(motion)

//            if ("back" == bluetoothDevice.name) {
//                lastUlabModel1FSR = motion
//            } else if ("stuprigh" == bluetoothDevice.name) {
//                lastUlabModel2FSR = motion
//            } else if ("stupleft" == bluetoothDevice.name) {
//                lastUlabModel3FSR = motion
//            } else if ("head" == bluetoothDevice.name) {
//                lastUlabModel4FSR = motion
//            }

        }
        checkModelAndStoreInDB()
    }

    private fun manageFsrDB_force_calculated(bluetoothDevice: BluetoothDevice, FSR1: Float, FSR2: Float, FSR3: Float, FSR4: Float){
        val motion = ThingyDevice()
        motion.ValueFSR1_force_calculated = FSR1
        motion.ValueFSR2_force_calculated = FSR2
        motion.ValueFSR3_force_calculated = FSR3
        motion.ValueFSR4_force_calculated = FSR4
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp

        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Force_calculated"

        activity!!.runOnUiThread {
            quaternionModels.add(motion)

//            if ("back" == bluetoothDevice.name) {
//                lastUlabModel1FSR = motion
//            } else if ("stuprigh" == bluetoothDevice.name) {
//                lastUlabModel2FSR = motion
//            } else if ("stupleft" == bluetoothDevice.name) {
//                lastUlabModel3FSR = motion
//            } else if ("head" == bluetoothDevice.name) {
//                lastUlabModel4FSR = motion
//            }

        }
        checkModelAndStoreInDB()
    }

    @SuppressLint("SetTextI18n")
    override fun onFsrDataValueChanged(bluetoothDevice: BluetoothDevice, data: ByteArray) {
//        index++
//        Log.e("onFsrDataValueChanged", "index number = " + index)
        //Log.d("InitialConfiguration", "onFSRDataValueChanged = " + bluetoothDevice.address)
        if (m_string_argument != null) {
            val string_argument = m_string_argument!!.substring(0, m_string_argument!!.indexOf(' '))
            //Log.e("APP", "STRING ARGUMENT START " + string_argument);
            if (string_argument.compareTo("V") == 0) {
                val mByteBuffer = ByteBuffer.wrap(data)
                mByteBuffer.order(ByteOrder.LITTLE_ENDIAN) // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                val mFSR1 = mByteBuffer.getShort(0)
                val mFSR2 = mByteBuffer.getShort(2)
                val mFSR3 = mByteBuffer.getShort(4)
                val mFSR4 = mByteBuffer.getShort(6)
                //Log.d("InitialConfiguration", "Data Voltage (mV) = " + " mFSR1 =" + mFSR1 + " mFSR2 = " + mFSR2 + " mFSR3 = " + mFSR3 + " mFSR4 = " + mFSR4)
                manageFsrDB_voltage(bluetoothDevice, mFSR1, mFSR2, mFSR3, mFSR4)
            } else if (string_argument.compareTo("F") == 0) {
                val mByteBuffer = ByteBuffer.wrap(data)
                mByteBuffer.order(ByteOrder.LITTLE_ENDIAN) // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                val mFSR1 = mByteBuffer.getFloat(0)
                val mFSR2 = mByteBuffer.getFloat(4)
                val mFSR3 = mByteBuffer.getFloat(8)
                val mFSR4 = mByteBuffer.getFloat(12)
                manageFsrDB_force(bluetoothDevice, mFSR1, mFSR2, mFSR3, mFSR4)
            } else if (string_argument.compareTo("FC") == 0) {
                val mByteBuffer = ByteBuffer.wrap(data)
                mByteBuffer.order(ByteOrder.LITTLE_ENDIAN) // setting to little endian as 32bit float from the nRF 52 is IEEE 754 floating
                val mFSR1 = mByteBuffer.getFloat(0)
                val mFSR2 = mByteBuffer.getFloat(4)
                val mFSR3 = mByteBuffer.getFloat(8)
                val mFSR4 = mByteBuffer.getFloat(12)
                manageFsrDB_force_calculated(bluetoothDevice, mFSR1, mFSR2, mFSR3, mFSR4)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onQuaternionValueChanged(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float) {
        //Log.d("InitialConfiguration", "onQuaternionValueChanged = " + bluetoothDevice.address + " w =" + w + " x = " + x + " y = " + y + " z = " + z)
        val motion = ThingyDevice()
        motion.valueX_quat = x
        motion.valueY_quat= y
        motion.valueZ_quat = z
        motion.valueW_quat = w
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Quaternions"

        //MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabData(motion)

            activity!!.runOnUiThread {
                quaternionModels.add(motion)
//                if (address1 == bluetoothDevice.address) {
//                        //tvULab1!!.text = "$x,$y,$z,$w"
//                    lastUlabModel1 = motion
//                } else if (address2 == bluetoothDevice.address) {
//                        //tvULab2!!.text = "$x,$y,$z,$w"
//                    lastUlabModel2 = motion
//                } else if (address3 == bluetoothDevice.address) {
//                        //tvULab2!!.text = "$x,$y,$z,$w"
//                    lastUlabModel3 = motion
//                } else if (address4 == bluetoothDevice.address) {
//                    //tvULab2!!.text = "$x,$y,$z,$w"
//                    lastUlabModel4 = motion
//                }
            }
            checkModelAndStoreInDB()
    }

    @SuppressLint("SetTextI18n")
    override fun onAccelerometerValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
        //Log.d("InitialConfiguration", "onAccelerometerValueChanged = " + bluetoothDevice.address + " x = " + x + " y = " + y + " z = " + z)

        val motion = ThingyDevice()
        motion.X_acc = x
        motion.Y_acc = y
        motion.Z_acc = z
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Accelerometer"

        //MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabData(motion)

        activity!!.runOnUiThread {
            quaternionModels.add(motion)
//            if (address1 == bluetoothDevice.address) {
//                //tvULab1!!.text = "$x,$y,$z,$w"
//                lastUlabModel1 = motion
//            } else if (address2 == bluetoothDevice.address) {
//                //tvULab2!!.text = "$x,$y,$z,$w"
//                lastUlabModel2 = motion
//            } else if (address3 == bluetoothDevice.address) {
//                //tvULab2!!.text = "$x,$y,$z,$w"
//                lastUlabModel3 = motion
//            }
        }
        checkModelAndStoreInDB()
    }

    override fun onGravityVectorChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
        //Log.d("InitialConfiguration", "onGravityVectorValueChanged = " + bluetoothDevice.address + " x = " + x + " y = " + y + " z = " + z)

        //Fill buffer if start is clicked and drift has stabilized
        if(start && dataReady)
        {
            exercise.buffer[0].addAccData(x, y, z)
        }

        val motion = ThingyDevice()
        motion.X_gravity = x
        motion.Y_gravity = y
        motion.Z_gravity = z
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Gravity"

        //MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabData(motion)

        activity!!.runOnUiThread {
            quaternionModels.add(motion)

//            if ("back" == bluetoothDevice.name) {
//                lastUlabModel1 = motion
//            } else if ("stuprigh" == bluetoothDevice.name) {
//                lastUlabModel2 = motion
//            } else if ("stupleft" == bluetoothDevice.name) {
//                lastUlabModel3 = motion
//            } else if ("head" == bluetoothDevice.name) {
//                lastUlabModel4 = motion
//            }

        }
        checkModelAndStoreInDB()
    }

        override fun onGyroscopeValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
            //Log.d("InitialConfiguration", "onGyroscopeValueChanged = " + bluetoothDevice.address + " x = " + x + " y = " + y + " z = " + z)
        }

        override fun onEulerAngleChanged(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float) {
        //Log.d("InitialConfiguration", "onEulerAngleChanged = " + bluetoothDevice.address + " roll = " + roll + " pitch = " + pitch + " yaw = " + yaw);
        //Log.e("InitialConfiguration", "count value = " + count)

        //Let's drift disappear
        if( (count >= validDataNbr) && !
                dataReady){
            dataReady = true
            val msg = Toast.makeText(context, "Data is ready", Toast.LENGTH_LONG)
            msg.show()
        }else count++

        //Fill buffer if start is clicked and drift has stabilized
        if(start && dataReady)
        {
            exercise.buffer[0].addEulerData(roll, pitch, yaw)
        }

        //Fill DB
        val motion = ThingyDevice()
        motion.roll_euler = roll
        motion.pitch_euler = pitch
        motion.yaw_euler = yaw
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Euler"

        //MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabData(motion)

        activity!!.runOnUiThread {
            quaternionModels.add(motion)

//            if ("back" == bluetoothDevice.name) {
//                lastUlabModel1 = motion
//            } else if ("stuprigh" == bluetoothDevice.name) {
//                lastUlabModel2 = motion
//            } else if ("stupleft" == bluetoothDevice.name) {
//                lastUlabModel3 = motion
//            } else if ("head" == bluetoothDevice.name) {
//                lastUlabModel4 = motion
//            }

        }
        checkModelAndStoreInDB()
    }

    override fun onHeadingValueChanged(bluetoothDevice: BluetoothDevice,heading: Float){
        //Log.d("InitialConfiguration", "onHeadingValueChanged = " + bluetoothDevice.address + " heading = " + heading)
        val motion = ThingyDevice()
        motion.heading = heading
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.event_type = "Heading"

        //MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabData(motion)

        activity!!.runOnUiThread {
            quaternionModels.add(motion)

//            if ("back" == bluetoothDevice.name) {
//                lastUlabModel1 = motion
//            } else if ("stuprigh" == bluetoothDevice.name) {
//                lastUlabModel2 = motion
//            } else if ("stupleft" == bluetoothDevice.name) {
//                lastUlabModel3 = motion
//            } else if ("head" == bluetoothDevice.name) {
//                lastUlabModel4 = motion
//            }

        }
        checkModelAndStoreInDB()
    }

    override fun onCommandValueChanged(bluetoothDevice: BluetoothDevice, answer: ByteArray) {}

    override fun onImpactValueChanged(bluetoothDevice: BluetoothDevice, impact: Int) {
        //Log.e("InitialConfiguration", "onImpactValueChanged = " + bluetoothDevice.address + " impact = " + impact )

        //Fill buffer if start is clicked and drift has stabilized
        if(start && dataReady)
        {
            validImpact++

            if(validImpact  <= exercise.reps)
            {
                exercise.buffer[0].impactDetect();
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            ThingyListenerHelper.unregisterThingyListener(context, mThingyListener)
        }

        if (!hidden && isAdded) {
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                ThingyListenerHelper.registerThingyListener(context, mThingyListener, mThingySdkManager!!.connectedDevices[i])
            }

            //MAX_DEVICES_FOR_CONNECTION = 4
        }
    }

    override fun defineLayoutResource(): Int {
        return R.layout.fragment_quaternions
    }

    override fun initializeComponent(view: View) {

        getCurrentState()
        // gullaume : no needed in kotlin
        ivStatus = view.findViewById(R.id.activity_home_ivStatus)

        // gullaume : no needed in kotlin
        tvElapsedTime = view.findViewById(R.id.activity_home_tvTime)
        // gullaume : no needed in kotlin
        tvULab1 = view.findViewById(R.id.activity_home_tvUlab1)
        // gullaume : no needed in kotlin
        tvULab2 = view.findViewById(R.id.activity_home_tvUlab2)
        // gullaume : no needed in kotlin
        tvULab3 = view.findViewById(R.id.activity_home_tvUlab3)
        tvULab4 = view.findViewById(R.id.activity_home_tvUlab4)
        // gullaume : no needed in kotlin
        tvULab1FSR = view.findViewById(R.id.activity_home_tvUlab1FSR)
        // gullaume : no needed in kotlin
        tvULab2FSR = view.findViewById(R.id.activity_home_tvUlab2FSR)
        // gullaume : no needed in kotlin
        tvULab3FSR = view.findViewById(R.id.activity_home_tvUlab3FSR)
        tvULab4FSR = view.findViewById(R.id.activity_home_tvUlab4FSR)
        // gullaume : no needed in kotlin
        tvULab1Label = view.findViewById(R.id.activity_home_tvUlab1Label)
        // gullaume : no needed in kotlin
        tvULab2Label = view.findViewById(R.id.activity_home_tvUlab2Label)
        // gullaume : no needed in kotlin
        tvULab3Label = view.findViewById(R.id.activity_home_tvUlab3Label)
        tvULab4Label = view.findViewById(R.id.activity_home_tvUlab4Label)

        tvImpact_traj = view.findViewById(R.id.tvImpact_traj)
        tvImpact_pos = view.findViewById(R.id.tvImpact_pos)
        tvImpact_acc = view.findViewById(R.id.tvImpact_acc)
        tvImpact_speed = view.findViewById(R.id.tvImpact_speed)
        tvImpact_reg = view.findViewById(R.id.tvImpact_reg)

        manageStatus(STATUS_DISCONNECTED)

        ivConnect.isChecked = true
        ivSend.isChecked = true
        chkEuler.isChecked = true

        ivConnect.setOnClickListener(this)
        ivDisconnect.setOnClickListener(this)
        ivStart.setOnClickListener(this)
        ivStop.setOnClickListener(this)
        ivSend.setOnClickListener(this)

        mThingySdkManager = ThingySdkManager.getInstance()

        selectedType = TYPE_QUATERNION

        header_tvTitle.text = ""
        Handler().postDelayed({ header_tvTitle.text = getString(R.string.quaternions) }, Constants.TITLE_ANIMATION)

        activity!!.registerReceiver(mLocationProviderChangedReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))

//        ThingyListenerHelper.registerThingyListener(context, mThingyListener)
        activity!!.registerReceiver(mBleStateChangedReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        header_ivSetting.setOnClickListener {
            if (mThingySdkManager!!.connectedDevices.size == MAX_DEVICES_FOR_CONNECTION) {
                if (ivStop.isChecked) {
                    dialogBuilder = null

                    showDialog(activity!!, getString(R.string.app_name), getString(R.string.alert_do_you_really_want_to_stop_capture), DialogInterface.OnClickListener { _, _ -> stopCapturingAndLoadNextFragment() })
                } else {
                    stopCapturingAndLoadNextFragment()
                }
            } else {
                Utils.displaySnackBar(activity, header_ivSetting, String.format(getString(R.string.alert_please_connect_two_thingy_devices), MAX_DEVICES_FOR_CONNECTION))
            }
        }

        ivStop.setOnClickListener {
            setValueToUI()

            MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabDataAll(quaternionModels)
            quaternionModels = ArrayList()

//            MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertUlabFsrDataAll(FsrModels)
//            FsrModels = ArrayList()

            super.onClick(ivStop)
        }

    }

    private fun stopCapturingAndLoadNextFragment() {
        if (ivStop.isChecked) {
            ivStop.performClick()
            disconnectAllThing()
        }

        addFragment(R.id.activity_home_llContainer, this, SettingQuaternionFragment(), false, false)
    }
}
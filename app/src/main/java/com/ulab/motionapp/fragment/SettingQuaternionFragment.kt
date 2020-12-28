package com.ulab.motionapp.fragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.ulab.motionapp.MotionApp
import com.ulab.motionapp.R
import com.ulab.motionapp.adapter.DeviceAdapter
import com.ulab.motionapp.common.Constants
import com.ulab.motionapp.common.DateUtils
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.db.QuaternionsConfigModel
import kotlinx.android.synthetic.main.fragment_gravity_setting.*
import kotlinx.android.synthetic.main.header_back.*
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager

/**
 * Created by R.S. on 18/10/18
 */
class SettingQuaternionFragment: BaseBLEFragment() {
    override fun setValueToUI() {

    }

    override fun onDevicesDisconnected() {

    }

    private var device1Address = ""
    private var device1Count = 0

    private var selectedPos = -1
    private var sesId = 0L

    private var lastClickedTime = 0L
    private var isCalibrationButtonClicked = false

    private lateinit var deviceAdapter: DeviceAdapter

    override fun onDevicesConnected() {

    }

    override fun onQuaternionValueChanged(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float) {
        if (!isFragmentRemoving && isCalibrationButtonClicked) {

            activity!!.runOnUiThread {
                if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size >= Constants.MAX_DEVICES_FOR_CONNECTION) {
                    Log.e("Device Motion", "Address :- " + bluetoothDevice.address + "-" + bluetoothDevice.name)

                    if (device1Count == Constants.MAX_DEVICE_VALUE_GATHERED) {
                        device1Count++
                        fragment_gravity_setting_tvCalibrated.visibility = View.VISIBLE
                        isCalibrationButtonClicked = false
                        ivStop.isChecked = true
                        super.onClick(ivStop)

                        if (snackBarForCalibration != null && snackBarForCalibration!!.isShown && ivStart!!.isChecked) {
                            ivStart!!.isChecked = false
                            dismissSnackBarForCalibration()
                        }

                        updateSessionValue(sesId)
                        stopTimer()
                        lastStoppedTime = 0

                    } else {
                        if (device1Address == bluetoothDevice.address && device1Count < Constants.MAX_DEVICE_VALUE_GATHERED) {
                            device1Count++
                            storeValueInDB(bluetoothDevice, x, y, z)
                        }
                    }
                }
            }
        }
    }

    override fun onFsrDataValueChanged(bluetoothDevice: BluetoothDevice, answer_FSR: ByteArray) {}

    override fun onImpactValueChanged(bluetoothDevice: BluetoothDevice, data: Int) {}

    override fun onCommandValueChanged(bluetoothDevice: BluetoothDevice, answer: ByteArray) {}

    override fun onAccelerometerValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onGravityVectorChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onGyroscopeValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onHeadingValueChanged(bluetoothDevice: BluetoothDevice,heading: Float){}

    override fun onEulerAngleChanged(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float) {

    }

    private fun storeValueInDB(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
        val motion = QuaternionsConfigModel()
        motion.valueX = x
        motion.valueY = y
        motion.valueZ = z
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sesId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        MotionApp.instance!!.getThingyDeviceDB()!!.quaternionsConfigDao().insertUlabData(motion)
    }

    fun setValue(value: Int): String? {
        return DateUtils.formatString(value)
    }

    fun getValue(tv: TextView): Int {
        val v = tv.text.toString().toInt()
        return if (v != 0) {
            (v / 5)
        } else {
            0
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        isFragmentRemoving = true
        ThingyListenerHelper.unregisterThingyListener(activity!!, mThingyListener)

    }

    @SuppressLint("SetTextI18n")
    override fun initializeComponent(view: View) {
        header_tvTitle.text = getString(R.string.quaternions_configuration)

        //Constants.MAX_DEVICES_FOR_CONNECTION = 1

        mThingySdkManager = ThingySdkManager.getInstance()
        fragment_gravity_setting_tvStartCalibrate.setOnClickListener {
            if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size >= Constants.MAX_DEVICES_FOR_CONNECTION) {
                isCalibrationButtonClicked = true
                device1Count = 0

                calendar = null
                startTimer()
                sesId = insertSessionValue()

                MotionApp.instance!!.getThingyDeviceDB()!!.quaternionsConfigDao().deleteAllModels(device1Address)

                fragment_gravity_setting_tvCalibrated.visibility = View.INVISIBLE

                ivStart.isChecked = true
                super.onClick(ivStart)
            }
        }

        if (mThingySdkManager!!.connectedDevices.size == 0) {
            activity!!.onBackPressed()
            return
        }

        selectedType = TYPE_QUATERNION

        fragment_gravity_setting_spConnectedDevices.post { fragment_gravity_setting_spConnectedDevices.dropDownWidth = fragment_gravity_setting_spConnectedDevices.width }

        deviceAdapter = DeviceAdapter(activity!!, R.layout.row_connected_devices, mThingySdkManager!!.connectedDevices)
        fragment_gravity_setting_spConnectedDevices.adapter = deviceAdapter

        fragment_gravity_setting_spConnectedDevices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPos = position
                device1Address = mThingySdkManager!!.connectedDevices[selectedPos].address
                ThingyListenerHelper.registerThingyListener(context, mThingyListener, mThingySdkManager!!.connectedDevices[selectedPos])
                setUI()
            }
        }

        header_ivBack.setOnClickListener(this)
    }

    private fun setUI() {
        fragment_gravity_setting_tvCalibrated.visibility = View.INVISIBLE
    }

    override fun onClick(view: View) {
        /*
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Utils.MAX_CLICK_INTERVAL) {
            return
        }
        lastClickedTime = SystemClock.elapsedRealtime()

        if (view.id == header_ivBack.id) {
            activity!!.onBackPressed()
        }
    }

    override fun defineLayoutResource(): Int {
        return R.layout.fragment_quaternions_setting
    }
}
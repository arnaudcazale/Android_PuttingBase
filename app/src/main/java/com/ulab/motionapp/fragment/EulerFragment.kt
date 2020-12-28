package com.ulab.motionapp.fragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import android.widget.TextView
import com.ulab.motionapp.MotionApp
import com.ulab.motionapp.R
import com.ulab.motionapp.common.Constants
import com.ulab.motionapp.common.DateUtils
import com.ulab.motionapp.common.FileUtils
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.db.DevicesModel
import com.ulab.motionapp.db.EulerAngleModel
import com.ulab.motionapp.db.EulerConfigModel
import com.ulab.motionapp.db.EulerModel
import kotlinx.android.synthetic.main.fragment_euler.*
import kotlinx.android.synthetic.main.header_back_with_setting.*
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager
import java.util.*

/**
 * Created by R.S. on 04/10/18
 */
class EulerFragment : BaseBLEFragment() {
    override fun onDevicesDisconnected() {
        lastAngle1Model = null
        lastAngle2Model = null
        lastUlabModel1 = null
        lastUlabModel2 = null

        resetUI()

    }

    override fun onDevicesConnected() {
        if (deviceModel1 == null) {
            getModelFromDB()
        }
    }

    private var lastUlabModel1: EulerModel? = null
    private var lastUlabModel2: EulerModel? = null

    private var deviceModel1: DevicesModel? = null
    private var deviceModel2: DevicesModel? = null

    private var avgAngle1Model: EulerConfigModel? = null
    private var avgAngle2Model: EulerConfigModel? = null

    private var lastAngle1Model: EulerAngleModel? = null
    private var lastAngle2Model: EulerAngleModel? = null

    private var address1 = ""
    private var address2 = ""

    private var eulerModels: ArrayList<EulerModel> = ArrayList()
    private var eulerAngleModels: ArrayList<EulerAngleModel> = ArrayList()

    @SuppressLint("SetTextI18n")
    override fun onQuaternionValueChanged(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float) {
    }

    override fun onGravityVectorChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onAccelerometerValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onEulerAngleChanged(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float) {
//        Log.d("InitialConfiguration", "onEulerAngleChanged = " + bluetoothDevice.address + " roll =" + roll + " pitch = " + pitch + " yaw = " + yaw)

        val motion = EulerModel()
        motion.valueR = roll
        motion.valueP = pitch
        motion.valueY = yaw
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
//        MotionApp.instance!!.getThingyDeviceDB()!!.eulerDao().insertUlabData(motion)

//        eulerModels.add(motion)

        processOnMainHandler(bluetoothDevice, motion, roll, pitch, yaw)

        checkModelAndStoreInDB()
    }

    override fun onHeadingValueChanged(bluetoothDevice: BluetoothDevice,heading: Float){}

    override fun onGyroscopeValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onFsrDataValueChanged(bluetoothDevice: BluetoothDevice, answer_FSR: ByteArray) {}

    override fun onCommandValueChanged(bluetoothDevice: BluetoothDevice, answer: ByteArray) {}

    override fun onImpactValueChanged(bluetoothDevice: BluetoothDevice, data : Int) {}

    private fun processOnMainHandler(bluetoothDevice: BluetoothDevice, motion: EulerModel, roll: Float, pitch: Float, yaw: Float) {
        if (!isFragmentRemoving) {

            activity!!.runOnUiThread {
                if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size > 1) {

//                    Log.e("Device Motion", "Address :- " + bluetoothDevice.address + "-" + bluetoothDevice.name)

                    if (!isTimerStarted && snackBarForCalibration!!.isShown) {

                        isTimerStarted = true
                        Constants.MAX_DEVICE_VALUE_TO_DISPLAY = (mThingySdkManager!!.getMotionInterval(mThingySdkManager!!.connectedDevices[0]))

                        dismissSnackBarForCalibration()
                        ivConnect.isChecked = false
                        ivDisconnect.isChecked = true
                        ivStart.isChecked = true
                        ivStop.isChecked = false

                        resetUI()

                    }

                    if (!snackBarForCalibration!!.isShown && !ivStart.isChecked && ivStop.isChecked) {
                        eulerModels.add(motion)
                    }

                    if (isTimerStarted) {

                        fragment_euler_ll1.visibility = View.VISIBLE
                        fragment_euler_ll2.visibility = View.VISIBLE
                        fragment_euler_ll3.visibility = View.VISIBLE

                        calculateAngleValues(bluetoothDevice.address, motion)

                        if (address1 == bluetoothDevice.address) {

//                        setXValue(tvUlab1_1, roll, 0)
//                        setYValue(tvUlab1_2, pitch, 0)
//                        setZValue(tvUlab1_3, yaw, 0)

                            lastUlabModel1 = motion
                        } else if (address2 == bluetoothDevice.address) {
//                        setXValue(tvUlab2_1, roll, 1)
//                        setYValue(tvUlab2_2, pitch, 1)
//                        setZValue(tvUlab2_3, yaw, 1)

                            lastUlabModel2 = motion

//                        setValueIntoTV(tvAngle_1, lastUlabModel1!!.valueR - roll)
//                        setValueIntoTV(tvAngle_2, lastUlabModel1!!.valueP - pitch)
//                        setValueIntoTV(tvAngle_3, lastUlabModel1!!.valueY - yaw)
                        }
                    }
                }
            }
        }
    }

    private fun resetUI() {
        if (tvUlab1_1 != null) {
            tvUlab1_1.text = ""
            tvUlab1_2.text = ""
            tvUlab1_3.text = ""
            tvUlab2_1.text = ""
            tvUlab2_2.text = ""
            tvUlab2_3.text = ""
            tvAngle_1.text = ""
            tvAngle_2.text = ""
            tvAngle_3.text = ""

            tvUlab1_1.setBackgroundColor(Color.TRANSPARENT)
            tvUlab1_2.setBackgroundColor(Color.TRANSPARENT)
            tvUlab1_3.setBackgroundColor(Color.TRANSPARENT)
            tvUlab2_1.setBackgroundColor(Color.TRANSPARENT)
            tvUlab2_2.setBackgroundColor(Color.TRANSPARENT)
            tvUlab2_3.setBackgroundColor(Color.TRANSPARENT)
            tvAngle_1.setBackgroundColor(Color.TRANSPARENT)
            tvAngle_2.setBackgroundColor(Color.TRANSPARENT)
            tvAngle_3.setBackgroundColor(Color.TRANSPARENT)

            fragment_euler_ll1.visibility = View.INVISIBLE
            fragment_euler_ll2.visibility = View.INVISIBLE
            fragment_euler_ll3.visibility = View.INVISIBLE
        }
    }

    override fun setValueToUI() {
        if (lastUlabModel1 != null) {
            setXValue(tvUlab1_1, lastUlabModel1!!.valueR, 0)
            setYValue(tvUlab1_2, lastUlabModel1!!.valueP, 0)
            setZValue(tvUlab1_3, lastUlabModel1!!.valueY, 0)

            tvAngle_1.setBackgroundResource(R.drawable.btn_selected)
            tvAngle_2.setBackgroundResource(R.drawable.btn_selected)
            tvAngle_3.setBackgroundResource(R.drawable.btn_selected)
            tvAngle_1.visibility = View.VISIBLE
            tvAngle_2.visibility = View.VISIBLE
            tvAngle_3.visibility = View.VISIBLE
        }
        if (lastUlabModel2 != null && lastUlabModel1 != null) {
            setXValue(tvUlab2_1, lastUlabModel2!!.valueR, 1)
            setYValue(tvUlab2_2, lastUlabModel2!!.valueP, 1)
            setZValue(tvUlab2_3, lastUlabModel2!!.valueY, 1)

            tvAngle_1.setBackgroundResource(R.drawable.btn_selected)
            tvAngle_2.setBackgroundResource(R.drawable.btn_selected)
            tvAngle_3.setBackgroundResource(R.drawable.btn_selected)
            tvAngle_2.visibility = View.VISIBLE
            tvAngle_3.visibility = View.VISIBLE

            setValueIntoTV(tvAngle_1, lastUlabModel1!!.valueR - lastUlabModel2!!.valueR)
            setValueIntoTV(tvAngle_2, lastUlabModel1!!.valueP - lastUlabModel2!!.valueP)
            setValueIntoTV(tvAngle_3, lastUlabModel1!!.valueY - lastUlabModel2!!.valueY)
        }
    }

    private fun setValueIntoTV(tv: TextView, value: Float) {
        Utils.setValueIntoTV(tv, value)
    }

    private fun calculateAngleValues(deviceAddress: String, currentModel: EulerModel) {
        val eulerAngleModel = EulerAngleModel()
        eulerAngleModel.valueX = currentModel.valueR
        eulerAngleModel.valueY = currentModel.valueP
        eulerAngleModel.valueZ = currentModel.valueY
        eulerAngleModel.timestamp = currentModel.timestamp
        eulerAngleModel.timestampinMillies = currentModel.timestampinMillies
        eulerAngleModel.session_id = currentModel.session_id
        eulerAngleModel.deviceName = currentModel.deviceName
        eulerAngleModel.deviceAddress = currentModel.deviceAddress

        val isLastModelsFilled = lastAngle1Model == null || lastAngle2Model == null
        if (address1 == deviceAddress) {
            //  Mode 1 = values of last captured device A minus last captured value of device B
            //  Mode 2 = values of last captured device A minus calibration values of device A
            //  Mode 3 = values of last captured device A minus calibration values of device B
            lastAngle1Model = eulerAngleModel
        } else {
            lastAngle2Model = eulerAngleModel
        }

        if (!isLastModelsFilled && lastAngle2Model != null && lastAngle2Model != null) {
            // Calculate Mode 1
            val cx1: Float = lastAngle1Model!!.valueX - lastAngle2Model!!.valueX
            val cy1: Float = lastAngle1Model!!.valueY - lastAngle2Model!!.valueY
            val cz1: Float = lastAngle1Model!!.valueZ - lastAngle2Model!!.valueZ

            // Calculate Mode 2
            val cx2: Float = eulerAngleModel.valueX - avgAngle1Model!!.valueR
            val cy2: Float = eulerAngleModel.valueY - avgAngle1Model!!.valueP
            val cz2: Float = eulerAngleModel.valueZ - avgAngle1Model!!.valueY

            // Calculate Mode 3
            val cx3: Float = eulerAngleModel.valueX - avgAngle2Model!!.valueR
            val cy3: Float = eulerAngleModel.valueY - avgAngle2Model!!.valueP
            val cz3: Float = eulerAngleModel.valueZ - avgAngle2Model!!.valueY

            eulerAngleModel.cx1 = cx1
            eulerAngleModel.cy1 = cy1
            eulerAngleModel.cz1 = cz1
            eulerAngleModel.cx2 = cx2
            eulerAngleModel.cy2 = cy2
            eulerAngleModel.cz2 = cz2
            eulerAngleModel.cx3 = cx3
            eulerAngleModel.cy3 = cy3
            eulerAngleModel.cz3 = cz3
        }
        eulerAngleModels.add(eulerAngleModel)

        checkModelAndStoreInDB()
//        MotionApp.instance!!.getThingyDeviceDB()!!.eulerAngleDao().insertUlabData(eulerAngleModel)
    }

    private fun checkModelAndStoreInDB() {
        if (eulerAngleModels.size == Constants.MAX_DB_MODELS_SIZE) {

            MotionApp.instance!!.getThingyDeviceDB()!!.eulerAngleDao().insertUlabDataAll(eulerAngleModels)
            eulerAngleModels = ArrayList()

            MotionApp.instance!!.getThingyDeviceDB()!!.eulerDao().insertUlabDataAll(eulerModels)
            eulerModels = ArrayList()
        }
    }

    private fun setXValue(tv: TextView, value: Float, pos: Int) {
        val min: Float
        val max: Float
        if (pos == 0) {
            min = deviceModel1!!.valueEulerMinR
            max = deviceModel1!!.valueEulerMaxR
        } else {
            min = deviceModel2!!.valueEulerMinR
            max = deviceModel2!!.valueEulerMaxR
        }
        tv.background = null
        if (value in min..max) {
//            min - value - max  == GREEN
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_green)
        } else if (value <= min && value <= max) {
//            value < min        == PURPLE
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_purple)
            checkMinAndPlaySound(pos)
        } else if (value >= max && value >= min) {
//            value > max        == RED
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_red)
            checkMaxAndPlaySound(pos)
        }
        tv.visibility = View.VISIBLE
        Utils.setValueIntoTV(tv, value)
    }

    private fun setYValue(tv: TextView, value: Float, pos: Int) {
        val min: Float
        val max: Float
        if (pos == 0) {
            min = deviceModel1!!.valueEulerMinP
            max = deviceModel1!!.valueEulerMaxP
        } else {
            min = deviceModel2!!.valueEulerMinP
            max = deviceModel2!!.valueEulerMaxP
        }
        tv.background = null
        if (value in min..max) {
//            min - value - max  == GREEN
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_green)
        } else if (value <= min && value <= max) {
//            value < min        == PURPLE
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_purple)
            checkMinAndPlaySound(pos)
        } else if (value >= max && value >= min) {
//            value > max        == RED
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_red)
            checkMaxAndPlaySound(pos)
        }
        tv.visibility = View.VISIBLE
        Utils.setValueIntoTV(tv, value)
    }

    private fun setZValue(tv: TextView, value: Float, pos: Int) {
        val min: Float
        val max: Float
        if (pos == 0) {
            min = deviceModel1!!.valueEulerMinY
            max = deviceModel1!!.valueEulerMaxY
        } else {
            min = deviceModel2!!.valueEulerMinY
            max = deviceModel2!!.valueEulerMaxY
        }
        tv.background = null
        if (value in min..max) {
//            min - value - max  == GREEN
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_green)
        } else if (value <= min && value <= max) {
//            value < min        == PURPLE
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_purple)
            checkMinAndPlaySound(pos)
        } else if (value >= max && value >= min) {
//            value > max        == RED
            tv.background = activity!!.getDrawable(R.drawable.bg_rounded_red)
            checkMaxAndPlaySound(pos)
        }
        tv.visibility = View.VISIBLE
        Utils.setValueIntoTV(tv, value)
    }

    private fun checkMinAndPlaySound(pos: Int) {
        if (pos == 0) {
            if (deviceModel1!!.warEulerMin) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        } else {
            if (deviceModel2!!.warEulerMin) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        }
    }

    private fun checkMaxAndPlaySound(pos: Int) {
        if (pos == 0) {
            if (deviceModel1!!.warEulerMax) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        } else {
            if (deviceModel2!!.warEulerMax) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun defineLayoutResource(): Int {
        return R.layout.fragment_euler
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
            //Constants.MAX_DEVICES_FOR_CONNECTION = 2

            getModelFromDB()
        }
    }

    override fun initializeComponent(view: View) {

        getCurrentState()
        ivStatus = view.findViewById(R.id.activity_home_ivStatus)

        tvElapsedTime = view.findViewById(R.id.activity_home_tvTime)
        tvULab1Label = view.findViewById(R.id.activity_home_tvUlab1Label)
        tvULab2Label = view.findViewById(R.id.activity_home_tvUlab2Label)

        manageStatus(STATUS_DISCONNECTED)
        ivConnect.isChecked = true

        ivConnect.setOnClickListener(this)
        ivDisconnect.setOnClickListener(this)
        ivStart.setOnClickListener(this)
        ivStop.setOnClickListener(this)

        mThingySdkManager = ThingySdkManager.getInstance()
//        ThingyListenerHelper.registerThingyListener(context, mThingyListener)
        MotionApp.instance!!.setThingySdkManager(mThingySdkManager!!)

        selectedType = TYPE_EULER

        header_tvTitle.text = ""
        Handler().postDelayed({ header_tvTitle.text = getString(R.string.euler_data) }, Constants.TITLE_ANIMATION)

        activity!!.registerReceiver(mLocationProviderChangedReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))

        activity!!.registerReceiver(mBleStateChangedReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        header_ivSetting.setOnClickListener {
            if (mThingySdkManager!!.connectedDevices.size == Constants.MAX_DEVICES_FOR_CONNECTION) {
                if (ivStop.isChecked) {
                    dialogBuilder = null

                    showDialog(activity!!, getString(R.string.app_name), getString(R.string.alert_do_you_really_want_to_stop_capture), DialogInterface.OnClickListener { _, _ -> stopCapturingAndLoadNextFragment() })
                } else {
                    stopCapturingAndLoadNextFragment()
                }
            } else {
                Utils.displaySnackBar(activity, header_ivSetting, getString(R.string.alert_please_connect_two_thingy_devices))
            }
        }

        ivStop.setOnClickListener {
            setValueToUI()

            MotionApp.instance!!.getThingyDeviceDB()!!.eulerDao().insertUlabDataAll(eulerModels)
            eulerModels = ArrayList()

            MotionApp.instance!!.getThingyDeviceDB()!!.eulerAngleDao().insertUlabDataAll(eulerAngleModels)
            eulerAngleModels = ArrayList()

            super.onClick(ivStop)
        }
    }

    private fun stopCapturingAndLoadNextFragment() {
        if (ivStop.isChecked) {
            ivStop.performClick()
            disconnectAllThing()
        }

        addFragment(R.id.activity_home_llContainer, this, SettingEulerFragment(), false, false)
    }

    private fun getModelFromDB() {
        deviceModel1 = MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().getDeviceModel(mThingySdkManager!!.connectedDevices[0].address)
        deviceModel2 = MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().getDeviceModel(mThingySdkManager!!.connectedDevices[1].address)

        if (deviceModel1 == null) {
            deviceModel1 = DevicesModel()
            deviceModel1!!.deviceAddress = mThingySdkManager!!.connectedDevices[0].address
            deviceModel1!!.deviceName = mThingySdkManager!!.connectedDevices[0].name
            MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().insertUlabData(deviceModel1!!)
        }
        if (deviceModel2 == null) {
            deviceModel2 = DevicesModel()
            deviceModel2!!.deviceAddress = mThingySdkManager!!.connectedDevices[1].address
            deviceModel2!!.deviceName = mThingySdkManager!!.connectedDevices[1].name
            MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().insertUlabData(deviceModel2!!)
        }

        address1 = mThingySdkManager!!.connectedDevices[0].address
        address2 = mThingySdkManager!!.connectedDevices[1].address

        try {
            avgAngle1Model = MotionApp.instance!!.getThingyDeviceDB()!!.eulerConfigDao().getAvgModel(address1, Constants.MAX_DEVICE_VALUE_GATHERED)
        } catch (e: Exception) {
            avgAngle1Model = EulerConfigModel()
            avgAngle1Model!!.deviceAddress = address1
            avgAngle1Model!!.valueR = 0f
            avgAngle1Model!!.valueP = 0f
            avgAngle1Model!!.valueY = 0f
        }

        try {
            avgAngle2Model = MotionApp.instance!!.getThingyDeviceDB()!!.eulerConfigDao().getAvgModel(address2, Constants.MAX_DEVICE_VALUE_GATHERED)
        } catch (e: Exception) {
            avgAngle2Model = EulerConfigModel()
            avgAngle2Model!!.deviceAddress = address2
            avgAngle2Model!!.valueR = 0f
            avgAngle2Model!!.valueP = 0f
            avgAngle2Model!!.valueY = 0f
        }
    }
}
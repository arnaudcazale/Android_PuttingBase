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
import com.ulab.motionapp.R.id.*
import com.ulab.motionapp.common.Constants
import com.ulab.motionapp.common.Constants.Companion.MAX_DEVICES_FOR_CONNECTION
import com.ulab.motionapp.common.DateUtils
import com.ulab.motionapp.common.FileUtils
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.db.DevicesModel
import com.ulab.motionapp.db.GravityModel
import kotlinx.android.synthetic.main.fragment_gravity.*
import kotlinx.android.synthetic.main.header_back_with_setting.*
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager

/**
 * Created by R.S. on 04/10/18
 */
class GravityFragment : BaseBLEFragment() {

    override fun onDevicesDisconnected() {
        MotionApp.instance!!.getThingyDeviceDB()!!.gravityDao().insertUlabDataAll(gravityModels)
        gravityModels = ArrayList()

        lastUlabModel1 = null
        lastUlabModel2 = null

        resetUI()
    }

    override fun onDevicesConnected() {
        if (deviceModel1 == null) {
            getModelFromDB()
        }
    }

    private var deviceModel1: DevicesModel? = null
    private var deviceModel2: DevicesModel? = null

    private var address1 = ""
    private var address2 = ""

    private var gravityModels: ArrayList<GravityModel> = ArrayList()

    private var lastUlabModel1: GravityModel? = null
    private var lastUlabModel2: GravityModel? = null

    @SuppressLint("SetTextI18n")
    override fun onQuaternionValueChanged(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float) {}

    override fun onAccelerometerValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onEulerAngleChanged(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float) {}

    override fun onGyroscopeValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onHeadingValueChanged(bluetoothDevice: BluetoothDevice,heading: Float){}

    override fun onFsrDataValueChanged(bluetoothDevice: BluetoothDevice, answer_FSR: ByteArray) {}

    override fun onCommandValueChanged(bluetoothDevice: BluetoothDevice, answer: ByteArray) {}

    override fun onImpactValueChanged(bluetoothDevice: BluetoothDevice, data: Int) {}

    private fun checkModelAndStoreInDB() {
        if (gravityModels.size == Constants.MAX_DB_MODELS_SIZE) {

            MotionApp.instance!!.getThingyDeviceDB()!!.gravityDao().insertUlabDataAll(gravityModels)
            gravityModels = ArrayList()
        }
    }

    override fun onGravityVectorChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {

//        Log.d("InitialConfiguration", "onGravityVectorChanged = " + bluetoothDevice.address + " x =" + x + " y = " + y + " z = " + z)

        fragment_gravity_ll1.visibility = View.VISIBLE
        fragment_gravity_ll2.visibility = View.VISIBLE

        val motion = GravityModel()
        motion.valueX = x
        motion.valueY = y
        motion.valueZ = z
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sessionId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
//        MotionApp.instance!!.getThingyDeviceDB()!!.gravityDao().insertUlabData(motion)

        gravityModels.add(motion)

        if (!isFragmentRemoving) {

            activity!!.runOnUiThread {
                if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size > 1) {
//                    Log.e("Device Motion", "Address :- " + bluetoothDevice.address + "-" + bluetoothDevice.name)

                    if (!isTimerStarted && snackBarForCalibration!!.isShown && ivStart.isChecked) {

                        dismissSnackBarForCalibration()
                        isTimerStarted = true

                        ivConnect.isChecked = false
                        ivDisconnect.isChecked = false
                        ivStart.isChecked = false
                        ivStop.isChecked = true

                        resetUI()

                        startTimer()
                    }

                    if (isTimerStarted) {

                        fragment_gravity_ll1.visibility = View.VISIBLE
                        fragment_gravity_ll2.visibility = View.VISIBLE

                        if (address1 == bluetoothDevice.address) {
//                        setXValue(tvGravityUlab1_1, x, 0)
//                        setYValue(tvGravityUlab1_2, y, 0)
//                        setZValue(tvGravityUlab1_3, z, 0)

                            lastUlabModel1 = motion

                        } else if (address2 == bluetoothDevice.address) {
//                        setXValue(tvGravityUlab2_1, x, 1)
//                        setYValue(tvGravityUlab2_2, y, 1)
//                        setZValue(tvGravityUlab2_3, z, 1)

                            lastUlabModel2 = motion

                        }
                    }
                    checkModelAndStoreInDB()
                }
            }
        }
    }

    private fun resetUI() {
        if (tvGravityUlab1_1 != null) {
            tvGravityUlab1_1.text = ""
            tvGravityUlab1_2.text = ""
            tvGravityUlab1_3.text = ""
            tvGravityUlab2_1.text = ""
            tvGravityUlab2_2.text = ""
            tvGravityUlab2_3.text = ""

            tvGravityUlab1_1.setBackgroundColor(Color.TRANSPARENT)
            tvGravityUlab1_2.setBackgroundColor(Color.TRANSPARENT)
            tvGravityUlab1_3.setBackgroundColor(Color.TRANSPARENT)
            tvGravityUlab2_1.setBackgroundColor(Color.TRANSPARENT)
            tvGravityUlab2_2.setBackgroundColor(Color.TRANSPARENT)
            tvGravityUlab2_3.setBackgroundColor(Color.TRANSPARENT)

            fragment_gravity_ll1.visibility = View.INVISIBLE
            fragment_gravity_ll2.visibility = View.INVISIBLE
        }
    }

    override fun setValueToUI() {
        if (lastUlabModel1 != null) {
            setXValue(tvGravityUlab1_1, lastUlabModel1!!.valueX, 0)
            setYValue(tvGravityUlab1_2, lastUlabModel1!!.valueY, 0)
            setZValue(tvGravityUlab1_3, lastUlabModel1!!.valueZ, 0)
        }
        if (lastUlabModel2 != null && lastUlabModel1 != null) {
            setXValue(tvGravityUlab2_1, lastUlabModel2!!.valueX, 1)
            setYValue(tvGravityUlab2_2, lastUlabModel2!!.valueY, 1)
            setZValue(tvGravityUlab2_3, lastUlabModel2!!.valueZ, 1)
        }
    }

    private fun setXValue(tv: TextView, value: Float, pos: Int) {
        val min: Float
        val max: Float
        if (pos == 0) {
            min = deviceModel1!!.valueGravityMinX
            max = deviceModel1!!.valueGravityMaxX
        } else {
            min = deviceModel2!!.valueGravityMinX
            max = deviceModel2!!.valueGravityMaxX
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
            if (deviceModel1!!.warGravityMin) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        } else {
            if (deviceModel2!!.warGravityMin) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        }
    }

    private fun checkMaxAndPlaySound(pos: Int) {
        if (pos == 0) {
            if (deviceModel1!!.warGravityMax) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        } else {
            if (deviceModel2!!.warGravityMax) {
                // Play Sound
                FileUtils.playBeepSound(activity)
            }
        }
    }

    private fun setYValue(tv: TextView, value: Float, pos: Int) {
        val min: Float
        val max: Float
        if (pos == 0) {
            min = deviceModel1!!.valueGravityMinY
            max = deviceModel1!!.valueGravityMaxY
        } else {
            min = deviceModel2!!.valueGravityMinY
            max = deviceModel2!!.valueGravityMaxY
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
            min = deviceModel1!!.valueGravityMinZ
            max = deviceModel1!!.valueGravityMaxZ
        } else {
            min = deviceModel2!!.valueGravityMinZ
            max = deviceModel2!!.valueGravityMaxZ
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun defineLayoutResource(): Int {
        return R.layout.fragment_gravity
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

            //MAX_DEVICES_FOR_CONNECTION = 2
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

        selectedType = TYPE_GRAVITY

        header_tvTitle.text = ""
        Handler().postDelayed({ header_tvTitle.text = getString(R.string.gravity_data) }, Constants.TITLE_ANIMATION)

        activity!!.registerReceiver(mLocationProviderChangedReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))

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
                Utils.displaySnackBar(activity, header_ivSetting, getString(R.string.alert_please_connect_two_thingy_devices))
            }
        }

        ivStop.setOnClickListener {
            setValueToUI()

            MotionApp.instance!!.getThingyDeviceDB()!!.gravityDao().insertUlabDataAll(gravityModels)
            gravityModels = ArrayList()

            super.onClick(ivStop)
        }
    }

    private fun stopCapturingAndLoadNextFragment() {
        if (ivStop.isChecked) {
            ivStop.performClick()
            disconnectAllThing()
        }

        addFragment(R.id.activity_home_llContainer, this, SettingGravityFragment(), false, false)
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
    }
}
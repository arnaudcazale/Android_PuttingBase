package com.ulab.motionapp.fragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.bigkoo.pickerview.MyOptionsPickerView
import com.ulab.motionapp.MotionApp
import com.ulab.motionapp.R
import com.ulab.motionapp.adapter.DeviceAdapter
import com.ulab.motionapp.common.Constants
import com.ulab.motionapp.common.Constants.Companion.MAX_DEVICES_FOR_CONNECTION
import com.ulab.motionapp.common.DateUtils
import com.ulab.motionapp.common.DateUtils.formatString
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.db.DevicesModel
import com.ulab.motionapp.db.GravityConfigModel
import kotlinx.android.synthetic.main.fragment_gravity_setting.*
import kotlinx.android.synthetic.main.header_back.*
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager

/**
 * Created by R.S. on 16/10/18
 */
class SettingGravityFragment : BaseBLEFragment(), View.OnClickListener {
    override fun setValueToUI() {

    }

    override fun onDevicesDisconnected() {

    }

    override fun onDevicesConnected() {

    }

    private var device1Address = ""
    private var device1Count = 0

    private var selectedPos = -1
    private var sesId = 0L

    private var lastClickedTime = 0L
    private var isCalibrationButtonClicked = false

    private lateinit var deviceAdapter: DeviceAdapter

    override fun onQuaternionValueChanged(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float) {
        Log.d("InitialConfiguration", "onGravityVectorChanged = " + bluetoothDevice.address + " x =" + x + " y = " + y + " z = " + z)
    }

    override fun onAccelerometerValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    override fun onGravityVectorChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
        if (!isFragmentRemoving && isCalibrationButtonClicked) {

            activity!!.runOnUiThread {
                if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size >= MAX_DEVICES_FOR_CONNECTION) {
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

    override fun onHeadingValueChanged(bluetoothDevice: BluetoothDevice,heading: Float){}

    override fun onCommandValueChanged(bluetoothDevice: BluetoothDevice, answer: ByteArray) {}

    override fun onImpactValueChanged(bluetoothDevice: BluetoothDevice, data: Int) {}

    override fun onGyroscopeValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

    private fun storeValueInDB(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
        val motion = GravityConfigModel()
        motion.valueX = x
        motion.valueY = y
        motion.valueZ = z
        val timestamp = System.currentTimeMillis()
        motion.timestampinMillies = "" + timestamp
        motion.timestamp = DateUtils.getDate(System.currentTimeMillis())
        motion.session_id = sesId
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        MotionApp.instance!!.getThingyDeviceDB()!!.gravityConfigDao().insertUlabData(motion)
    }

    private fun storeMinMaxInDB(bluetoothDevice: BluetoothDevice, minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float) {
        val motion = DevicesModel()
        motion.valueGravityMinX = minX
        motion.valueGravityMinY = minY
        motion.valueGravityMinZ = minZ
        motion.valueGravityMaxX = maxX
        motion.valueGravityMaxY = maxY
        motion.valueGravityMaxZ = maxZ
        motion.deviceName = bluetoothDevice.name
        motion.deviceAddress = bluetoothDevice.address
        motion.warGravityMin = fragment_gravity_setting_swMin.isChecked
        motion.warGravityMax = fragment_gravity_setting_swMax.isChecked
        MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().insertUlabData(motion)
    }

    override fun onEulerAngleChanged(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float) {

    }

    private val ULab1Min = 0
    private val ULab1Max = 1

    private var clickedPicker = ULab1Min

    private fun onConfirmClickListener(value1: Int, value2: Int, value3: Int) {
        when (clickedPicker) {
            ULab1Min -> {
                if (value1 <= getValue(fragment_gravity_setting_tvUlab1MaxR) && value2 <= getValue(fragment_gravity_setting_tvUlab1MaxP) && value3 <= getValue(fragment_gravity_setting_tvUlab1MaxY)) {
                    fragment_gravity_setting_tvUlab1MinR.text = setValue(value1)
                    fragment_gravity_setting_tvUlab1MinP.text = setValue(value2)
                    fragment_gravity_setting_tvUlab1MinY.text = setValue(value3)

                } else {
                    Utils.displaySnackBar(activity, fragment_gravity_setting_tvUlab1MinY, getString(R.string.alert_min_value_cannot_greater_than_max))
                }
            }
            ULab1Max -> {
                if (value1 >= getValue(fragment_gravity_setting_tvUlab1MinR) && value2 >= getValue(fragment_gravity_setting_tvUlab1MinP) && value3 >= getValue(fragment_gravity_setting_tvUlab1MinY)) {
                    fragment_gravity_setting_tvUlab1MaxR.text = setValue(value1)
                    fragment_gravity_setting_tvUlab1MaxP.text = setValue(value2)
                    fragment_gravity_setting_tvUlab1MaxY.text = setValue(value3)
                } else {
                    Utils.displaySnackBar(activity, fragment_gravity_setting_tvUlab1MinY, getString(R.string.alert_max_value_cannot_less_than_min))
                }
            }
        }
    }

    fun setValue(value: Int): String? {
        return formatString(value)
    }

    private fun getValue(tv: TextView): Int {
        val v = tv.text.toString().toInt()
        return if (v != 0) {
            (v)
        } else {
            0
        }
    }

    private fun getValueFromPicker(tv: TextView): Int {
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
        header_tvTitle.text = getString(R.string.gravity_configuration)

        //MAX_DEVICES_FOR_CONNECTION = 1

        mThingySdkManager = ThingySdkManager.getInstance()
        fragment_gravity_setting_tvStartCalibrate.setOnClickListener {
            if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size >= MAX_DEVICES_FOR_CONNECTION) {
                isCalibrationButtonClicked = true
                device1Count = 0

                calendar = null
                startTimer()
                sesId = insertSessionValue()

                MotionApp.instance!!.getThingyDeviceDB()!!.gravityConfigDao().deleteAllModels(device1Address)

                fragment_gravity_setting_tvCalibrated.visibility = View.INVISIBLE

                ivStart.isChecked = true
                super.onClick(ivStart)
            }
        }

        if (mThingySdkManager!!.connectedDevices.size == 0) {
            activity!!.onBackPressed()
            return
        }

        selectedType = TYPE_GRAVITY

        fragment_gravity_setting_llUlab1Min.setOnClickListener {
            clickedPicker = ULab1Min
            openNumberPickerDialog(
                    getValueFromPicker(fragment_gravity_setting_tvUlab1MinR),
                    getValueFromPicker(fragment_gravity_setting_tvUlab1MinP),
                    getValueFromPicker(fragment_gravity_setting_tvUlab1MinY))
        }
        fragment_gravity_setting_llUlab1Max.setOnClickListener {
            clickedPicker = ULab1Max
            openNumberPickerDialog(
                    getValueFromPicker(fragment_gravity_setting_tvUlab1MaxR),
                    getValueFromPicker(fragment_gravity_setting_tvUlab1MaxP),
                    getValueFromPicker(fragment_gravity_setting_tvUlab1MaxY))
        }

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

        fragment_gravity_setting_tvConfirm.setOnClickListener(this)
        fragment_gravity_setting_tvCancel.setOnClickListener(this)
        header_ivBack.setOnClickListener(this)
    }

    private fun setUI() {
//        for (i in 0..1) {
        fragment_gravity_setting_tvCalibrated.visibility = View.INVISIBLE

        fragment_gravity_setting_tvUlab1.text = mThingySdkManager!!.connectedDevices[selectedPos].name

        val devicesModel = MotionApp.instance!!.getThingyDeviceDB()!!.devicesDao().getDeviceModel(mThingySdkManager!!.connectedDevices[selectedPos].address)
        if (devicesModel != null) {
            fragment_gravity_setting_tvUlab1MinR.text = formatString(devicesModel.valueGravityMinX)
            fragment_gravity_setting_tvUlab1MinP.text = formatString(devicesModel.valueGravityMinY)
            fragment_gravity_setting_tvUlab1MinY.text = formatString(devicesModel.valueGravityMinZ)

            fragment_gravity_setting_tvUlab1MaxR.text = formatString(devicesModel.valueGravityMaxX)
            fragment_gravity_setting_tvUlab1MaxP.text = formatString(devicesModel.valueGravityMaxY)
            fragment_gravity_setting_tvUlab1MaxY.text = formatString(devicesModel.valueGravityMaxZ)

            fragment_gravity_setting_swMin.isChecked = devicesModel.warGravityMin
            fragment_gravity_setting_swMax.isChecked = devicesModel.warGravityMax

        }
    }
//  }

    override fun onClick(view: View) {
        /*
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Utils.MAX_CLICK_INTERVAL) {
            return
        }
        lastClickedTime = SystemClock.elapsedRealtime()

        if (view.id == fragment_gravity_setting_tvConfirm.id) {
            storeMinMaxInDB(mThingySdkManager!!.connectedDevices[selectedPos],
                    fragment_gravity_setting_tvUlab1MinR.text.toString().toFloat(),
                    fragment_gravity_setting_tvUlab1MinP.text.toString().toFloat(),
                    fragment_gravity_setting_tvUlab1MinY.text.toString().toFloat(),
                    fragment_gravity_setting_tvUlab1MaxR.text.toString().toFloat(),
                    fragment_gravity_setting_tvUlab1MaxP.text.toString().toFloat(),
                    fragment_gravity_setting_tvUlab1MaxY.text.toString().toFloat()
            )
            Utils.displaySnackBar(activity, fragment_gravity_setting_tvUlab1MinR, getString(R.string.alert_value_saved))

        } else if (view.id == fragment_gravity_setting_tvCancel.id || view.id == header_ivBack.id) {
            activity!!.onBackPressed()
        }
    }

    private fun openNumberPickerDialog(value1: Int, value2: Int, value3: Int) {
        //Three Options PickerView
        val threePicker: MyOptionsPickerView<String>? = MyOptionsPickerView(activity)
        val threeItemsOptions1 = ArrayList<String>()
        val threeItemsOptions2 = ArrayList<String>()
        val threeItemsOptions3 = ArrayList<String>()

        for (i in 0..27) {
            threeItemsOptions1.add(formatString(i * 5))
        }
        threeItemsOptions2.addAll(threeItemsOptions1.clone() as Collection<String>)
        threeItemsOptions3.addAll(threeItemsOptions1.clone() as Collection<String>)

        threePicker!!.setPicker(threeItemsOptions1, threeItemsOptions2, threeItemsOptions3, false)
        threePicker.setCyclic(false, false, false)
        threePicker.setSelectOptions(value1, value2, value3)

        threePicker.setOnoptionsSelectListener(MyOptionsPickerView.OnOptionsSelectListener { options1, options2, options3 ->
            onConfirmClickListener(threeItemsOptions1[options1].toInt(), threeItemsOptions2[options2].toInt(), threeItemsOptions3[options3].toInt())
        })
        threePicker.show()

    }

    override fun defineLayoutResource(): Int {
        return R.layout.fragment_gravity_setting
    }
}
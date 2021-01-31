package com.ulab.motionapp.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.location.LocationManager
import android.os.*
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.ulab.motionapp.MotionApp
import com.ulab.motionapp.R
import com.ulab.motionapp.ThingyService
import com.ulab.motionapp.activity.HomeActivity
import com.ulab.motionapp.common.Constants
import com.ulab.motionapp.common.Constants.Companion.MAX_DEVICES_FOR_CONNECTION
import com.ulab.motionapp.common.DateUtils
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.custom.Exercise
import com.ulab.motionapp.db.Session
import kotlinx.android.synthetic.main.fragment_euler.*
import kotlinx.android.synthetic.main.fragment_gravity.*
import kotlinx.android.synthetic.main.fragment_quaternions.*
import kotlinx.android.synthetic.main.header_back_with_setting.*
import no.nordicsemi.android.support.v18.scanner.*
import no.nordicsemi.android.thingylib.ThingyListener
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager
import no.nordicsemi.android.thingylib.utils.ThingyUtils
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by R.S. on 04/10/18
 */


// guillaume : All the BLE part should be encapsulate into a service or something responsible only for BLE interactions.
// Currently, it's doing way more than that, such as error message, display popups, sharing views ...
// There are no separation between the controller (the logic) and the views

abstract class BaseBLEFragment : BaseFragment(), EasyPermissions.PermissionCallbacks, View.OnClickListener {

    abstract fun onQuaternionValueChanged(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float)
    abstract fun onAccelerometerValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float)
    abstract fun onGravityVectorChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float)
    abstract fun onEulerAngleChanged(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float)
    abstract fun onFsrDataValueChanged(bluetoothDevice: BluetoothDevice, answer_FSR: ByteArray)
    abstract fun onHeadingValueChanged(bluetoothDevice: BluetoothDevice, heading: Float)
    abstract fun onCommandValueChanged(bluetoothDevice: BluetoothDevice, answer: ByteArray)
    abstract fun onGyroscopeValueChanged(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float)
    abstract fun onImpactValueChanged(bluetoothDevice: BluetoothDevice, data: Int)
    abstract fun onDevicesConnected()
    abstract fun onDevicesDisconnected()
    abstract fun setValueToUI()

    companion object {
        private const val RC_LOCATION = 102
        //     // guillaume : again STATE_1 ? But not the Same as the one in the constant file ??
        private const val STATE_1: Int = 1
        private const val STATE_2: Int = 2
        private const val STATE_3: Int = 3
        private const val STATE_4: Int = 4
    }

    ///////////////////////////////////////////////////////////
    var start = false
    var dataReady: Boolean = false
    val validDataNbr: Int = 1000
    var count: Int = 0
    var validImpact: Int = 0
    var tvImpact_traj: TextView? = null
    var tvImpact_pos: TextView? = null
    var tvImpact_acc: TextView? = null
    var tvImpact_speed: TextView? = null
    var tvImpact_reg: TextView? = null
    val mlistener = object : Exercise.ExerciseListener {
        override fun onImpactTrajectoryChanged(trajectory: String?)
        {
            tvImpact_traj!!.text = trajectory
        }
        override fun onImpactPositionChanged(position: Array<Float?>?)
        {
            tvImpact_pos!!.text = "yaw =" + String.format("%.2f", position!![0]) + " pitch =" + String.format("%.2f", position!![1])
        }
        override fun onImpactAccelerationChanged(acceleration: String?)
        {
            tvImpact_acc!!.text = acceleration
        }
        override fun onImpactSpeedChanged(speed: Float?)
        {
            tvImpact_speed!!.text = String.format("%.4f", speed)
        }
        override fun onRegularityChanged(regularity: Array<String?>?)
        {
            Log.d("ExerciseListener", "onImpactTrajectoryChange " )
            tvImpact_reg!!.text = "yaw =" + regularity!![0].toString() + " pitch =" + regularity!![1].toString()
            endOfExercise();
        }
    }

    //Arnaud
    var exercise: Exercise = Exercise(Exercise.exerciseName.PUTTING_BASE, mlistener)

    //TODO set to 50 Hz each time, deal with disconect & drift compensation

    //var exercise: Exercise = Exercise(nbrSeries, mlistener)
    var tvUlabPBar: TextView? = null
    var pBar: ProgressBar? = null

    ////////////////////////////////////////////////////////////

     // guillaume : Type should be enum
    val TYPE_QUATERNION = 1
    val TYPE_GRAVITY = 2
    val TYPE_EULER = 3

    var selectedType = TYPE_QUATERNION

    var isTimerStarted = true
    private val STATUS_CONNECTED = 1
    val STATUS_DISCONNECTED = 0
    private val STATUS_ERROR = -1

    var isFragmentRemoving = false

    // guillaume :should not be there
    var ivStatus: ImageView? = null

    // guillaume :should not be there
    var tvElapsedTime: TextView? = null
    // guillaume :should not be there
    var tvULab1: TextView? = null
    // guillaume :should not be there
    var tvULab2: TextView? = null
    // guillaume :should not be there
    var tvULab3: TextView? = null
    var tvULab4: TextView? = null
    // guillaume :should not be there
    var tvULab1FSR: TextView? = null
    // guillaume :should not be there
    var tvULab2FSR: TextView? = null
    // guillaume :should not be there
    var tvULab3FSR: TextView? = null
    var tvULab4FSR: TextView? = null
    // guillaume :should not be there
    var tvULab1Label: TextView? = null
    // guillaume :should not be there
    var tvULab2Label: TextView? = null
    // guillaume :should not be there
    var tvULab3Label: TextView? = null
    // guillaume :should not be there
    var tvULab4Label: TextView? = null

    var m_string: String? = null
    var m_string_command: String? = null
    var m_string_argument: String? = null

    private lateinit var devicelist: ArrayList<BluetoothDevice>
    private var connectedDeviceList = ArrayList<BluetoothDevice>()

    private var connectionStatus = STATUS_DISCONNECTED

    /**
     * Max time interval to prevent double click
     */
    private val MAX_CLICK_INTERVAL: Long = 1000
    private var REQUEST_ENABLE_BT = 101
    private var REQUEST_LOCATION = 102

    private var isConnecting: Boolean = false
    private var isDisconnectClicked: Boolean = false

    var snackBar: Snackbar? = null
    var snackBarForCalibration: Snackbar? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var isFirstTime: Boolean = true

    var mThingySdkManager: ThingySdkManager? = null
    var mBinder: ThingyService.ThingyBinder? = null
    private var mDevice: BluetoothDevice? = null
    private val mProgressHandler = Handler()
    var calendar: Calendar? = null
    private var timer: Timer? = null
    private lateinit var timerTask: TimerTask

    var lastStoppedTime = 0L
    private var lastClickedTime = 0L
    var timeDiff = 0L
    private var currentState = STATE_1
    private var tempHandler: Handler? = null
    var sessionId = 0L

    private var errorBuilder: AlertDialog.Builder? = null
    var dialogBuilder: AlertDialog.Builder? = null

    private var nbrDeviceSerciveDiscovered = 0



    /**
     * Used for setting timer
     */
    fun startTimer() {
        //initialize the TimerTask's job
        if (calendar == null) {
            calendar = Calendar.getInstance()
            initializeTimerTask()
            timer = Timer()
            timer!!.schedule(timerTask, 0, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tempHandler = Handler()
        timer = Timer()
    }

    /**
     * Used for execute the timer task
     */
    private fun initializeTimerTask() {
        timerTask = object : TimerTask() {
            override fun run() {

                Handler(Looper.getMainLooper()).postDelayed({
                    if (calendar != null) {

                        setValueToUI()

                        val calNow = Calendar.getInstance()

                        timeDiff = lastStoppedTime + (calNow.timeInMillis - calendar!!.timeInMillis)
                        val diffInHHMMSS = DateUtils.diffInHHMMSS(timeDiff)

                        if (tvElapsedTime != null) {
                            tvElapsedTime!!.text = diffInHHMMSS
                        }
                    }
                }, 10)
            }
        }
    }

    /**
     * Used to stop the timer task
     */
    fun stopTimer() {
        //lastStoppedTime = 0
        lastStoppedTime = timeDiff
        isTimerStarted = false
        if (timer != null) {
            timer!!.cancel()
            calendar = null
        }
    }

    fun endOfExercise()
    {
        //Reset Impact count of each buffers
        for(i in 0 .. (exercise.nbrXpN-1))
        {
            exercise.buffer[i].impactReset();
        }
        //Set up UI for end session and stop impact notifs
        mThingySdkManager!!.enableImpactNotifications(mThingySdkManager!!.connectedDevices[0], false)
        ivStop.isChecked = false
        ivStart.isChecked = true
        start = false
        validImpact = 0
    }

    /**
     * This listener used when Thingy device is connected with Thingy SDK.
     */
    val mThingyListener = object : ThingyListener {

        override fun onDeviceConnected(device: BluetoothDevice, connectionState: Int) {
//            if (!mThingySdkManager!!.connectedDevices.contains(device)) {
            Log.d("DEVICE_CONNECT ", device.address + "_" + device.name)
            if (!connectedDeviceList.contains(device)) {
                connectedDeviceList.add(device)
            }
            if (connectedDeviceList.size == MAX_DEVICES_FOR_CONNECTION) {
                currentState = Constants.FOUND_ALL_DEVICES
                sessionId = insertSessionValue()
            }
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, connectionState: Int) {
            Log.e("DEVICE_DISCONNECT", device.address + "_" + device.name + " connectionState " + connectionState)

            try {
                Log.d(TAG, "Thread sleeping for 200 milliseconds")
                Thread.sleep(Constants.BLE_DELAY)
                Log.d(TAG, "Thread slept for 200 milliseconds")
            } catch (e: Exception) {
            }

            if (connectedDeviceList.contains(device)) {
                connectedDeviceList.remove(device)
            }

            if (connectionState == 133) {
                showErrorDialog(activity!!, getString(R.string.app_name), getString(R.string.alert_connection_error))
            }

            if (!isFragmentRemoving) {
                if (isDisconnectClicked) {
                    btnStopClicked()
                    manageStatus(STATUS_DISCONNECTED)
                } else {
                    dismissSnackBar()
                    if (connectedDeviceList.size < MAX_DEVICES_FOR_CONNECTION) {
                        manage1DevicesStatus()
                        btnDisconnectClicked()
                        btnStopClicked()
                        if (currentState == Constants.FOUND_ALL_DEVICES || currentState == Constants.STATE_3) {
                            showErrorDialog(activity!!, getString(R.string.app_name), getString(R.string.alert_connection_error))
                        } else if (currentState == Constants.STATE_1) {
                            showErrorDialog(activity!!, getString(R.string.app_name), String.format(getString(R.string.alert_unable_to_connect_two_devices), MAX_DEVICES_FOR_CONNECTION))
                        }
                    }
                }
            }
            currentState = Constants.STATE_1
        }

        override fun onServiceDiscoveryCompleted(device: BluetoothDevice) {
            Log.d(TAG, "onServiceDiscoveryCompleted, device = " + device.address)

            try {
                Log.d(TAG, "Thread sleeping for 200 milliseconds")
                Thread.sleep(Constants.BLE_DELAY)
                Log.d(TAG, "Thread slept for 200 milliseconds")
            } catch (e: Exception) {
            }

            onServiceDiscoveryCompletion(device)
        }

        override fun onBatteryLevelChanged(bluetoothDevice: BluetoothDevice, batteryLevel: Int) {}

        override fun onTemperatureValueChangedEvent(bluetoothDevice: BluetoothDevice, temperature: String) {}

        override fun onPressureValueChangedEvent(bluetoothDevice: BluetoothDevice, pressure: String) {}

        override fun onHumidityValueChangedEvent(bluetoothDevice: BluetoothDevice, humidity: String) {}

        override fun onAirQualityValueChangedEvent(bluetoothDevice: BluetoothDevice, eco2: Int, tvoc: Int) {}

        override fun onColorIntensityValueChangedEvent(bluetoothDevice: BluetoothDevice, red: Float, green: Float, blue: Float, alpha: Float) {}

        override fun onButtonStateChangedEvent(bluetoothDevice: BluetoothDevice, buttonState: Int) {}

        override fun onTapValueChangedEvent(bluetoothDevice: BluetoothDevice, direction: Int, count: Int) {}

        override fun onOrientationValueChangedEvent(bluetoothDevice: BluetoothDevice, orientation: Int) {}

        @SuppressLint("SetTextI18n")
        override fun onQuaternionValueChangedEvent(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float) {
            //Log.d("InitialConfiguration", "onQuaternionValueChanged = " + bluetoothDevice.address + " w =" + w + " x = " + x + " y = " + y + " z = " + z)
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingQuaternionFragment) {
                    fragment.onQuaternionValueChanged(bluetoothDevice, w, x, y, z)
                } else {
                    onQuaternionValueChanged(bluetoothDevice, w, x, y, z)
                }
            }
        }

        override fun onPedometerValueChangedEvent(bluetoothDevice: BluetoothDevice, steps: Int, duration: Long) {}

        override fun onAccelerometerValueChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingQuaternionFragment) {
                    fragment.onAccelerometerValueChanged(bluetoothDevice, x, y, z)
                } else {
                    onAccelerometerValueChanged(bluetoothDevice, x, y, z)
                }
            }
        }

        override fun onGyroscopeValueChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingEulerFragment) {
                    fragment.onGyroscopeValueChanged(bluetoothDevice, x, y, z)
                } else {
                    onGyroscopeValueChanged(bluetoothDevice, x, y, z)
                }
            }
        }

        override fun onCompassValueChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

        override fun onEulerAngleChangedEvent(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float) {
            //Log.d("InitialConfiguration", "onEulerAngleChanged = " + bluetoothDevice.address + " roll =" + roll + " pitch = " + pitch + " yaw = " + yaw)
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingEulerFragment) {
                    fragment.onEulerAngleChanged(bluetoothDevice, roll, pitch, yaw)
                } else {
                    onEulerAngleChanged(bluetoothDevice, roll, pitch, yaw)
                }
            }
        }

        override fun onRotationMatixValueChangedEvent(bluetoothDevice: BluetoothDevice, matrix: ByteArray) {}

        override fun onHeadingValueChangedEvent(bluetoothDevice: BluetoothDevice, heading: Float) {
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingQuaternionFragment) {
                    fragment.onHeadingValueChanged(bluetoothDevice, heading)
                } else {
                    onHeadingValueChanged(bluetoothDevice, heading)
                }
            }
        }

        override fun onGravityVectorChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {
            //Log.d("InitialConfiguration", "onGravityVectorChanged = " + bluetoothDevice.address + " x =" + x + " y = " + y + " z = " + z)
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingGravityFragment) {
                    fragment.onGravityVectorChanged(bluetoothDevice, x, y, z)
                } else {
                    onGravityVectorChanged(bluetoothDevice, x, y, z)
                }
            }
        }

        override fun onSpeakerStatusValueChangedEvent(bluetoothDevice: BluetoothDevice, status: Int) {}

        override fun onMicrophoneValueChangedEvent(bluetoothDevice: BluetoothDevice, data: ByteArray) {}

        override fun onFsrDataValueChangedEvent(bluetoothDevice: BluetoothDevice, answer_FSR: ByteArray) {
            //Log.d("InitialConfiguration", "onFSRDataValueChanged = " + bluetoothDevice.address + " ByteArray" + answer_FSR )
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingQuaternionFragment) {
                    fragment.onFsrDataValueChanged(bluetoothDevice, answer_FSR)
                } else {
                    onFsrDataValueChanged(bluetoothDevice, answer_FSR)
                }
            }
        }

        override fun onCommandValueChangedEvent(bluetoothDevice: BluetoothDevice, answer: ByteArray) {}

        override fun onImpactValueChangedEvent(bluetoothDevice: BluetoothDevice, data : Int) {
            //Log.d("InitialConfiguration", "onImpactValueChanged = " + bluetoothDevice.address + " ByteArray" + answer_FSR )
            if (!isFragmentRemoving && !isRemoving) {
                val fragment = fragmentManager!!.findFragmentById(R.id.activity_home_llContainer)
                if (fragment is SettingQuaternionFragment) {
                    fragment.onImpactValueChanged(bluetoothDevice, data)
                } else {
                    onImpactValueChanged(bluetoothDevice, data)
                }
            }
        }
    }

    override fun onClick(view: View) {
        /*
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < MAX_CLICK_INTERVAL) {
            return
        }
        lastClickedTime = SystemClock.elapsedRealtime()

        dismissSnackBar()

        if (view.id == ivConnect.id) {

            if (ivConnect.isChecked && !this.isConnecting) {
                isDisconnectClicked = false
                this.isConnecting = true
                isFirstTime = true
                ivDisconnect.isChecked = false
                ivStart.isChecked = false
                ivStop.isChecked = false

                // guillaume : this fragment should not be responsible for this
                if (tvULab1 != null) {
                    tvULab1!!.text = ""
                }
                // guillaume : this fragment should not be responsible for this
                if (tvULab2 != null) {
                    tvULab2!!.text = ""
                }
                // guillaume : this fragment should not be responsible for this
                if (tvULab3 != null) {
                    tvULab3!!.text = ""
                }
                if (tvULab4 != null) {
                    tvULab4!!.text = ""
                }

                val bluetoothManager = activity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                mBluetoothAdapter = bluetoothManager.adapter
                getBLEPermission()
            }
        } else if (view.id == ivDisconnect.id) {
            // Only one time this value should be become true when user comes in this fragment for first time.
            isTimerStarted = true
            isDisconnectClicked = true
            dismissSnackBarForCalibration()
            btnDisconnectClicked()
            connectedDeviceList.clear()
            stopTimer()
//            ivSend.visibility = View.GONE
//            ev_command_text.visibility = View.GONE
            command_text.isEnabled = false
            chkGravity.isEnabled = false
            chkEuler.isEnabled = false
            chkHeading.isEnabled = false
            chkAcc.isEnabled = false
            chkQuat.isEnabled = false
            // guillaume : this fragment should not be responsible for this
            if (tvULab1 != null) {
                tvULab1!!.text = ""
            }
            if (tvULab2 != null) {
                tvULab2!!.text = ""
            }
            if (tvULab3 != null) {
                tvULab3!!.text = ""
            }
            if (tvULab4 != null) {
                tvULab4!!.text = ""
            }
            // guillaume : this fragment should not be responsible for this
            if (tvULab1FSR != null) {
                tvULab1FSR!!.text = ""
            }
            // guillaume : this fragment should not be responsible for this
            if (tvULab2FSR != null) {
                tvULab2FSR!!.text = ""
            }
            // guillaume : this fragment should not be responsible for this
            if (tvULab3FSR != null) {
                tvULab3FSR!!.text = ""
            }
            if (tvULab4FSR != null) {
                tvULab4FSR!!.text = ""
            }

        } else if (view.id == ivStart.id) {

            /*m_string = command_text.getText().toString().toUpperCase()

            if (m_string != null && !m_string!!.isEmpty() && m_string!!.get(0) == '*') {
                try {
                    m_string_command = m_string!!.substring(1, m_string!!.indexOf(' '))
                    m_string_argument = m_string!!.substring(m_string!!.indexOf(' ') + 1)
                } catch (e: StringIndexOutOfBoundsException) {
                    m_string_command = m_string!!.substring(1)
                }

                Log.e("APP", "STRING COMMAND $m_string_command")
                Log.e("APP", "STRING ARGUMENT $m_string_argument")
            }

           /* m_string_command = "START"
            m_string = command_text.getText().toString().toUpperCase()
            Log.e("APP", "STRING COMMAND $m_string")*/

            parseCommand()
            val data_bytes = m_string!!.toByteArray()

            /** Send command to 4 devices **/
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                mThingySdkManager!!.sendCommandData(mThingySdkManager!!.connectedDevices[i], data_bytes)
                //Log.e("Send Command", "Command: " + data_bytes + i)
//                    try {
//                        Log.d(TAG, "Thread sleeping for 200 milliseconds")
//                        Thread.sleep(Constants.BLE_DELAY)
//                        Log.d(TAG, "Thread slept for 200 milliseconds")
//                    } catch (e: Exception) {
//                    }
            }
            /** Show data send to user **/
            val msg = Toast.makeText(context, "send data", Toast.LENGTH_LONG)
            msg.show()*/

            if(dataReady){

                start = true

                //TODO Erase all textViews

                //mThingySdkManager!!.enableEulerNotifications(mThingySdkManager!!.connectedDevices[0], true)
                //Log.e("configureNotifications", "enable euler notifications " )
                mThingySdkManager!!.enableImpactNotifications(mThingySdkManager!!.connectedDevices[0], true)
                Log.d("configureNotifications", "Enable impact notifications " )
                try {
                    Log.d(TAG, "Thread sleeping for 200 milliseconds")
                    Thread.sleep(Constants.BLE_DELAY)
                    Log.d(TAG, "Thread slept for 200 milliseconds")
                } catch (e: Exception) {
                }

                ivStart.isChecked = false
                ivStop.isChecked = true
                command_text.isEnabled = false
                chkGravity.isEnabled = false
                chkEuler.isEnabled = false
                chkHeading.isEnabled = false
                chkAcc.isEnabled = false
                chkQuat.isEnabled = false

            }else{

                val msg = Toast.makeText(context, "Wait for data ready", Toast.LENGTH_LONG)
                msg.show()
            }


        } else if (view.id == ivStop.id) {

            start = false
            /*m_string = "*STOP"
            m_string_command = "STOP"
            parseCommand()
            val data_bytes = m_string!!.toByteArray()
            /** Send command to all devices **/
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                mThingySdkManager!!.sendCommandData(mThingySdkManager!!.connectedDevices[i], data_bytes)
                //Log.e("Send Command", "Command: " + data_bytes + i)
//                    try {
//                        Log.d(TAG, "Thread sleeping for 200 milliseconds")
//                        Thread.sleep(Constants.BLE_DELAY)
//                        Log.d(TAG, "Thread slept for 200 milliseconds")
//                    } catch (e: Exception) {
//                    }
            }*/

            //mThingySdkManager!!.enableEulerNotifications(mThingySdkManager!!.connectedDevices[0], false)
            //Log.e("configureNotifications", "disable euler notifications " )
            //mThingySdkManager!!.enableGravityVectorNotifications(mThingySdkManager!!.connectedDevices[0], false)
            //Log.e("configureNotifications", "Enable impact notifications " )
            mThingySdkManager!!.enableImpactNotifications(mThingySdkManager!!.connectedDevices[0], false)
            Log.d("configureNotifications", "Disable impact notifications " )
            try {
                Log.d(TAG, "Thread sleeping for 200 milliseconds")
                Thread.sleep(Constants.BLE_DELAY)
                Log.d(TAG, "Thread slept for 200 milliseconds")
            } catch (e: Exception) {
            }

            ivStart.isChecked = true
            ivStop.isChecked = false

            command_text.isEnabled = true
            chkGravity.isEnabled = true
            chkEuler.isEnabled = true
            chkHeading.isEnabled = true
            chkAcc.isEnabled = true
            chkQuat.isEnabled = true

//            val fragment = this@BaseBLEFragment
//            if (!(fragment is SettingGravityFragment || fragment is SettingEulerFragment || fragment is SettingQuaternionFragment)) {
//                if (snackBarForCalibration == null || (snackBarForCalibration != null && !snackBarForCalibration!!.isShown)) {
//                    dismissSnackBarForCalibration()
//
//                    isDisconnectClicked = false
//                    setValueToUI()
//                    btnStopClicked()
//                    stopTimer()
//                }
//            } else {
//                isDisconnectClicked = false
//                setValueToUI()
//                btnStopClicked()
//                stopTimer()
//            }

        } else if( view.id == ivSend.id ) {
//            Log.e(TAG, "SEND CLICKED")
//            //Convert string to uppercase
//            m_string = ev_command_text.getText().toString().toUpperCase()
//            Log.e("APP", "STRING COMMAND $m_string")
//            //m_string = m_string.toUpperCase()
//
//            if (m_string != null && !m_string!!.isEmpty() && m_string!!.get(0) == '*') {
//                try {
//                    m_string_command = m_string!!.substring(1, m_string!!.indexOf(' '))
//                    m_string_argument = m_string!!.substring(m_string!!.indexOf(' ') + 1)
//                } catch (e: StringIndexOutOfBoundsException) {
//                    m_string_command = m_string!!.substring(1)
//                }
//
//                Log.e("APP", "STRING COMMAND $m_string_command")
//                Log.e("APP", "STRING ARGUMENT $m_string_argument")
//                // Good command format
//                parseCommand()
//                val data_bytes = m_string!!.toByteArray()
//
//                /** Send command to 3 devices **/
//                for (i in mThingySdkManager!!.connectedDevices.indices) {
//                    mThingySdkManager!!.sendCommandData(mThingySdkManager!!.connectedDevices[i], data_bytes)
//                    Log.e("Send Command", "Command: " + data_bytes + i)
////                    try {
////                        Log.d(TAG, "Thread sleeping for 200 milliseconds")
////                        Thread.sleep(Constants.BLE_DELAY)
////                        Log.d(TAG, "Thread slept for 200 milliseconds")
////                    } catch (e: Exception) {
////                    }
//                }
//
//                /** Show data send to user **/
//                val msg = Toast.makeText(context, "send data", Toast.LENGTH_LONG)
//                msg.show()
//
//            } else {
//                /** Wrong command format**/
//                val msg = Toast.makeText(context, "WRONG FORMAT", Toast.LENGTH_SHORT)
//                msg.setGravity(Gravity.TOP, 0, 0)
//                msg.show()
//            }

//            /** start task reponsible for display data to UI every seconds **/
//            startTimer()
////            ivConnect.isChecked = false
////            ivDisconnect.isChecked = false
////            ivStart.isChecked = false
////            ivStop.isChecked = true
//            isTimerStarted = true

        } else if (view.id == header_ivBack.id) {
            super.onClick(view)
        }
    }

    private fun parseCommand() {
        when (m_string_command) {
            "START" -> {

                lastStoppedTime = 0

                /** Send notifications to all devices **/
                for (i in mThingySdkManager!!.connectedDevices.indices) {
                    if(chkGravity.isChecked) {
                        mThingySdkManager!!.enableGravityVectorNotifications(mThingySdkManager!!.connectedDevices[i], true)
                        Log.e("configureNotifications", "Enable GravityVector notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkEuler.isChecked){
                        mThingySdkManager!!.enableEulerNotifications(mThingySdkManager!!.connectedDevices[i], true)
                        Log.e("configureNotifications", "Enable Euler notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkHeading.isChecked){
                        mThingySdkManager!!.enableHeadingNotifications(mThingySdkManager!!.connectedDevices[i], true)
                        Log.e("configureNotifications", "Enable Heading notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkAcc.isChecked){
                        mThingySdkManager!!.enableRawDataNotifications(mThingySdkManager!!.connectedDevices[i], true)
                        Log.e("configureNotifications", "Enable Acceleroùeter notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkQuat.isChecked){
                        mThingySdkManager!!.enableQuaternionNotifications(mThingySdkManager!!.connectedDevices[i], true)
                        Log.e("configureNotifications", "Enable Quaternions notifications" + mThingySdkManager!!.connectedDevices[i])
                    }

                    try {
                    Log.d(TAG, "Thread sleeping for 200 milliseconds")
                    Thread.sleep(Constants.BLE_DELAY)
                      Log.d(TAG, "Thread slept for 200 milliseconds")
                    } catch (e: Exception) {
                    }
                }

                /** start task responsible for display data to UI every seconds **/
                startTimer()
                isTimerStarted = true

            }

            "STOP" ->{

                /** Send notifications to all devices **/
                for (i in mThingySdkManager!!.connectedDevices.indices) {
                    if(chkGravity.isChecked) {
                        mThingySdkManager!!.enableGravityVectorNotifications(mThingySdkManager!!.connectedDevices[i], false)
                        Log.e("configureNotifications", "Disable GravityVector notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkEuler.isChecked){
                        mThingySdkManager!!.enableEulerNotifications(mThingySdkManager!!.connectedDevices[i], false)
                        Log.e("configureNotifications", "Disable Euler notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkHeading.isChecked){
                        mThingySdkManager!!.enableHeadingNotifications(mThingySdkManager!!.connectedDevices[i], false)
                        Log.e("configureNotifications", "Disable Heading notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkAcc.isChecked){
                        mThingySdkManager!!.enableRawDataNotifications(mThingySdkManager!!.connectedDevices[i], false)
                        Log.e("configureNotifications", "Disable Acceleroùeter notifications" + mThingySdkManager!!.connectedDevices[i])
                    }else if(chkQuat.isChecked){
                        mThingySdkManager!!.enableQuaternionNotifications(mThingySdkManager!!.connectedDevices[i], false)
                        Log.e("configureNotifications", "Disable Quaternions notifications" + mThingySdkManager!!.connectedDevices[i])
                    }
                }

                /**Stop timer only when thingy don't send any datas**/
                setValueToUI()
                currentState = STATE_2
                stopTimer()

            }

            else -> {

            }
        }
    }

    // Comment received by Client:
    // We have to take for granted that the Thingy needs some time to initialize once the motion process is started ( and not the connnection)
    // We need to manage this thru the app :
    // - by freezing the start/stop/settings buttons during 30 seconds after connection is done in each of the three motion capture option (quaternions, euler, gravity) and
    // displaying a message "Please wait while device is calibrating"
    private fun stopThreadFor30Seconds() {
        snackBarForCalibration = Snackbar.make(ivConnect, getString(R.string.please_wait_while_device_is_calibrating), Snackbar.LENGTH_INDEFINITE)
        snackBarForCalibration!!.view.setBackgroundResource(R.color.colorOrangeLight)
        snackBarForCalibration!!.view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        snackBarForCalibration!!.show()

        val fragment = this@BaseBLEFragment
        if (!(fragment is SettingGravityFragment || fragment is SettingEulerFragment || fragment is SettingQuaternionFragment)) {
            if (isTimerStarted) {
                Handler().postDelayed({
                    //            dismissSnackBarForCalibration()
                    isTimerStarted = false
                }, Constants.CALIBRATION_DURATION)
            } else {
                isTimerStarted = false
            }
        } else {
            isTimerStarted = false
        }
    }

    public fun dismissSnackBarForCalibration() {
        if (snackBarForCalibration != null && snackBarForCalibration!!.isShown) {
            snackBarForCalibration!!.dismiss()
        }
    }

    public fun disconnectAllThing(){
//        for (i in mThingySdkManager!!.connectedDevices.indices) {
//            if (selectedType == TYPE_QUATERNION) {
//                mThingySdkManager!!.enableQuaternionNotifications(mThingySdkManager!!.connectedDevices[i], false)
//            } else if (selectedType == TYPE_EULER) {
//                mThingySdkManager!!.enableEulerNotifications(mThingySdkManager!!.connectedDevices[i], false)
//            } else if (selectedType == TYPE_GRAVITY) {
//                mThingySdkManager!!.enableGravityVectorNotifications(mThingySdkManager!!.connectedDevices[i], false)
//            }
//        }
    }

    /**
     * This method manages the Buttons Status when user clicks on Disconnect button and disconnect all the connected devices from Thingy SDK
     */
    private fun btnDisconnectClicked() {
        if (ivDisconnect != null && ivDisconnect.isChecked) {
            ivConnect.isChecked = true
            ivDisconnect.isChecked = false
            ivStart.isChecked = false
            ivStop.isChecked = false

            disconnectDevices()

            // guillaume : this fragment should not be responsible for this
            if (tvULab1Label != null) {
                tvULab1Label!!.text = "Motion"
                //tvElapsedTime!!.text = ""
            }
            // guillaume : this fragment should not be responsible for this
            if (tvULab2Label != null) {
                tvULab2Label!!.text = "Motion"
            }
            // guillaume : this fragment should not be responsible for this
            if (tvULab3Label != null) {
                tvULab3Label!!.text = "Motion"
            }

            if (tvULab4Label != null) {
                tvULab4Label!!.text = "Motion"
            }

            updateSessionValue(sessionId)
        }
    }

    fun insertSessionValue(): Long {
//        val fragment = this@BaseBLEFragment
//        if (!(fragment is SettingGravityFragment || fragment is SettingEulerFragment || fragment is SettingQuaternionFragment)) {
        //Start Session
        val session = Session()
        session.duaration = "00:00:00"
        if (selectedType == TYPE_QUATERNION) {
            return MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().insertSession(session)
        } else if (selectedType == TYPE_EULER) {
            return MotionApp.instance!!.getThingyDeviceDB()!!.eulerDao().insertSession(session)
        } else {
            return MotionApp.instance!!.getThingyDeviceDB()!!.gravityDao().insertSession(session)
        }
//        }
    }

    fun updateSessionValue(sessionId: Long) {
//        val fragment = this@BaseBLEFragment
//            if (!(fragment is SettingGravityFragment || fragment is SettingEulerFragment || fragment is SettingQuaternionFragment)) {
        val session = Session()
        session.id = sessionId
        session.duaration = DateUtils.diffInHHMMSS(timeDiff)
        if (selectedType == TYPE_QUATERNION) {
            MotionApp.instance!!.getThingyDeviceDB()!!.thingyDeviceDao().updateSession(session)
        } else if (selectedType == TYPE_EULER) {
            MotionApp.instance!!.getThingyDeviceDB()!!.eulerDao().updateSession(session)
        } else if (selectedType == TYPE_GRAVITY) {
            MotionApp.instance!!.getThingyDeviceDB()!!.gravityDao().updateSession(session)
        }
        Log.e(TAG, "value updated")
        //            }
    }

    private fun disconnectDevices() {
        lastStoppedTime = 0

        this.isConnecting = false

        if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size > 0) {
            // guillaume : this fragment should not be responsible for this
            var device1: BluetoothDevice? = null
            try {
                device1 = mThingySdkManager!!.connectedDevices[0]
            } catch (e: Exception) {
            }

            // guillaume : this fragment should not be responsible for this
            var device2: BluetoothDevice? = null
            try {
                device2 = mThingySdkManager!!.connectedDevices[1]
            } catch (e: Exception) {
                //e.printStackTrace()
            }

            // guillaume : this fragment should not be responsible for this
            var device3: BluetoothDevice? = null
            try {
                device3 = mThingySdkManager!!.connectedDevices[2]
            } catch (e: Exception) {
                //e.printStackTrace()
            }

            var device4: BluetoothDevice? = null
            try {
                device4 = mThingySdkManager!!.connectedDevices[3]
            } catch (e: Exception) {
                //e.printStackTrace()
            }

            Handler().post {
                // guillaume : this fragment should not be responsible for this
                try {
                    if (device1 != null) {
                        mThingySdkManager!!.disconnectFromThingy(device1)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // guillaume : this fragment should not be responsible for this
                try {
                    if (device2 != null) {
                        mThingySdkManager!!.disconnectFromThingy(device2)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    // guillaume : this fragment should not be responsible for this
                    if (device3 != null) {
                        mThingySdkManager!!.disconnectFromThingy(device3)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    // guillaume : this fragment should not be responsible for this
                    if (device4 != null) {
                        mThingySdkManager!!.disconnectFromThingy(device4)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                try {
                    mThingySdkManager!!.disconnectFromAllThingies()
                } catch (e: Exception) {
                }

                // guillaume : this fragment should not be responsible for this
                if (tvULab1 != null) {
                    tvULab1!!.text = ""
                }
                // guillaume : this fragment should not be responsible for this
                if (tvULab2 != null) {
                    tvULab2!!.text = ""
                }
                // guillaume : this fragment should not be responsible for this
                if (tvULab3 != null) {
                    tvULab3!!.text = ""
                }
                if (tvULab4 != null) {
                    tvULab4!!.text = ""
                }
                onDevicesDisconnected()
                try {
                    fragment_gravity_ll1.visibility = View.INVISIBLE
                    fragment_gravity_ll2.visibility = View.INVISIBLE
                } catch (e: Exception) {
                }
                try {
                    fragment_euler_ll1.visibility = View.INVISIBLE
                    fragment_euler_ll2.visibility = View.INVISIBLE
                    fragment_euler_ll3.visibility = View.INVISIBLE
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * This method manages the Buttons Status when user clicks on Stop button and Disabled the Quaternion Notification from the Thingy Device.
     */
    private fun btnStopClicked() {
        if (ivStop != null && ivStop.isChecked) {
            currentState = STATE_2
            ivConnect.isChecked = false
            ivDisconnect.isChecked = true
            ivStart.isChecked = true
            ivStop.isChecked = false
        }
    }

    /**
     * Used for manage the BLE status whether Android Device's BLE is enabled or disabled.
     */
    val mBleStateChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        btnStopClicked()
                        btnDisconnectClicked()
                        getBLEPermission()
                    }
                }
            }
        }
    }

    /**
     * Used for manage the Location status whether Android Device's Location is enabled or disabled.
     */
    val mLocationProviderChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val enabled = isLocationEnabled()
            Log.d(TAG, "mLocationProviderChangedReceiver =  $enabled")
            if (!enabled) {
                btnStopClicked()
                btnDisconnectClicked()
                showLocationAlert()
            }
        }
    }

    /**
     * Used to handle the BLE scan listener
     */
    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.e("DEVICE", "onBatchScanResults = " + result.device.address)
            if (!devicelist.contains(result.device)) {
                devicelist.add(result.device)
//                onServiceDiscoveryCompletion(result.device)
                onDeviceSelected(result.device, result.device.name)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            Log.d("DEVICE", "onBatchScanResults = " + results.size)
            for (i in results.indices) {
                val result = results[i]
                if (!devicelist.contains(result.device)) {
                    devicelist.add(result.device)
//                    onServiceDiscoveryCompletion(result.device)
                    onDeviceSelected(result.device, result.device.name)
                    //Log.e("DEVICE", "onBatchScanResults = " + result.device + result.device.name)
                }
                try {
                    Log.d(TAG, "Thread sleeping for 200 milliseconds")
                    Thread.sleep(Constants.BLE_DELAY)
                    Log.d(TAG, "Thread slept for 200 milliseconds")
                } catch (e: Exception) {
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // should never be called
            Log.d(TAG, "onScanFailed = $errorCode")
            dismissSnackBar()
        }
    }

    /**Manage 3 connected device**/
    fun manage4DevicesStatus(){
        if ( mThingySdkManager!!.connectedDevices.size == MAX_DEVICES_FOR_CONNECTION ) {

            dismissSnackBar()
            ivConnect.isChecked = false
            manageStatus(STATUS_CONNECTED)
            stopScanForBLE()

            if (tvULab1Label != null) {
                var indice = 0
                for (i in mThingySdkManager!!.connectedDevices.indices) {
                    if (mThingySdkManager!!.connectedDevices[i].name == "back"){
                        indice = i
                    }
                }
                tvULab1Label!!.tag = mThingySdkManager!!.connectedDevices[indice].address
                tvULab1Label!!.text = "Motion - " + mThingySdkManager!!.connectedDevices[indice].name
            }
            // guillaume : this fragment should not be responsible for this
            if (tvULab2Label != null && mThingySdkManager!!.connectedDevices.size >= 2) {
                var indice = 0
                for (i in mThingySdkManager!!.connectedDevices.indices) {
                    if (mThingySdkManager!!.connectedDevices[i].name == "stuprigh"){
                        indice = i
                    }
                }
                tvULab2Label!!.tag = mThingySdkManager!!.connectedDevices[indice].address
                tvULab2Label!!.text = "Motion - " + mThingySdkManager!!.connectedDevices[indice].name
            }

            if (tvULab3Label != null && mThingySdkManager!!.connectedDevices.size >= 3) {
                var indice = 0
                for (i in mThingySdkManager!!.connectedDevices.indices) {
                    if (mThingySdkManager!!.connectedDevices[i].name == "stupleft"){
                        indice = i
                    }
                }
                tvULab3Label!!.tag = mThingySdkManager!!.connectedDevices[indice].address
                tvULab3Label!!.text = "Motion - " + mThingySdkManager!!.connectedDevices[indice].name
            }

            if (tvULab4Label != null && mThingySdkManager!!.connectedDevices.size >= 4) {
                var indice = 0
                for (i in mThingySdkManager!!.connectedDevices.indices) {
                    if (mThingySdkManager!!.connectedDevices[i].name == "head"){
                        indice = i
                    }
                }
                tvULab4Label!!.tag = mThingySdkManager!!.connectedDevices[indice].address
                tvULab4Label!!.text = "Motion - " + mThingySdkManager!!.connectedDevices[indice].name
            }

            //dismissSnackBar()
            //ivConnect.isChecked = false

            /**Enable FSR Notification for the 4 devices**/
            for (i in mThingySdkManager!!.connectedDevices.indices) {
                mThingySdkManager!!.enableFsrDataNotifications(mThingySdkManager!!.connectedDevices[i], true)
                Log.e("configureNotifications", "Enable FSR notifications " + i)
                try {
                    Log.d(TAG, "Thread sleeping for 200 milliseconds")
                    Thread.sleep(Constants.BLE_DELAY)
                    Log.d(TAG, "Thread slept for 200 milliseconds")
                } catch (e: Exception) {
                }
            }

//            try {
//                Log.d(TAG, "Thread sleeping for 200 milliseconds")
//                Thread.sleep(Constants.BLE_DELAY)
//                Log.d(TAG, "Thread slept for 200 milliseconds")
//            } catch (e: Exception) {
//            }

//            ivSend.visibility = View.VISIBLE
//            ev_command_text.visibility = View.VISIBLE
            ivDisconnect.isChecked = true
            ivStart.isChecked = true
            command_text.isEnabled = true
            chkGravity.isEnabled = true
            chkEuler.isEnabled = true
            chkHeading.isEnabled = true
            chkAcc.isEnabled = true
            chkQuat.isEnabled = true

            isDisconnectClicked = false
            currentState = STATE_3
            isFirstTime = false
            onDevicesConnected()

        }
//        isDisconnectClicked = false
//        currentState = STATE_3
//        isFirstTime = false
//        onDevicesConnected()
    }

    /**Manage 1 connected device**/
    fun manage1DevicesStatus()
    {
        Log.d(TAG, "connectedDevices.size = " + mThingySdkManager!!.connectedDevices.size)
        if ( mThingySdkManager!!.connectedDevices.size == MAX_DEVICES_FOR_CONNECTION ) {

            dismissSnackBar()
            ivConnect.isChecked = false
            manageStatus(STATUS_CONNECTED)
            stopScanForBLE()

            /*if (tvULab1Label != null) {
                var indice = 0
                for (i in mThingySdkManager!!.connectedDevices.indices) {
                    if (mThingySdkManager!!.connectedDevices[i].name == "back"){
                        indice = i
                    }
                }
                tvULab1Label!!.tag = mThingySdkManager!!.connectedDevices[indice].address
                tvULab1Label!!.text = "Motion - " + mThingySdkManager!!.connectedDevices[indice].name
            }*/

            /**Enable Euler and Raw Notification **/
            enableNotifications();

            try {
                Log.d(TAG, "Thread sleeping for 200 milliseconds")
                Thread.sleep(Constants.BLE_DELAY)
                Log.d(TAG, "Thread slept for 200 milliseconds")
            } catch (e: Exception) {
            }

            ivDisconnect.isChecked = true
            command_text.isEnabled = true

            tvUlabPBar!!.visibility = View.VISIBLE
            pBar!!.visibility = View.VISIBLE

            isDisconnectClicked = false
            currentState = STATE_3
            isFirstTime = false
            onDevicesConnected()
        }

    }

    private fun enableNotifications()
    {
        // connectedDevices[x] must be linked with the appropriate thingy, depending of XpertNode type (XpN, XpS, etc...)
        for (i in 0..(exercise.nbrXpN - 1) )
        {
            var mask = exercise.getXpNmask(i)

            if( (mask and 0x01) == 1 ) {
                mThingySdkManager!!.enableImpactNotifications(mThingySdkManager!!.connectedDevices[0], true)
                Log.d("enableNotifications", "enableImpactNotifications" )
            }
            if( (mask shr 1) and 0x01 == 1 ){
                mThingySdkManager!!.enableRawDataNotifications(mThingySdkManager!!.connectedDevices[0], true)
                Log.d("enableNotifications", "enableRawDataNotifications" )
            }
            if( (mask shr 2) and 0x01 == 1 ){
                mThingySdkManager!!.enableQuaternionNotifications(mThingySdkManager!!.connectedDevices[0], true)
                Log.d("enableNotifications", "enableQuaternionNotifications" )
            }
            if( (mask shr 3) and 0x01 == 1 ){
                mThingySdkManager!!.enableEulerNotifications(mThingySdkManager!!.connectedDevices[0], true)
                Log.d("enableNotifications", "enableEulerNotifications" )
            }
            if( (mask shr 4) and 0x01 == 1 ){
                mThingySdkManager!!.enableGravityVectorNotifications(mThingySdkManager!!.connectedDevices[0], true)
                Log.d("enableNotifications", "enableGravityVectorNotifications" )
            }
            if( (mask shr 5) and 0x01 == 1 ){
                mThingySdkManager!!.enableHeadingNotifications(mThingySdkManager!!.connectedDevices[0], true)
                Log.d("enableNotifications", "enableHeadingNotifications" )
            }
        }
        for (i in 0..exercise.nbrXpS ) {

        }
        for (i in 0..exercise.nbrXpI ) {

        }
        for (i in 0..exercise.nbrXpA ) {

        }
        for (i in 0..exercise.nbrXpR ) {

        }
        //exercise.getXpTotalNbr()
    }

    /**
     * Used to GPS/Location is enabled or not
     */
    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showLocationAlert()
        return isLocationEnabled()
    }

    /**
     * Used to display Alert when Android Device's GPS/Location is off.
     */
    @SuppressLint("MissingPermission")
    private fun showLocationAlert() {
        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.interval = 30 * 1000;
        locationRequest.fastestInterval = 5 * 1000;

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this.activity!!)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getBLEPermission()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(activity, RC_LOCATION)

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    /**
     * Used to GPS/Location is enabled or not
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Used to check BLE's Permission
     */
    @AfterPermissionGranted(RC_LOCATION)
    private fun getBLEPermission() {
        if (EasyPermissions.hasPermissions(this.activity!!, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (checkGPSEnabled()) {
                if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } else {
                    startScanForBLE()
                    displaySnackBar()
                }
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.alert_location_permission_needed), RC_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    /**
     * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out
     * using class ScannerServiceParser
     */
    private fun startScanForBLE() {
        devicelist = ArrayList()

        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(750).setUseHardwareBatchingIfSupported(false).setUseHardwareFilteringIfSupported(false).build()
        val filters = ArrayList<ScanFilter>()
        filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid(ThingyUtils.THINGY_BASE_UUID)).build())
        scanner.startScan(filters, settings, scanCallback)

        ivConnect.isChecked = false

        this.isConnecting = true
        //Handler to stop scan after the duration time out
        mProgressHandler.postDelayed(mBleScannerTimeoutRunnable, Constants.SCAN_DURATION.toLong())
    }

    /**
     * Stop scan if user tap Cancel button
     */
    private fun stopScanForBLE() {
        if (isConnecting) {
//            if (mBinder != null) {
//                mBinder!!.isScanningState = false
//            }
            Log.d(Utils.TAG, "Stopping scan")
//            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable)
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            isConnecting = false

        } else if (activity != null) {
//            if (mBinder != null) {
//                mBinder!!.isScanningState = false
//            }
            Log.d(Utils.TAG, "Stopping scan on rotation")
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable)
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            isConnecting = false
        }
    }

    private fun connectDevice() {
        for (item: BluetoothDevice in devicelist) {
            onDeviceSelected(item, item.name)
            // ...
//            if (!mThingySdkManager!!.connectedDevices.contains(item)) {
//                Handler().post {
//                    //                        stopScanForBLE()
//                    onDeviceSelected(item, item.name)
//                    Log.d(TAG, "Connect?")
//                }
//            }
        }
    }

    /**
     * After 30 sec of start BLE scanner, This method is called.
     */
    private val mBleScannerTimeoutRunnable: Runnable = Runnable {
        if (!isFragmentRemoving) {
            stopScanForBLE()
            if (devicelist.size == 0) {
                dismissSnackBar()
                val snackBar = Snackbar.make(ivConnect, getString(R.string.alert_device_not_found), Snackbar.LENGTH_LONG)
                snackBar.view.setBackgroundResource(R.color.colorOrangeLight)
                snackBar.view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
                snackBar.show()
                ivConnect.isChecked = true
            } else if (devicelist.size < MAX_DEVICES_FOR_CONNECTION) {
                manageStatus(STATUS_ERROR)
                dismissSnackBar()
                showErrorDialog(activity!!, getString(R.string.app_name), String.format(getString(R.string.alert_unable_to_connect_two_devices), MAX_DEVICES_FOR_CONNECTION))
//            } else {
                //btnDisconnectClicked()
//                connectDevice()
            }
        }
    }

    /**
     * This method connect the devices which are found in BLE scanning into Thingy SDK.
     */
    private fun onDeviceSelected(device: BluetoothDevice, @Suppress("UNUSED_PARAMETER") name: String?) {
        if (mThingySdkManager != null && activity != null) {
            if (devicelist.size <= MAX_DEVICES_FOR_CONNECTION) {
                mThingySdkManager!!.connectToThingy(activity!!, device, ThingyService::class.java)
                mDevice = device
                if (!mThingySdkManager!!.connectedDevices.contains(device)) {
                    ThingyListenerHelper.registerThingyListener(activity!!, mThingyListener, device)
                }
            }
            if (mThingySdkManager!!.connectedDevices.size == MAX_DEVICES_FOR_CONNECTION) {
                stopScanForBLE()
            }

        }
    }

    /**
     * This method enables the Quaternion Notification from the Thingy Device.
     */
    fun onServiceDiscoveryCompletion(device: BluetoothDevice?) {
        if (mThingySdkManager != null) {
            //mThingySdkManager!!.enableEnvironmentNotifications(device, true)

            mBinder = (activity as HomeActivity).getmBinder()

            nbrDeviceSerciveDiscovered++
            Log.e("onServiceDiscovery","nbrDeviceSerciveDiscovered = " + nbrDeviceSerciveDiscovered)

            if(nbrDeviceSerciveDiscovered == MAX_DEVICES_FOR_CONNECTION){
                nbrDeviceSerciveDiscovered = 0
                manage1DevicesStatus();
            }
            //manage2DevicesStatus()
//            Log.e("onServiceDiscovery","CALL 2 manage3DevicesStatus")
//            manage3DevicesStatus()

//            val motionInterval = mThingySdkManager!!.getMotionInterval(device)
//            if (motionInterval > 0) {
//                Log.d(TAG, "Device = " + device!!.address + " motionInterval = $motionInterval")
//            }

//        To set Motion frequency, use below line
//        mThingySdkManager!!.setMotionProcessingFrequency(device, 10)
        }
    }

    /**
     * This method calls when user closing the application
     */
    private fun stopScanOnExit() {
        if (isRemoving && isConnecting) {
//        if (activity!!.isFinishing) {
//            if (mBinder != null) {
//                mBinder!!.isScanningState = false
//            }
            Log.d(Utils.TAG, "Stopping scan")
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable)
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            isConnecting = false
        }
    }

    override fun onDestroyView() {

        dismissSnackBarForCalibration()
        dismissSnackBar()

        if (ivStop != null) {
            ivStop.isChecked = true
            btnStopClicked()

            if (sessionId != 0L) {
                updateSessionValue(sessionId)
            }
        }
        ThingyListenerHelper.unregisterThingyListener(activity!!, mThingyListener)

        val fragment = this
        if (!(fragment is SettingGravityFragment || fragment is SettingEulerFragment || fragment is SettingQuaternionFragment)) {

            isFragmentRemoving = true
            disconnectDevices()

            tempHandler!!.removeCallbacks(mStatusRunnable)

//        if (mBinder != null) {
//            val isFinishing = activity!!.isFinishing
//            mBinder!!.activityFinishing = isFinishing
//        }
            stopScanOnExit()

            try {
                activity!!.unregisterReceiver(mLocationProviderChangedReceiver)
                activity!!.unregisterReceiver(mBleStateChangedReceiver)
            } catch (e: Exception) {
            }
        }
        super.onDestroyView()
    }

    fun dismissSnackBar() {
        if (snackBar != null && snackBar!!.isShown) {
            snackBar!!.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                activity!!.finish()
                return
            } else {
                startScanForBLE()
                displaySnackBar()
            }
        } else if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                activity!!.finish()
                return
            } else {
                getBLEPermission()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun displaySnackBar() {
        snackBar = Snackbar.make(ivConnect, getString(R.string.connecting), Snackbar.LENGTH_INDEFINITE)
        snackBar!!.view.setBackgroundResource(R.color.colorOrangeLight)
        snackBar!!.view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        snackBar!!.show()
    }

    fun manageStatus(status: Int) {
        connectionStatus = status
        if (ivStatus != null) {
            when (connectionStatus) {
                STATUS_DISCONNECTED -> ivStatus!!.setBackgroundResource(R.drawable.bg_status_orange)
                STATUS_CONNECTED -> ivStatus!!.setBackgroundResource(R.drawable.bg_status_green)
                STATUS_ERROR -> ivStatus!!.setBackgroundResource(R.drawable.bg_status_red)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)
        this.isConnecting = false
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    fun getCurrentState() {
        tempHandler!!.postDelayed(mStatusRunnable, 5000)
    }

    private val mStatusRunnable: Runnable = Runnable {
        Log.e("Status", "Status is $currentState")
    }

    private fun showErrorDialog(context: Context, title: String, message: String) {
        if (errorBuilder == null) {
            // Initialize a new instance of
            errorBuilder = AlertDialog.Builder(context)

            // Set the alert dialog title
            errorBuilder!!.setTitle(title)

            // Display a message on alert dialog
            errorBuilder!!.setMessage(message)
            errorBuilder!!.setCancelable(false)

            // Set a positive button and its click listener on alert dialog
            errorBuilder!!.setPositiveButton("ok") { _, _ ->
                if (message == getString(R.string.alert_connection_error)) {
                    activity!!.finish()
                } else {
                    activity!!.onBackPressed()
                }
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = errorBuilder!!.create()
            // Display the alert dialog on app interface
            dialog.show()

//        dialog.setOnDismissListener { dialogInterface: DialogInterface? ->
//            dialog != null
//
//        }
        }
    }

    fun showDialog(context: Context, title: String, message: String, onOkClickListener: DialogInterface.OnClickListener) {
        if (dialogBuilder == null) {
            // Initialize a new instance of
            dialogBuilder = AlertDialog.Builder(context)

            // Set the alert dialog title
            dialogBuilder!!.setTitle(title)

            // Display a message on alert dialog
            dialogBuilder!!.setMessage(message)
            dialogBuilder!!.setCancelable(false)

            // Set a positive button and its click listener on alert dialog
            dialogBuilder!!.setPositiveButton(getString(android.R.string.yes)) { dialogInterface: DialogInterface?, which: Int ->
                onOkClickListener.onClick(dialogInterface, which)
                dialogBuilder = null
            }

            dialogBuilder!!.setNegativeButton(getString(android.R.string.no)) { _, _ ->
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = dialogBuilder!!.create()
            // Display the alert dialog on app interface
            dialog.show()

//        dialog.setOnDismissListener { dialogInterface: DialogInterface? ->
//            dialog != null
//
//        }
        }
    }
}
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
import android.os.Handler
import android.os.ParcelUuid
import android.os.SystemClock
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.TextView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.ulab.motionapp.R
import com.ulab.motionapp.ThingyService
import com.ulab.motionapp.activity.HomeActivity
import com.ulab.motionapp.adapter.DeviceAdapter
import com.ulab.motionapp.common.Constants
import com.ulab.motionapp.common.Utils
import com.ulab.motionapp.common.Utils.MAX_CLICK_INTERVAL
import kotlinx.android.synthetic.main.fragment_general_setting.*
import kotlinx.android.synthetic.main.header_back.*
import no.nordicsemi.android.support.v18.scanner.*
import no.nordicsemi.android.thingylib.ThingyListener
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager
import no.nordicsemi.android.thingylib.utils.ThingyUtils
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

/**
 * Created by R.S. on 11/10/18
 */
class GeneralSettingFragment : BaseFragment(), EasyPermissions.PermissionCallbacks, CompoundButton.OnCheckedChangeListener {
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        resetCheckboxValues()

        when (buttonView!!.id) {
            fragment_general_setting_cb10.id -> {
                fragment_general_setting_cb10.isChecked = true
                selectedFreValue = 10
            }
            fragment_general_setting_cb15.id -> {
                fragment_general_setting_cb15.isChecked = true
                selectedFreValue = 15
            }
            fragment_general_setting_cb20.id -> {
                fragment_general_setting_cb20.isChecked = true
                selectedFreValue = 20
            }
            fragment_general_setting_cb50.id -> {
                fragment_general_setting_cb50.isChecked = true
                selectedFreValue = 50
            }
            fragment_general_setting_cb100.id -> {
                fragment_general_setting_cb100.isChecked = true
                selectedFreValue = 100
            }
        }

        setCheckboxListener()
    }

    private fun resetCheckboxValues() {
        fragment_general_setting_cb10.setOnCheckedChangeListener(null)
        fragment_general_setting_cb15.setOnCheckedChangeListener(null)
        fragment_general_setting_cb20.setOnCheckedChangeListener(null)
        fragment_general_setting_cb50.setOnCheckedChangeListener(null)
        fragment_general_setting_cb100.setOnCheckedChangeListener(null)

        fragment_general_setting_cb10.isChecked = false
        fragment_general_setting_cb15.isChecked = false
        fragment_general_setting_cb20.isChecked = false
        fragment_general_setting_cb50.isChecked = false
        fragment_general_setting_cb100.isChecked = false
    }

    companion object {
        private const val RC_LOCATION = 102
    }

    private var isConnecting: Boolean = false
    private var isClickedOnAnotherDevice: Boolean = false

    private var connectedDevice: BluetoothDevice? = null

    private var REQUEST_ENABLE_BT = 101

    private var selectedDevicePos = 0;
    private var selectedFreValue = 0;
    private var lastClickedTime = 0L;

    private lateinit var deviceAdapter: DeviceAdapter

    private var deviceList = ArrayList<BluetoothDevice>()
    private var connectedDeviceList = ArrayList<BluetoothDevice>()

    private var snackBar: Snackbar? = null

    private val mProgressHandler = Handler()

    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var mThingySdkManager: ThingySdkManager? = null
    private var mBinder: ThingyService.ThingyBinder? = null

    private var errorBuilder: AlertDialog.Builder? = null

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
                    displaySnackBarForInfinite(getString(R.string.scanning_devices))
                }
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.alert_location_permission_needed), RC_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    @SuppressLint("SetTextI18n")
    /**
     * This method enables the Quaternion Notification from the Thingy Device.
     */
    fun onServiceDiscoveryCompletion(device: BluetoothDevice?) {
        if (mThingySdkManager != null) {
            //mThingySdkManager!!.enableEnvironmentNotifications(device, true)

            mBinder = (activity as HomeActivity).getmBinder()

            val motionInterval = mThingySdkManager!!.getMotionInterval(device)
            if (motionInterval > 0) {
                Log.d(TAG, "Device = " + device!!.address + " motionInterval = $motionInterval")
            }

            dismissSnackBar()
            fragment_general_setting_llMotionFrq.visibility = View.VISIBLE
            fragment_general_setting_SettingName.visibility = View.VISIBLE
            fragment_general_setting_SettingName.visibility = View.VISIBLE

            connectedDevice = device
            fragment_general_setting_tvConnected.visibility = View.VISIBLE
            fragment_general_setting_tvConnected.text = getString(R.string.connected) + " : " + connectedDevice!!.name
            displayFrequency(motionInterval)

            when (motionInterval) {
                10 -> {
                    fragment_general_setting_cb10.setOnCheckedChangeListener(null)
                    fragment_general_setting_cb10.isChecked = true
                    fragment_general_setting_cb10.setOnCheckedChangeListener(this)
                }
                15 -> {
                    fragment_general_setting_cb15.setOnCheckedChangeListener(null)
                    fragment_general_setting_cb15.isChecked = true
                    fragment_general_setting_cb15.setOnCheckedChangeListener(this)
                }
                20 -> {
                    fragment_general_setting_cb20.setOnCheckedChangeListener(null)
                    fragment_general_setting_cb20.isChecked = true
                    fragment_general_setting_cb20.setOnCheckedChangeListener(this)
                }
                50 -> {
                    fragment_general_setting_cb50.setOnCheckedChangeListener(null)
                    fragment_general_setting_cb50.isChecked = true
                    fragment_general_setting_cb50.setOnCheckedChangeListener(this)
                }
                100 -> {
                    fragment_general_setting_cb100.setOnCheckedChangeListener(null)
                    fragment_general_setting_cb100.isChecked = true
                    fragment_general_setting_cb100.setOnCheckedChangeListener(this)
                }

            }

//        To set Motion frequency, use below line
//        mThingySdkManager!!.setMotionProcessingFrequency(device, 10)
        }
    }

    private fun displayFrequency(motionInterval: Int) {
        selectedFreValue = motionInterval
        fragment_general_setting_tvCurrentFrq.text = motionInterval.toString() + " Hz"
    }

    /**
     * Used to handle the BLE scan listener
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.e("DEVICE", "onBatchScanResults = " + result.device.address)
            if (!deviceList.contains(result.device)) {
                deviceList.add(result.device)
                deviceAdapter.notifyDataSetChanged()
//                onServiceDiscoveryCompletion(result.device)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            Log.d("DEVICE", "onBatchScanResults = " + results.size)
            for (i in results.indices) {
                val result = results[i]
                if (!deviceList.contains(result.device)) {
                    deviceList.add(result.device)
                    deviceAdapter.notifyDataSetChanged()
//                    onServiceDiscoveryCompletion(result.device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // should never be called
            Log.d(TAG, "onScanFailed = $errorCode")
            dismissSnackBar()
        }
    }

    /**
     * Stop scan if user tap Cancel button
     */
    private fun stopScanForBLE() {
        if (isConnecting) {
            if (mBinder != null) {
                mBinder!!.isScanningState = false
            }
            Log.d(TAG, "Stopping scan")
//            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable)
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            isConnecting = false

        } else if (!activity!!.isFinishing) {
            if (mBinder != null) {
                mBinder!!.isScanningState = false
            }
            Log.d(TAG, "Stopping scan on rotation")
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable)
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            isConnecting = false
        }
    }

    /**
     * After 30 sec of start BLE scanner, This method is called.
     */
    private val mBleScannerTimeoutRunnable: Runnable = Runnable {
        if (!isRemoving) {
            dismissSnackBar()
            stopScanForBLE()
            if (deviceList.size == 0) {
                val snackBar = Snackbar.make(fragment_general_setting_tvCancel, getString(R.string.alert_device_not_found), Snackbar.LENGTH_LONG)
                snackBar.view.setBackgroundResource(R.color.colorOrangeLight)
                snackBar.view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
                snackBar.show()
//        } else {
//            connectDevice()
            }
        }
    }

//
//    private fun connectDevice() {
//        for (item: BluetoothDevice in deviceList) {
//            onDeviceSelected(item, item.name)
//        }
//    }

    /**
     * This method connect the devices which are found in BLE scanning into Thingy SDK.
     */
    private fun onDeviceSelected(device: BluetoothDevice, @Suppress("UNUSED_PARAMETER") name: String?) {
        if (mThingySdkManager != null) {
            mThingySdkManager!!.connectToThingy(activity!!, device, ThingyService::class.java)
            ThingyListenerHelper.registerThingyListener(activity!!, mThingyListener, device)
        }
    }

    /**
     * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out
     * using class ScannerServiceParser
     */
    private fun startScanForBLE() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(750).setUseHardwareBatchingIfSupported(false).setUseHardwareFilteringIfSupported(false).build()
        val filters = ArrayList<ScanFilter>()
        filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid(ThingyUtils.THINGY_BASE_UUID)).build())
        scanner.startScan(filters, settings, scanCallback)

        this.isConnecting = true
        //Handler to stop scan after the duration time out
        mProgressHandler.postDelayed(mBleScannerTimeoutRunnable, Constants.SCAN_DURATION.toLong())
    }

    private fun displaySnackBarForInfinite(message: String) {
        snackBar = Snackbar.make(fragment_general_setting_tvCancel, message, Snackbar.LENGTH_INDEFINITE)
        snackBar!!.view.setBackgroundResource(R.color.colorOrangeLight)
        snackBar!!.view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        snackBar!!.show()
    }

    private fun displaySnackBarForShort(message: String) {
        snackBar = Snackbar.make(fragment_general_setting_tvCancel, message, Snackbar.LENGTH_SHORT)
        snackBar!!.view.setBackgroundResource(R.color.colorOrangeLight)
        snackBar!!.view.findViewById<TextView>(R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        snackBar!!.show()
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
     * Used for manage the Location status whether Android Device's Location is enabled or disabled.
     */
    private val mLocationProviderChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val enabled = isLocationEnabled()
            Log.d(TAG, "mLocationProviderChangedReceiver =  $enabled")
            if (!enabled) {
                disconnectClicked()
                showLocationAlert()
            }
        }

        /**
         * Used to display Alert when Android Device's GPS/Location is off.
         */
        private fun showLocationAlert() {
            isConnecting = false
            val dialog = AlertDialog.Builder(activity)
            dialog.setTitle(getString(R.string.alert_title_enable_location))
                    .setMessage(getString(R.string.alert_location_is_off))
                    .setPositiveButton(getString(R.string.alert_location_settings)) { _, _ ->
                        val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(myIntent, RC_LOCATION)
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            dialog.show()
        }
    }

    private fun disconnectClicked() {
        if (mThingySdkManager != null && mThingySdkManager!!.connectedDevices.size > 0) {
            mThingySdkManager!!.disconnectFromAllThingies()
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
                displaySnackBarForInfinite(getString(R.string.scanning_devices))
            }
        } else if (requestCode == RC_LOCATION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                activity!!.finish()
                return
            } else {
                getBLEPermission()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * This listener used when Thingy device is connected with Thingy SDK.
     */
    private val mThingyListener = object : ThingyListener {

        override fun onDeviceConnected(device: BluetoothDevice, connectionState: Int) {
//            if (!mThingySdkManager!!.connectedDevices.contains(device)) {
            Log.d("DEVICE_CONNECT ", device.address + "_" + device.name)
            if (!connectedDeviceList.contains(device)) {
                connectedDeviceList.add(device)
            }
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, connectionState: Int) {
            Log.e("DEVICE_DISCONNECT", device.address + "_" + device.name + " connectionState " + connectionState)
            if (connectedDeviceList.contains(device)) {
                connectedDeviceList.remove(device)
            }

            if (!isClickedOnAnotherDevice) {
                dismissSnackBar()
            }

            if (connectionState == 133) {
                showErrorDialog(activity!!, getString(R.string.app_name), getString(R.string.alert_connection_error))
            }

            isClickedOnAnotherDevice = false
        }

        override fun onServiceDiscoveryCompleted(device: BluetoothDevice) {
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

        override fun onQuaternionValueChangedEvent(bluetoothDevice: BluetoothDevice, w: Float, x: Float, y: Float, z: Float) {}

        override fun onPedometerValueChangedEvent(bluetoothDevice: BluetoothDevice, steps: Int, duration: Long) {}

        override fun onAccelerometerValueChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

        override fun onGyroscopeValueChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

        override fun onCompassValueChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

        override fun onEulerAngleChangedEvent(bluetoothDevice: BluetoothDevice, roll: Float, pitch: Float, yaw: Float) {}

        override fun onRotationMatixValueChangedEvent(bluetoothDevice: BluetoothDevice, matrix: ByteArray) {}

        override fun onHeadingValueChangedEvent(bluetoothDevice: BluetoothDevice, heading: Float) {}

        override fun onGravityVectorChangedEvent(bluetoothDevice: BluetoothDevice, x: Float, y: Float, z: Float) {}

        override fun onSpeakerStatusValueChangedEvent(bluetoothDevice: BluetoothDevice, status: Int) {}

        override fun onMicrophoneValueChangedEvent(bluetoothDevice: BluetoothDevice, data: ByteArray) {}

        override fun onFsrDataValueChangedEvent(bluetoothDevice: BluetoothDevice, answer_FSR: ByteArray) {}

        override fun onCommandValueChangedEvent(bluetoothDevice: BluetoothDevice, answer: ByteArray) {}

        override fun onImpactValueChangedEvent(bluetoothDevice: BluetoothDevice, data : Int) {}

    }

    private fun dismissSnackBar() {
        if (snackBar != null && snackBar!!.isShown) {
            snackBar!!.dismiss()
        }
    }

    override fun initializeComponent(view: View) {
        fragment_general_setting_tvConnect.setOnClickListener(this)
        fragment_general_setting_tvCancel.setOnClickListener(this)
        fragment_general_setting_tvConfirm.setOnClickListener(this)
        fragment_general_setting_tvConfirmName.setOnClickListener(this)

        mThingySdkManager = ThingySdkManager.getInstance()
        header_tvTitle.text = getString(R.string.general_settings)

        activity!!.registerReceiver(mLocationProviderChangedReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))

        ThingyListenerHelper.registerThingyListener(context, mThingyListener)
        activity!!.registerReceiver(mBleStateChangedReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        val bluetoothManager = activity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        getBLEPermission()

        fragment_general_setting_spDevices.post { fragment_general_setting_spDevices.dropDownWidth = fragment_general_setting_spDevices.width }

        deviceAdapter = DeviceAdapter(activity!!, R.layout.row_connected_devices, deviceList)
        fragment_general_setting_spDevices.adapter = deviceAdapter

        fragment_general_setting_spDevices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (selectedDevicePos != position) {
                    selectedDevicePos = position
                }
            }
        }
        setCheckboxListener()
    }

    private fun setCheckboxListener() {
        fragment_general_setting_cb10.setOnCheckedChangeListener(this)
        fragment_general_setting_cb15.setOnCheckedChangeListener(this)
        fragment_general_setting_cb20.setOnCheckedChangeListener(this)
        fragment_general_setting_cb50.setOnCheckedChangeListener(this)
        fragment_general_setting_cb100.setOnCheckedChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        dismissSnackBar()
        disconnectClicked()

        if (mBinder != null) {
            val isFinishing = activity!!.isFinishing
            mBinder!!.activityFinishing = isFinishing
        }
        stopScanOnExit()
        mBinder = null
        ThingyListenerHelper.unregisterThingyListener(activity!!, mThingyListener)

        activity!!.unregisterReceiver(mLocationProviderChangedReceiver)
        activity!!.unregisterReceiver(mBleStateChangedReceiver)
    }

    /**
     * This method calls when user closing the application
     */
    private fun stopScanOnExit() {
        if (isRemoving && isConnecting) {
            if (mBinder != null) {
                mBinder!!.isScanningState = false
            }
            Log.d(Utils.TAG, "Stopping scan")
            mProgressHandler.removeCallbacks(mBleScannerTimeoutRunnable)
            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)
            isConnecting = false
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

        when (view.id) {
            fragment_general_setting_tvConnect.id -> {
                if ((connectedDevice == null || (connectedDevice!!.name != deviceList[selectedDevicePos].name)) && deviceList.size != 0) {
                    stopScanOnExit()
                    isClickedOnAnotherDevice = true
                    disconnectClicked()
                    resetCheckboxValues()
                    setCheckboxListener()
                    displaySnackBarForInfinite(getString(R.string.connecting))
                    fragment_general_setting_llMotionFrq.visibility = View.GONE
                    fragment_general_setting_tvConnected.visibility = View.INVISIBLE

                    Handler().postDelayed({ onDeviceSelected(deviceList[selectedDevicePos], "") }, Constants.BLE_DELAY)
                }
            }
            fragment_general_setting_tvConfirm.id -> {
//                if (selectedFreValue != 0) {
                mThingySdkManager!!.setMotionProcessingFrequency(connectedDevice, selectedFreValue)
                displayFrequency(selectedFreValue)
                displaySnackBarForShort(getString(R.string.alert_value_updated))
//                }
            }
            fragment_general_setting_tvConfirmName.id -> {
                mThingySdkManager!!.setDeviceName(connectedDevice, fragment_general_setting_changeName_text.text.toString())
                displaySnackBarForShort(getString(R.string.alert_value_updated))
//                }
            }
            fragment_general_setting_tvCancel.id -> {
                activity!!.onBackPressed()
            }
            header_ivBack.id -> {
                super.onClick(view)
            }
        }
    }

    /**
     * Used for manage the BLE status whether Android Device's BLE is enabled or disabled.
     */
    private val mBleStateChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        disconnectClicked()
                        getBLEPermission()
                    }
                }
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
        }
    }

    override fun defineLayoutResource(): Int {
        return R.layout.fragment_general_setting
    }
}
package com.ulab.motionapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.ulab.motionapp.activity.HomeActivity;
import com.ulab.motionapp.common.Utils;
import com.ulab.motionapp.db.ThingyDevice;
import com.ulab.motionapp.db.ThingyDeviceDB;

import java.util.ArrayList;
import java.util.Objects;

import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyConnection;

import static com.ulab.motionapp.common.Utils.NOTIFICATION_ID;

public class ThingyService extends BaseThingyService {
    private static final String PRIMARY_GROUP = "Thingy:52 Connectivity Summary";
    private static final String PRIMARY_GROUP_ID = "com.ulab.motionapp.nrfthingy";
    private static final String PRIMARY_CHANNEL = "Thingy:52 Connectivity Status";
    private static final String PRIMARY_CHANNEL_ID = "com.ulab.motionapp.nrfthingy";

    private boolean mIsActivityFinishing = false;

    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;

    private ThingyDeviceDB deviceDB;

    private BroadcastReceiver mNotificationDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Utils.ACTION_DISCONNECT:
                    final BluetoothDevice device = intent.getExtras().getParcelable(Utils.EXTRA_DEVICE);
                    if (device != null) {
                        final ThingyConnection thingyConnection = mThingyConnections.get(device);
                        if (thingyConnection != null) {
                            thingyConnection.disconnect();
                            if (mDevices.contains(device)) {
                                mDevices.remove(device);
                            }
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //@Override
    private Class<? extends Activity> getNotificationTarget() {
        return HomeActivity.class;
    }


    @Override
    public void onDeviceConnected(final BluetoothDevice device, final int connectionState) {
        createBackgroundNotification();
        /*if (!mBound) {
        }*/
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device, final int connectionState) {
        super.onDeviceDisconnected(device, connectionState);
        cancelNotification(device);
//        createBackgroundNotification();
    }

    @Nullable
    @Override
    public ThingyBinder onBind(Intent intent) {
        return new ThingyBinder();
    }

    @Override
    protected void onRebind() {
        cancelNotifications();
        createBackgroundNotification();
    }

    @Override
    protected void onUnbind() {
        if (mIsActivityFinishing) {
            final ArrayList<BluetoothDevice> devices = mDevices;
            if (devices != null && devices.size() == 0) {
                stopForegroundThingyService();
                return;
            }
        }
        createBackgroundNotification();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationPrerequisites();
        startForeground(NOTIFICATION_ID, createForegroundNotification());
        registerReceiver(mNotificationDisconnectReceiver, new IntentFilter(Utils.ACTION_DISCONNECT));
        deviceDB = Objects.requireNonNull(Objects.requireNonNull(MotionApp.Companion.getInstance()).getThingyDeviceDB());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNotificationDisconnectReceiver);
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForegroundThingyService();
    }

    private void stopForegroundThingyService() {
        stopForeground(true);
        stopSelf();
    }

    private void createNotificationPrerequisites() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.checkIfVersionIsOreoOrAbove()) {
            if (mNotificationChannel == null) {
                mNotificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, PRIMARY_CHANNEL, NotificationManager.IMPORTANCE_LOW);
            }
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
    }

    /**
     * Creates a Notifications for the devices that are currently connected.
     */
    private Notification createForegroundNotification() {
        final NotificationCompat.Builder builder = getBackgroundNotificationBuilder();
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        builder.setContentTitle((getString(R.string.alert_tap_to_launch_motion_app)));

        return builder.build();
    }

    /**
     * Creates a Notifications for the devices that are currently connected.
     */
    private void createNotificationForConnectedDevice(final BluetoothDevice device, final String deviceName) {
        final NotificationCompat.Builder builder = getBackgroundNotificationBuilder();
        builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        builder.setDefaults(0).setOngoing(false); // an ongoing notification will not be shown on Android Wear
        builder.setGroup(PRIMARY_GROUP_ID).setGroupSummary(true);
        builder.setContentTitle(getString(R.string.thingy_notification_text, deviceName));

        final Intent disconnect = new Intent(Utils.ACTION_DISCONNECT);
        disconnect.putExtra(Utils.EXTRA_DEVICE, device);
        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, Utils.DISCONNECT_REQ + device.hashCode(), disconnect, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.addAction(new NotificationCompat.Action(R.mipmap.ic_launcher, getString(R.string.thingy_action_disconnect), disconnectAction));
        builder.setSortKey(deviceName + device.getAddress()); // This will keep the same order of notification even after an action was clicked on one of them

        final Notification notification = builder.build();
        mNotificationManager.notify(device.getAddress(), NOTIFICATION_ID, notification);
    }

    /**
     * Returns a notification builder
     */
    private NotificationCompat.Builder getBackgroundNotificationBuilder() {
        final Intent parentIntent = new Intent(this, getNotificationTarget());
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivities(this, Utils.OPEN_ACTIVITY_REQ, new Intent[]{parentIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL);
        builder.setContentIntent(pendingIntent).setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_launcher_notif);
        builder.setChannelId(PRIMARY_CHANNEL_ID);
        return builder;
    }

    /**
     * Returns a notification builder
     */
    private NotificationCompat.Builder getSummaryNotificationBuilder() {
        final Intent parentIntent = new Intent(this, getNotificationTarget()/*MainActivity.class*/);
        parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivities(this, Utils.OPEN_ACTIVITY_REQ, new Intent[]{parentIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Utils.checkIfVersionIsOreoOrAbove()) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL);
            builder.setContentIntent(pendingIntent).setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_launcher_notif);
            if (mNotificationChannel == null) {
                mNotificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, PRIMARY_CHANNEL, NotificationManager.IMPORTANCE_LOW);
                builder.setChannelId(PRIMARY_CHANNEL_ID);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(mNotificationChannel);
            }
            return builder;
        } else {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getPackageName());
            builder.setContentIntent(pendingIntent).setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_launcher_notif);
            return builder;
        }
    }

    /**
     * Creates a summary notification for devices
     */
    private void createSummaryNotification() {
        final NotificationCompat.Builder builder = getBackgroundNotificationBuilder();
        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        builder.setShowWhen(false).setDefaults(0).setOngoing(false); // an ongoing notification will not be shown on Android Wear
        builder.setGroup(Utils.THINGY_GROUP_ID).setGroupSummary(true);

        final ArrayList<ThingyDevice> managedDevices = (ArrayList<ThingyDevice>) deviceDB.thingyDeviceDao().getAllData();
        final ArrayList<BluetoothDevice> connectedDevices = mDevices;
        if (connectedDevices != null && connectedDevices.isEmpty()) {
            // No connected devices
            final int numberOfManagedDevices = managedDevices.size();
            if (numberOfManagedDevices == 1) {
                final String name = managedDevices.get(0).getDeviceName();
                builder.setContentTitle(getString(R.string.one_disconnected, name));
            } else {
                builder.setContentTitle(getString(R.string.two_disconnected, numberOfManagedDevices));
            }
        } else {
            // There are some proximity tags connected
            final int numberOfConnectedDevices = connectedDevices.size();
            if (numberOfConnectedDevices == 1) {
                final String name = getDeviceName(connectedDevices.get(0));
                builder.setContentTitle(getString(R.string.one_connected, name));
            } else {
                builder.setContentTitle(getString(R.string.two_connected, numberOfConnectedDevices));
            }
            builder.setNumber(numberOfConnectedDevices);

            // If there are some disconnected devices, also print them
            final int numberOfDisconnectedDevices = managedDevices.size() - numberOfConnectedDevices;
            if (numberOfDisconnectedDevices == 1) {
                // Find the single disconnected device to get its name
                for (final ThingyDevice thingy : managedDevices) {
                    if (!isConnected(thingy, connectedDevices)) {
                        final String name = thingy.getDeviceName();
                        builder.setContentText(getResources().getQuantityString(R.plurals.thingy_notification_text_nothing_connected, numberOfDisconnectedDevices, name));
                        break;
                    }
                }
            } else if (numberOfConnectedDevices > 1) {
                // If there are more, just write number of them
                builder.setContentText(getResources().getQuantityString(R.plurals.thingy_notification_text_nothing_connected, numberOfDisconnectedDevices, numberOfDisconnectedDevices));
            }
        }

        final Notification notification = builder.build();
        final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        nm.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Checks if the device is among the connected devices list.
     *
     * @param thingy
     *         device to be checked
     * @param connectedDevices
     *         list of connected devices
     */
    private boolean isConnected(ThingyDevice thingy, ArrayList<BluetoothDevice> connectedDevices) {
        for (BluetoothDevice device : connectedDevices) {
            if (thingy.getDeviceAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the name of a device
     */
    private String getDeviceName(final BluetoothDevice device) {
        if (device != null) {
            final String deviceName = device.getName();
            if (!TextUtils.isEmpty(deviceName)) {
                return deviceName;
            }
        }
        return getString(R.string.default_thingy_name);
    }

    /**
     * Creates background notifications for devices
     */
    private void createBackgroundNotification() {
        final ArrayList<BluetoothDevice> devices = mDevices;

        for (int i = 0; i < devices.size(); i++) {
            createNotificationForConnectedDevice(devices.get(i), getDeviceName(devices.get(i)));
        }
    }

    /**
     * Cancels the existing notification. If there is no active notification this method does nothing
     */
    private void cancelNotifications() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);

        final ArrayList<BluetoothDevice> devices = mDevices;
        for (int i = 0; i < devices.size(); i++) {
            nm.cancel(devices.get(i).getAddress(), NOTIFICATION_ID);
        }
    }

    /**
     * Cancels the existing notification for given device. If there is no active notification this method does nothing
     *
     * @param device
     *         of whose notification must be removed
     */
    private void cancelNotification(final BluetoothDevice device) {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(device.getAddress(), NOTIFICATION_ID);
    }

    public class ThingyBinder extends BaseThingyBinder {
        //You can create your own functionality related to the application in side the binder here
        private static final int SCANNING = 1;
        private static final int CONNECTING = 2;
        private boolean mIsScanning;
        private int mState;

        /**
         * Returns the activity state.
         */
        public final boolean getActivityFinishing() {
            return mIsActivityFinishing;
        }

        /**
         * Saves the activity state.
         *
         * @param activityFinishing
         *         if the activity is finishing or not
         */
        public final void setActivityFinishing(final boolean activityFinishing) {
            mIsActivityFinishing = activityFinishing;
        }

        public boolean isScanningState() {
            return mIsScanning;
        }

        public void setScanningState(final boolean isScanning) {
            mIsScanning = isScanning;
        }

        @Override
        public ThingyConnection getThingyConnection(BluetoothDevice device) {
            return mThingyConnections.get(device);
        }
    }
}

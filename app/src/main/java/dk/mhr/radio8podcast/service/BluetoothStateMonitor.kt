package dk.mhr.radio8podcast.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import dk.mhr.radio8podcast.presentation.DEBUG_LOG

class BluetoothStateMonitor(private val appContext: Context): BroadcastReceiver() {
    var isHeadsetConnected = false
    var bluetoothHeadset: BluetoothHeadset? = null

    /** Start monitoring */
    fun start() {
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        Log.i(DEBUG_LOG, "getProfileProxy called!!!")
        bluetoothManager.adapter.getProfileProxy(appContext, object:BluetoothProfile.ServiceListener {
            /** */
            override fun onServiceDisconnected(profile: Int) {
                Log.i(DEBUG_LOG, "onServiceDisconnedted called: $profile")
                isHeadsetConnected = false
                if (profile == BluetoothProfile.HEADSET) {
                    bluetoothHeadset = null
                }
            }

            /** */
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                Log.i(DEBUG_LOG, "onServiceConnected called: $profile")
                isHeadsetConnected = proxy!!.connectedDevices.size > 0

                if (profile == BluetoothProfile.HEADSET) {
                    bluetoothHeadset = proxy as BluetoothHeadset
                }
            }

        }, 1)

        appContext.registerReceiver(this, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    /** Stop monitoring */
    fun stop() {
        Log.i(DEBUG_LOG, "Unregister receiver called")
        appContext.unregisterReceiver(this)
    }

    /** For broadcast receiver */
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(DEBUG_LOG, "onReceive called")
        val connectionState = intent!!.extras!!.getInt(BluetoothAdapter.ACTION_STATE_CHANGED)

        when(connectionState) {
            BluetoothAdapter.STATE_CONNECTED -> isHeadsetConnected = true
            BluetoothAdapter.STATE_DISCONNECTED -> isHeadsetConnected = false
            else -> {}
        }
    }
}
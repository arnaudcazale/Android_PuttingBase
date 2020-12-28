package com.ulab.motionapp.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Created by R.S. on 11/10/18
 */
class DeviceAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val deviceList: List<BluetoothDevice>) :
        ArrayAdapter<BluetoothDevice>(context, layoutResource, deviceList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
        view.text = deviceList[position].name
        return view
    }
}
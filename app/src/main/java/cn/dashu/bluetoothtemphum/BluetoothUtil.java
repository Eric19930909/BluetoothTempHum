package cn.dashu.bluetoothtemphum;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.UUID;

/**
 * Created by Bob on 2016/8/17.10:06
 * Email: 631554401@qq.com
 */

public class BluetoothUtil {

    private BluetoothAdapter mAdapter;

    private boolean isScanning = false;

    private static BluetoothUtil sBluetoothUtil;

    private float mTemperature;
    private float mHumidity;

    private static final UUID BLE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");

    private BluetoothUtil(BluetoothAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public static BluetoothUtil getInstance(BluetoothAdapter mAdapter) {
        if (sBluetoothUtil == null) {
            sBluetoothUtil = new BluetoothUtil(mAdapter);
        }
        return sBluetoothUtil;
    }

    public void findDevices() {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }

        mAdapter.startDiscovery();
    }

    /**
     * @return 如果是执行该方法是仍在搜索，取消搜索并返回true，否则返回false
     */
    public boolean cancelFindDevices() {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
            return true;
        } else {
            return false;
        }
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void scan() {
        mAdapter.startLeScan(new UUID[]{BLE_UUID}, mLeScanCallback);
        isScanning = true;
    }

    public void stopScan() {
        mAdapter.stopLeScan(mLeScanCallback);
        isScanning = false;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            if ((scanRecord == null) || (scanRecord.length <= 0)) {
                return;
            }
//            KLog.e(scanRecord.length);

            String mSeNum = String.valueOf(256 * (256 * (scanRecord[9] & 0xBF) + (scanRecord[10] & 0xFF)) + (scanRecord[11] & 0xFF));

            long l1;
            long l2;
            if (mSeNum.length() >= 7) {
                l1 = getValue(scanRecord, 12);
                l2 = getValue(scanRecord, 15);

                if (scanRecord[7] != 6) {
                    mTemperature = ((float) ((float) l1 / 100.0));
                    mHumidity = ((float) ((float) l2 / 100.0));
                    return;
                }
                mTemperature = ((float) ((float) l1 / 128.0));
                mHumidity = ((float) ((float) l2 / 128.0));
            }

        }
    };

    private long getValue(byte[] mBytes, int param) {
        if ((mBytes[param] & 0x80) == 0) {
            return (mBytes[param] & 0xFF) * 256 + (mBytes[(param + 1)] & 0xFF);
        }
        if ((mBytes[(param + 1)] & 0xFF) == 0) {
        }
        for (long l = ((~mBytes[param] - 1) & 0x7F) * 256; ; l = ((~mBytes[param]) & 0x7F) * 256 + ((~mBytes[(param + 1)] - 1) & 0xFF)) {
            return -l;
        }
    }

    public float getTemperature() {
        return mTemperature;
    }

    public void setTemperature(float temperature) {
        mTemperature = temperature;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public void setHumidity(float humidity) {
        mHumidity = humidity;
    }
}

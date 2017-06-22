package cn.dashu.bluetoothtemphum;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    private TextView tvTemp;
    private TextView tvHum;
    private CheckBox cbOpen;

    private BluetoothUtil mBluetoothUtil;
    private BluetoothAdapter mBluetoothAdapter;

    private InfoThread mThread = new InfoThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();

    }

    private void initView() {

        tvTemp = (TextView) findViewById(R.id.temp);
        tvHum = (TextView) findViewById(R.id.hum);
        cbOpen = (CheckBox) findViewById(R.id.check_box);

    }

    private void initEvent() {

        cbOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //定位权限
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // We don't have permission so prompt the user
                            requestPermissions(
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    0xAAAA);
                        }
                    }

                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(MainActivity.this, "该设备不支持蓝牙功能！", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, 0xBBBB);
                        }
                        mBluetoothUtil = BluetoothUtil.getInstance(mBluetoothAdapter);
                        mBluetoothUtil.scan();
                    }
                } else {
                    if (mBluetoothUtil != null && mBluetoothUtil.isScanning())
                        mBluetoothUtil.stopScan();

                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mThread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //控制蓝牙
        if (requestCode == 0xAAAA) {

            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED)
                    isAllGranted = false;
            }

            if (isAllGranted) {
                mBluetoothUtil = BluetoothUtil.getInstance(mBluetoothAdapter);
                mBluetoothUtil.scan();
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 0xBBBB:
                if (resultCode == RESULT_OK) {
                    mBluetoothUtil = BluetoothUtil.getInstance(mBluetoothAdapter);
                    mBluetoothUtil.scan();
                }
                break;

            default:
                break;

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThread.stopRunning();
    }

    private class InfoThread extends Thread {

        private boolean isRunning = false;

        @Override
        public synchronized void start() {
            isRunning = true;
            super.start();
        }

        @Override
        public void run() {
            super.run();

            while (isRunning) {

                if (mBluetoothUtil != null) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            tvTemp.setText(String.format(Locale.getDefault(), "温度：%.2f℃", mBluetoothUtil.getTemperature()));
                            tvHum.setText(String.format(Locale.getDefault(), "湿度：%.2f％", mBluetoothUtil.getHumidity()));

                        }
                    });

                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

        public void stopRunning() {
            isRunning = false;
        }

    }

}

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.rt_net.rt_bt_9axis_logger.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.lang.String;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.ProgressBar;
import android.speech.RecognizerIntent;

import android.text.format.Time;

import jp.rt_net.rt_bt_9axis_logger.common.logger.Log;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_CODE = 4;
    //private static final int RESULT_OK = 4;

    // センサの状態
    private static final int SEN_STATE_STOP = 1;
    private static final int SEN_STATE_MOVING = 2;
    private static final int SEN_STATE_TURNOVER = 3;

    private int senState =SEN_STATE_STOP;
    private long time_pre= 0;
    private long timeSaveStart = 0;

    // Layout Views
    private TextView mBatteryView;
    private TextView mTempView;
    private TextView mHumiView;
    private TextView mAtmView;

    private TextView mGyroxView;
    private TextView mGyroyView;
    private TextView mGyrozView;

    private TextView mMagxView;
    private TextView mMagyView;
    private TextView mMagzView;

    private ProgressBar mTemp;
    private ProgressBar Humi;
    private ProgressBar Atm;

    private ProgressBar mGyrox;
    private ProgressBar mGyroy;
    private ProgressBar mGyroz;

    private ProgressBar mMagx;
    private ProgressBar mMagy;
    private ProgressBar mMagz;


    private ImageView ivStop;
    private ImageView ivMoving;
    private ImageView ivTurnOver;


    private static Toast t;
    private boolean isVib = false;
    private PeriodicTask pTask;

    private int bluetoothState;

    private TextView   tv_hozonsaki;
    private TextView   tv_savedirname;
    private RadioGroup rg_autoconnect;
    private RadioGroup rg_logging;

    private File file;
    private String dirPath = "";
    private String csvName = "";


    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    //private BluetoothChatService mChatService = null;

    /**
     * Member object for the chat services
     */
    private AI_CHIP mAIChat = null;

    private String connectAddressPre = "";

    /**
     * 周期的に行うタスク(グラフの更新に利用)
     */
    class PeriodicTask extends AbstractPeriodicTask {

        public PeriodicTask(long period, boolean isDaemon) {
            super(period, isDaemon);
        }

        @Override
        protected void invokersMethod() {
            System.out.println(connectAddressPre);

            if(bluetoothState != BluetoothChatService.STATE_CONNECTED  &&
               bluetoothState != BluetoothChatService.STATE_CONNECTING &&
               connectAddressPre != "" &&
               rg_autoconnect.getCheckedRadioButtonId() == (R.id.rb_ac_on)
            ) {
                mAIChat.connect(mBluetoothAdapter.getRemoteDevice(connectAddressPre), false);
                Toast.makeText(getActivity(), "再接続を試みます.",Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        protected void postInvokersMethod(){

        }
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();

            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            
            activity.finish();
        }

        // 接続が切れたときに自動再接続するための別スレッド
        pTask = new PeriodicTask(500 ,true);

        // fragment再生成抑止。Fragment を破棄させないようにする。
        setRetainInstance(true);

    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mAIChat == null) {
            setupChat();
        }

        System.out.println("onStart()");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mAIChat != null) {
            mAIChat.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mAIChat != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mAIChat.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mAIChat.start();
            }
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        System.out.println("onViewCreated()");
        final View view_ = view;


        mBatteryView = (TextView) view.findViewById(R.id.battery_View);

        mTempView = (TextView) view.findViewById(R.id.Temp_txt);
        mHumiView = (TextView) view.findViewById(R.id.Humi_text);
        mAtmView = (TextView) view.findViewById(R.id.Atm_text);

        mTemp = (ProgressBar) view.findViewById(R.id.Temp);
        Humi = (ProgressBar) view.findViewById(R.id.Humi);
        Atm = (ProgressBar) view.findViewById(R.id.Atm);

        /********** toastの定義 ***************/
        t = new Toast(getActivity());

        /********** オートコネクトのON/OFF用RadioGroup ******************/
        rg_autoconnect = (RadioGroup) view.findViewById(R.id.rg_autoconnect);
        //Offをチェック
        rg_autoconnect.check(R.id.rb_ac_off);
         // ラジオグループのチェック状態が変更された時に呼び出されるコールバックリスナーを登録
        rg_autoconnect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            // ラジオグループのチェック状態が変更された時に呼び出される
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) view_.findViewById(checkedId);
                Toast.makeText(getActivity(),
                        "自動再接続機能:" + radioButton.getText(),
                        Toast.LENGTH_SHORT).show();


                if( rg_autoconnect.getCheckedRadioButtonId() == R.id.rb_ac_on){
                    if(pTask!=null) pTask.execute();
                }
                else pTask.cancel();

            }
        });

        /********* ログ取得機能のON/OFF用RadioGroup *********/
        rg_logging = (RadioGroup) view.findViewById(R.id.rg_logging);
        //Offをチェック
        rg_logging.check(R.id.rb_log_off);
        // ラジオグループのチェック状態が変更された時に呼び出されるコールバックリスナーを登録
        rg_logging.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            // ラジオグループのチェック状態が変更された時に呼び出される
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) view_.findViewById(checkedId);

                Toast.makeText(getActivity(),
                        "ログ取得機能:" + radioButton.getText(),
                        Toast.LENGTH_SHORT).show();

                if(rg_logging.getCheckedRadioButtonId() == (R.id.rb_log_on)){
                    dirPath = Environment.getExternalStorageDirectory() + "/BT-9AXIS-LOG";
                    Time time = new Time("Asia/Tokyo");
                    time.setToNow();
                    String date = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日" + time.hour + "時" + time.minute + "分" + time.second + "秒";


                    //BT-9AXIS-LOGのディレクトリがなければ作成する.
                    File outDir01 = new File( Environment.getExternalStorageDirectory(),"/BT-9AXIS-LOG" );
                    if ( outDir01.exists() == false ) {
                        outDir01.mkdir();
                        Toast.makeText(getActivity(), "保存先のディレクトリを作成しました.", Toast.LENGTH_SHORT).show();
                    }

                    String saveFolderName = "";
                    csvName = "/"+ date + ".csv";
                    file = new File(dirPath + csvName);

                    try {

                        FileWriter fw = new FileWriter(file);
                        fw.write("");
                        fw.close();


                    }catch(Exception e){

                    }
                    timeSaveStart = System.currentTimeMillis();
                    String str ="センサ側経過時間 ,android側経過時間, Accx, Accy, Accz, Gyrox, Gyroy, Gyroz, Magx, Magy, Magz, Temp, voltage ";
                    writeFile(str, file);
                    //保存用のフォルダのパスを表示する
                    tv_hozonsaki.setText(dirPath);
                    tv_savedirname.setText(date);
                }

            }
        });



        /********** ファイル保存先表示用テキストビューの定義********/
        tv_hozonsaki = (TextView) view.findViewById(R.id.tv_savepath);
        tv_savedirname =(TextView) view.findViewById(R.id.tv_savedirname);


    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mAIChat = new AI_CHIP(getActivity(), mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:

                    bluetoothState = msg.arg1;

                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_UPDATE_DATA:
                    reflectUpdateData();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case Constants.MESSAGE_LOST_CONNECTION:
                      Toast.makeText(activity, "接続が切れました.",Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        String str = msg.getData().getString(Constants.TOAST);
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();


                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {

                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // Attempt to connect to the device
        mAIChat.connect(device, secure);
        connectAddressPre = address;
        System.out.println("/////////////////////////////////connectDevicee" + address);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

    private void reflectUpdateData(){

        System.out.println("reflectUpdateData()");
        String battery = String.valueOf(mAIChat.get_battery());
        float temp_data = mAIChat.get_Temp();
        float[] acc_data =   mAIChat.get_ACC();
        float[] gyro_data = mAIChat.get_Gyro();
        float[] mag_data = mAIChat.get_Mag();
        int[] acc  = {0,0,0};
        int[] gyro = {0,0,0};
        int[] mag  = {0,0,0};

        double acc_norm = Math.sqrt(acc_data[0] * acc_data[0] + acc_data[1] * acc_data[1] + acc_data[2] * acc_data[2]);

        if(acc_norm >4.0 ) {
            ivTurnOver.setImageResource(R.drawable.turnover_on);
            time_pre = System.currentTimeMillis();


            senState =SEN_STATE_TURNOVER;
        }
        else {
            if(System.currentTimeMillis() - time_pre > 3000) {
                ivTurnOver.setImageResource(R.drawable.turnover_off);
            }
        }
        if(System.currentTimeMillis() - time_pre >3000 && t != null) t.cancel();

        if(Math.abs(1.0 - acc_norm) < 0.15
                && Math.abs(gyro_data[0])  < 5.0
                && Math.abs(gyro_data[1])  < 5.0
                && Math.abs(gyro_data[2])  < 5.0
                ) {
            ivStop.setImageResource(R.drawable.stop_on);
            ivMoving.setImageResource(R.drawable.moving_off);
        }
        else {
            ivStop.setImageResource(R.drawable.stop_off);
            ivMoving.setImageResource(R.drawable.moving_on);
        }

        mBatteryView.setText("Battery Voltage: " + battery + "V");

        mTempView.setText("Temp: " + temp_data + "[T]");
        mHumiView.setText("Humi: " + acc_data[1] + "[G]");
        mAtmView.setText("Atm: " + acc_data[2] + "[G]");

        acc[0]= (int) (100 * (acc_data[0] * 2048 + 32767) / 65536);
        mTemp.setProgress(acc[0]);
        acc[1] = (int) (100 * (acc_data[1] * 2048 + 32767) / 65536);
        Humi.setProgress(acc[1]);
        acc[2] = (int) (100 * (acc_data[2] * 2048 + 32767) / 65536);
        Atm.setProgress(acc[2]);

        gyro[0]= (int) (100 * gyro_data[0] / 2000.0 +50.0);
        mGyrox.setProgress(gyro[0]);
        gyro[1] = (int) (100 * gyro_data[1] / 2000.0+50.0);
        mGyroy.setProgress(gyro[1]);
        gyro[2] = (int) (100 * gyro_data[2] /2000.0+50.0);
        mGyroz.setProgress(gyro[2]);


        mag[0]= (int) (100 * mag_data[0] / 1200.0 +50.0);
        mMagx.setProgress(mag[0]);
        mag[1] = (int) (100 * mag_data[1] / 1200.0+50.0);
        mMagy.setProgress(mag[1]);
        mag[2] = (int) (100 * mag_data[2] / 1200.0+50.0);
        mMagz.setProgress(mag[2]);

        long time_temp = System.currentTimeMillis() -  timeSaveStart;
        String str =  "" + mAIChat.get_Time() + ","
                      +  time_temp   + ","
                      +  acc_data[0] + ","
                      +  acc_data[1] + ","
                      +  acc_data[2] + ","
                      +  gyro_data[0] + ","
                      +  gyro_data[1] + ","
                      +  gyro_data[2] + ","
                      +  mag_data[0]  + ","
                      +  mag_data[1]  + ","
                      +  mag_data[2]  + ","
                      +  mAIChat.get_Temp() + ","
                      +  battery  ;

        if(rg_logging.getCheckedRadioButtonId() == (R.id.rb_log_on)  &&
                file.exists() == true) {


            writeFile(str, file);
        }

    }


    private void writeFile(String str, File file){
        FileOutputStream fos;
        try {
            //ファイルに書き込み
            fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(str);
            bw.newLine();

            //Toast.makeText(context,"kita1", Toast.LENGTH_SHORT).show();
            bw.flush();
            //Toast.makeText(context,"kita2.", Toast.LENGTH_SHORT).show();
            bw.close();
            //Toast.makeText(context, dirPath+"にファイルを保存しました." , Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(),"書き込みに失敗しました.", Toast.LENGTH_SHORT).show();
        }


    }

    

}



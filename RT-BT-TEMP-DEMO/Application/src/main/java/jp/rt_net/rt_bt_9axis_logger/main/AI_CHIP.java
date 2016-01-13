package jp.rt_net.rt_bt_9axis_logger.main;


import java.lang.String;
import java.net.ConnectException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.util.Log;

public class AI_CHIP extends BluetoothChatService{
    // Debugging
    private static final String TAG = "AI_CHIP";

    //Member Val
    public float[] mGyroref = {0,0,0};

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public AI_CHIP(Context context, Handler handler) {
        super(context, handler);
    }

    public int get_TimeStamp(){
        int start_byte = 7;
        int start_bit;
        int timeStamp;

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return 0;
        }

        start_bit = start_byte * 2;
        String[] hexdata = {"0"};
        String data = getNewestData();
        hexdata[0] = data.substring(start_bit,start_bit+2);
        timeStamp = Integer.parseInt(hexdata[0],16);

        return timeStamp;
    }

    public float[] get_ACC(){
        int start_byte = 8;
        int start_bit;
        float[] ACC = {0,0,0};
        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return ACC;
        }

        for(int i = 0; i < 3; i++) {
            if(i != 0) {
                start_byte = start_byte + 2;
            }
            start_bit = start_byte * 2;
            String[] hexdata = {"0","0"};
            String data = getNewestData();
            hexdata[0] = data.substring(start_bit+2,start_bit+4);
            hexdata[1] = data.substring(start_bit,start_bit+2);
            ACC[i] = (float)Integer.parseInt(hexdata[0]+hexdata[1],16);
            if(ACC[i] > 32767){
                ACC[i] -= 65536;
            }
            // System.out.println(ACC);
            ACC[i] = ACC[i] / 2048;

        }
        return ACC;
    }

    public float get_Temp(){
        //int start_byte = 0;
        int start_bit = 4;
        float Temp = 0;

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return Temp;
        }

        String data = getNewestData();
        System.out.println("test");
        System.out.println("data: "+data);
        Temp = Float.valueOf(data);
        return Temp;
    }

    public void set_Gyro(){
        int start_byte = 16;
        int start_bit;
        float[] Gyro = {0,0,0};

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return;
        }

        for(int i = 0; i < 3; i++) {
            if (i != 0) {
                start_byte = start_byte + 2;
            }
            start_bit = start_byte * 2;
            String[] hexdata = {"0","0"};
            String data = getNewestData();
            hexdata[0] = data.substring(start_bit+2,start_bit+4);
            hexdata[1] = data.substring(start_bit,start_bit+2);
           // System.out.println(hexdata[0]+hexdata[1]);
            Gyro[i] = (float)Integer.parseInt(hexdata[0]+hexdata[1],16);
            if(Gyro[i] > 32767){
                Gyro[i] = Gyro[i] - 65536;
            }
            // System.out.println(ACC);
            mGyroref[i] = Gyro[i];
           // System.out.println("mGyroref["+i+"] = " + mGyroref[i]);
        }
    }

    public float[] get_Gyro(){
        int start_byte = 16;
        int start_bit;
        float[] Gyro = {0,0,0};

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return Gyro;
        }

        for(int i = 0; i < 3; i++) {
            if (i != 0) {
                start_byte = start_byte + 2;
            }
            start_bit = start_byte * 2;
            String[] hexdata = {"0", "0"};
            String data = getNewestData();
            hexdata[0] = data.substring(start_bit + 2, start_bit + 4);
            hexdata[1] = data.substring(start_bit, start_bit + 2);
            Gyro[i] = (float) Integer.parseInt(hexdata[0] + hexdata[1], 16);
            if (Gyro[i] > 32767) {
                Gyro[i] -= 65536;
            }
            // System.out.println(ACC);
            //System.out.println("mGyroref[i] = " + (Gyro[i] - mGyroref[i]));
            Gyro[i] = (float) ((Gyro[i] - mGyroref[i]) / 16.4);

        }
        return Gyro;
    }

    public float[] get_Mag(){
        int start_byte = 22;
        int start_bit;
        float[] Mag = {0,0,0};

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return Mag;
        }

        for(int i = 0; i < 3; i++) {
            if(i != 0) {
                start_byte = start_byte + 2;
            }
            start_bit = start_byte * 2;
            String[] hexdata = {"0","0"};
            String data = getNewestData();
            hexdata[0] = data.substring(start_bit+2,start_bit+4);
            hexdata[1] = data.substring(start_bit,start_bit+2);
            Mag[i] = (float)Integer.parseInt(hexdata[0]+hexdata[1],16);
            if(Mag[i] > 32767){
                Mag[i] -= 65536;
            }
             //System.out.println(Mag);
            Mag[i] = (float) (0.3 * Mag[i]);
        }
        return Mag;
    }

    public float get_battery() {
        int start_byte = 28;
        int start_bit = start_byte * 2;
        float battery = 0;

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return battery;
        }

        String[] hexdata = {"0", "0"};
        String data = getNewestData();
        hexdata[0] = data.substring(start_bit + 2, start_bit + 4);
        hexdata[1] = data.substring(start_bit, start_bit + 2);
        battery = (float) Integer.parseInt(hexdata[0] + hexdata[1], 16);

        //System.out.println(battery);
        battery = battery / 13107;
        return battery;
    }


    public int get_Time() {
        int start_byte = 30;
        int start_bit = start_byte * 2;
        int Time = 0;

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return Time;
        }

        String[] hexdata = {"0", "0", "0", "0"};
        String data = getNewestData();
        hexdata[0] = data.substring(start_bit + 6, start_bit + 8);
        hexdata[1] = data.substring(start_bit + 4, start_bit + 6);
        hexdata[2] = data.substring(start_bit + 2, start_bit + 4);
        hexdata[3] = data.substring(start_bit, start_bit + 2);
        Time = Integer.parseInt(hexdata[0] + hexdata[1] + hexdata[2] + hexdata[3], 16);

        return Time;
    }


    @Override
    public String getNewestData(){

        if (super.getState() != BluetoothChatService.STATE_CONNECTED) {
            return "";
        }
        return super.getNewestData() ;
    }

}

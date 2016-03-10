package edu.asu.cse535assgn1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.sql.Timestamp;

public class SensorHandlerClass extends Service implements SensorEventListener {
    SQLiteDatabase db;
    private SensorManager accelManage;
    private Sensor senseAccel;
    private String tableName;
    float accelValuesX[] = new float[128];
    float accelValuesY[] = new float[128];
    float accelValuesZ[] = new float[128];
    int index = 0;
    int k=0;
    Bundle b;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ[index] = sensorEvent.values[2];
            insertData(accelValuesX[index], accelValuesY[index], accelValuesZ[index]);

            if(index >= 127){
                index = 0;
                accelManage.unregisterListener(this);
                accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        b = intent.getExtras();
        tableName = b.getString("tableName");
        return START_STICKY;
    }

    public void insertData(float x, float y, float z){
        try {

            String path = Environment.getExternalStorageDirectory() + "/databaseFolder/myDB";
            db = SQLiteDatabase.openOrCreateDatabase(path, null);
            db.beginTransaction();
            try {
                java.util.Date date= new java.util.Date();
                db.execSQL( "insert into "+tableName+" (Timestamp, X, Y, Z) values ('"+new Timestamp(date.getTime())+"','"+x+"', '"+y+"', '"+z+"' );" );

                db.setTransactionSuccessful(); //commit your changes
            } catch (SQLiteException e) {
                //report problem
            } finally {
                db.endTransaction();
            }
        }
        catch (SQLException e){

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onCreate(){
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);

    }
}

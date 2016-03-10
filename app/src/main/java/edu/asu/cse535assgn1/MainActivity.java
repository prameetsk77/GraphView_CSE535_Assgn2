package edu.asu.cse535assgn1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

//import edu.asu.cse535assgn1.lib.GraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import edu.asu.cse535assgn1.models.Accelerometer;
import edu.asu.cse535assgn1.models.Patient;

/**
 * @author Jithin Roy
 */
public class MainActivity extends AppCompatActivity {

    private GraphView mGraphView;
    GridLabelRenderer graphProperties;
    private boolean mIsAnimating = false;
    SQLiteDatabase db;
    private String tableName;

    private TextView mPatientIDTextView;
    private TextView mPatientNameTextView;
    private TextView mPatientAgeTextView;
    private RadioGroup mSexRadioGroup;

    private Patient mPatient;

    private int mIndex = 0;
    private int mGraphIndex = 0;
    private Handler mHandler = new Handler();
    private List<Float> mCurrentGraphValues = new ArrayList<>() ;
    private List<Float> mCurrentGraphValues2 = new ArrayList<>() ;
    private List<Accelerometer> accelerometerList = new ArrayList<Accelerometer>() ;


    private float[] values1 = {10, 50, 60, 80, 60, 48, 00, 06, 20, 04, 02, 00, 06, 07, 8, 9, 00, 04, 02, 30, 05, 64, 89, 8, 00, 06, 05, 8, 00, 45, 00, 06, 04, 54, 03,};
    private float[] values2 = {3, 4, 5, 6, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,};


    LineGraphSeries<DataPoint> mSeries1;
    LineGraphSeries<DataPoint> mSeries2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        float[] values = {0};
        String[] labels = {"10","15","20","25","30","35","40"};
        String[] labels1 = {"1","2","3","4","5"};

        //this.mGraphView = new GraphView(this,values,"Graph",labels,labels1, GraphView.LINE);
        this.mGraphView = new GraphView(this);

        ViewGroup layout = (ViewGroup) findViewById(R.id.baseLayout);
        this.mGraphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(this.mGraphView);

        this.mPatientIDTextView = (TextView)findViewById(R.id.patientId);
        this.mPatientNameTextView = (TextView)findViewById(R.id.patientName);
        this.mPatientAgeTextView = (TextView)findViewById(R.id.patientAge);
        this.mSexRadioGroup = (RadioGroup)findViewById(R.id.radio_group);

        mSeries1 = new LineGraphSeries<DataPoint>();
        mSeries1.setColor(Color.BLUE);
        mGraphView.addSeries(mSeries1);
        mSeries2 = new LineGraphSeries<DataPoint>();
        mSeries1.setColor(Color.WHITE);
        mGraphView.addSeries(mSeries2);

        graphProperties = new GridLabelRenderer(mGraphView);
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(10);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save the current state of animation so that we can
        // restart on orientation change.
        savedInstanceState.putBoolean("IsAnimating", this.mIsAnimating);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Check if the graph was already animating and call the animation function.
        this.mIsAnimating = savedInstanceState.getBoolean("IsAnimating");
        if (this.mIsAnimating) {
            startAnimation();
        }
    }

    public String createDatabase(String id, String name, int age, String patientSex){
        String tblName=id+"_"+name+"_"+age+"_"+patientSex;
        try {
            //File db_file = new File(Environment.getExternalStorageDirectory(), "databaseFolder/myDB");
            String path = Environment.getExternalStorageDirectory() + File.separator + "databaseFolder" + File.separator + "myDB";
            db = SQLiteDatabase.openOrCreateDatabase(path, null);
            db.beginTransaction();
            try {
                //perform your database operations here ...
                db.execSQL("create table " + tblName + "("
                        + " Timestamp text , "
                        + " X float, "
                        + " Y float, "
                        + " Z float ); ");

                db.setTransactionSuccessful(); //commit your changes
            } catch (SQLiteException e) {
                //report problem
            } finally {
                db.endTransaction();
            }
        }
        catch (SQLException e){

            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return tblName;
    }

    public void selectData(String tableName){
        try {
            String path = Environment.getExternalStorageDirectory() + File.separator + "databaseFolder" + File.separator + "myDB";
            db = SQLiteDatabase.openOrCreateDatabase(path, null);
            db.beginTransaction();
            try {
                Cursor c = db.rawQuery("select X,Y,Z from " + tableName + " order by Timestamp LIMIT 10 ;", null);
                if (c != null ) {
                    if  (c.moveToFirst()) {
                        do {
                            Accelerometer accelerometer=new Accelerometer();
                            accelerometer.setX(c.getFloat(c.getColumnIndex("X")));
                            accelerometer.setY(c.getFloat(c.getColumnIndex("Y")));
                            accelerometer.setZ(c.getFloat(c.getColumnIndex("Z")));
                            accelerometerList.add(accelerometer);
                        }while (c.moveToNext());
                    }
                }
                c.close();
                Log.i("test ","Acc value 1"+accelerometerList.get(7).getX());
                db.setTransactionSuccessful(); //commit your changes
            } catch (SQLiteException e) {
                //report problem
            } finally {
                db.endTransaction();
            }
        }
        catch (SQLException e){

            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void startButtonClicked(View view) {
        if (mIsAnimating == false) {
            String patientSex=null;
            String id = this.mPatientIDTextView.getText().toString();
            String name = this.mPatientNameTextView.getText().toString();
            String ageString = this.mPatientAgeTextView.getText().toString();
            int age = 0;
            if (ageString.equals("") == false && ageString.length()<=3) {
               // System.out.println("before "+ageString);
                age = Integer.parseInt(ageString);
               // System.out.println("after "+ageString);
            }
            boolean isMale = true;
            if (this.mSexRadioGroup.getCheckedRadioButtonId() == R.id.radio_female) {
                   isMale = false;
            }
            if (isMale)
                patientSex="Male";
            else
                patientSex="Female";
            tableName=createDatabase(id, name, age, patientSex);

            //Intent startSenseService = new Intent(MainActivity.this, SensorHandlerClass.class);
            Bundle b = new Bundle();
            b.putString("tableName", tableName);
            //startSenseService.putExtras(b);
            //startService(startSenseService);

            this.mPatient = new Patient(id,name,age,isMale);

            startAnimation();
            showMessage();
        }

    }
    public void runButtonClicked(View view){
        selectData(tableName);
    }

    public void stopButtonClicked(View view) {
        this.mIsAnimating = false;
        mIndex = 0;
        mGraphIndex = 0;
        mSeries1.resetData(new DataPoint[]{new DataPoint(0,0)});
        mSeries2.resetData(new DataPoint[]{new DataPoint(0,0)});
        //this.mGraphView.invalidate();
    }
    /**
     * @author Prameet Kohli
     */
    private void showMessage() {
        String message = null;

        if (this.mPatient.getId() != null && this.mPatient.getId().equals("") == false) {
            message = " Patient Id: " + this.mPatient.getId();

            if (this.mPatient.getName() != null && this.mPatient.getName().equals("") == false) {
                message += " Name: " + this.mPatient.getName();
            }

            if (this.mPatient.getAge() > 0 ) {
                message += " Age: " + this.mPatient.getAge();
            }

            if (this.mPatient.isMale()) {
                message += " (Male)";
            } else {
                message += " (Female)";
            }
        }
        if (message != null) {
            Toast.makeText(this,message, Toast.LENGTH_LONG).show();
        }
    }

    private Runnable mUpdateGraph = new Runnable() {
        public void run() {
            update();
            if (mIsAnimating) {
                mHandler.postDelayed(mUpdateGraph, 120);
            }
        }
    };

    private void startAnimation() {
        this.mIsAnimating = true;
        this.mHandler.postDelayed(mUpdateGraph, 10);
    }

    private void update() {

        if (this.mIsAnimating == true) {

           if (this.mIndex == this.values1.length) {
               this.mIndex = 0;
           }
            mSeries1.appendData(new DataPoint(mGraphIndex, this.values1[this.mIndex]), true, 10);
            mSeries2.appendData(new DataPoint(mGraphIndex, this.values2[this.mIndex]), true, 10);

            mGraphIndex++;
            this.mIndex ++;
        }
    }

}

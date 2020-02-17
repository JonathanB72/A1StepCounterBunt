package com.example.a1stepcounterbunt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor sensors;

    private TextView XYZText, ShakinText, CountText, thsdOutput;
    private EditText thsdInput;
    private Button StartButton, StopButton;

    private long CurrentTime;
    private long LastTime;
    private long ShakinTime;

    private float XValue, YValue, ZValue, SumValue = 0;
    //Fifty samples of data
    private float[] XSlidingWindow = new float[50]; //SW
    private float[] YSlidingWindow = new float[50];
    private float[] ZSlidingWindow = new float[50];
    private float[] SumSlidingWindow = new float[50];
    private boolean Shakin = false;
    private float thsdNum = 0; //THSD!
    private int Count = 0;

    private String XYZDummyText = "{0, 0, 0}";
    private String thsdDummyText = "thsd: 0"; //THSD!

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeAllTheStuff();
        // Accelerometer data
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        //update thsd on editText
        StartButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        onResume();
                        thsdNum = Float.parseFloat(thsdInput.getText().toString());
                        Count = 0;
                        resetWindows();
                    }
                }
        );

        StopButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view){
                        onPause();
                    }
                }
        );

        if (mAccelerometer != null) {
            //heck yeah our accelerometer is working
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        else{
            //fail
        }
    }

    public void initializeAllTheStuff(){
        //This tells us where to find the text boxes we need to update
        XYZText = (TextView) findViewById(R.id.XYZText);
        thsdOutput = (TextView) findViewById(R.id.thsdOutput);
        ShakinText = (TextView) findViewById(R.id.ShakinText);
        CountText = (TextView) findViewById(R.id.CountText);
        //Find where to find the thsd text
        thsdInput = (EditText) findViewById(R.id.thsdInput);
        //Buttons yay
        StartButton = (Button) findViewById(R.id.StartButton);
        StopButton = (Button)findViewById(R.id.StopButton);

    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }
    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        XValue = event.values[0];
        YValue = event.values[1];
        ZValue = event.values[2];
        SumValue = (XValue+YValue+ZValue);
        CurrentTime = System.currentTimeMillis();
        //10 milliseconds
        if(CurrentTime - LastTime > 10){
            addToWindow(); //SW
            LastTime = CurrentTime;
        }

        isShaking();
        //if stopped shaking for more than 3 seconds, Shakin = false
        if(CurrentTime - ShakinTime > 3000)
            Shakin = false;
        updateText();
    }

    public void addToWindow(){ //SW
        for (int i = 1; i < XSlidingWindow.length; i++){
            XSlidingWindow[i-1] = XSlidingWindow[i];
            YSlidingWindow[i-1]= YSlidingWindow[i];
            ZSlidingWindow[i-1] = ZSlidingWindow[i];
            SumSlidingWindow[i-1] = SumSlidingWindow[i];
        }
        XSlidingWindow[XSlidingWindow.length-1]=Math.round(XValue);
        YSlidingWindow[YSlidingWindow.length-1]=Math.round(YValue);
        ZSlidingWindow[ZSlidingWindow.length-1]=Math.round(ZValue);
        SumSlidingWindow[SumSlidingWindow.length-1]= SumValue;
    }

    public void resetWindows(){
        // reset all sliding windows whenever you hit the start button
        XSlidingWindow = new float[50]; //SW
        YSlidingWindow = new float[50];
        ZSlidingWindow = new float[50];
        SumSlidingWindow = new float[50];
    }

    public void isShaking(){
        //check to see if the phone is shaking
        //Thsd Crossing
        for(int i = 1; i < XSlidingWindow.length; i++){
            //if the last number is less than the thsd and the next one is greater than or equal
            if((XSlidingWindow[i-1] > thsdNum) && ((XSlidingWindow[i] < thsdNum) || (XSlidingWindow[i] == thsdNum))){
                Count++;
                //reset the sliding window as soon as this is true
                resetWindows();
                Shakin = true;
                ShakinTime = CurrentTime;
            }
        }
    }

    public void updateText(){

        XYZDummyText = "{" + Math.round(XValue*10.0)/10.0 + ", " + Math.round(YValue*10.0)/10.0 + ", " + Math.round(ZValue*10.0)/10.0 + "}";
        thsdDummyText = "thsd: " + thsdNum;

        XYZText.setText(XYZDummyText);
        thsdOutput.setText(thsdDummyText);
        ShakinText.setText(Boolean.toString(Shakin));
        CountText.setText(Integer.toString(Count));
    }



}

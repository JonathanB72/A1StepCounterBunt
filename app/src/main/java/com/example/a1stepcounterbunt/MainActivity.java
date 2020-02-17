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
    private Sensor mProximity;

    private TextView XYZText, ShakinText, CountText, thsdOutput;
    private EditText thsdInput;
    private Button StartButton, StopButton, ProxButton;

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
    private boolean ProximityFlag = false;
    private float thsdNum = 0; //THSD!
    private float distance = 0; //PROX!
    private int Count = 0;

    private String XYZDummyText = "{0, 0, 0}";
    private String thsdDummyText = "thsd: 0"; //THSD!
    private String shakeDummyText = "not shaking";
    private String ProximityDummyText = "Turn Prox Feature On";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeAllTheStuff();
        // Accelerometer data
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (mAccelerometer != null) {
            //heck yeah our accelerometer is working
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        else{
            //fail
        }

        //BUTTONS
        StartButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        onResume();
                        try {
                            thsdNum = Float.parseFloat(thsdInput.getText().toString());
                        }catch (Exception e) {
                            thsdNum = 0;
                            thsdInput.setText("0.0");
                        }
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

        ProxButton.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View view){
                        ProximityFlag = !ProximityFlag;
                    }
                }
        );


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
        ProxButton = (Button)findViewById(R.id.ProxButton);

    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mProximity, SensorManager.SENSOR_DELAY_GAME);
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
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            XValue = event.values[0];
            YValue = event.values[1];
            ZValue = event.values[2];
            SumValue = (XValue + YValue + ZValue);
            CurrentTime = System.currentTimeMillis();
            //10 milliseconds
            if (CurrentTime - LastTime > 10) {
                addToWindow(); //SW
                LastTime = CurrentTime;
            }

            isShaking();
            //if stopped shaking for more than 2 seconds, Shakin = false just for visibility after shaking
            if (CurrentTime - ShakinTime > 2000)
                Shakin = false;
            updateText();
        }
        //PROXIMITY SENSOR EXTRA CREDIT
        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            if(ProximityFlag){
                //This can either be 0 if close or 8 if far
                distance = event.values[0];
                if(distance == 0){ // hide buttons if in pocket
                    StartButton.setVisibility(View.GONE);
                    StopButton.setVisibility(View.GONE);
                }else{
                    StartButton.setVisibility(View.VISIBLE);
                    StopButton.setVisibility(View.VISIBLE);
                }
            }else{
                StartButton.setVisibility(View.VISIBLE);
                StopButton.setVisibility(View.VISIBLE);
            }
        }
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
        SumSlidingWindow[SumSlidingWindow.length-1]= Math.round(SumValue);
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

        //if z crossing 0 first and foremost, easiest step detection method I found

        if((ZSlidingWindow[0] < 0 && ZSlidingWindow[49] >0)||(ZSlidingWindow[0] > 0 && ZSlidingWindow[49] < 0)){
            Count++;
            resetWindows();
            Shakin = true;
            ShakinTime = CurrentTime;
        } else {
        //else check the sliding window and if the last number is less than the thsd
        // and the next one is greater than it then detect a shake/step

            for (int i = 1; i < XSlidingWindow.length; i++) {
                if ((SumSlidingWindow[i - 1] > thsdNum) && (SumSlidingWindow[i] < thsdNum)) {
                    Count++;
                    //reset the sliding window as soon as soon as detected
                    resetWindows();
                    Shakin = true;
                    ShakinTime = CurrentTime;
                }
            }
        }
    }


    public void updateText(){

        XYZDummyText = "{" + Math.round(XValue*10.0)/10.0 + ", " + Math.round(YValue*10.0)/10.0 + ", " + Math.round(ZValue*10.0)/10.0 + "}";
        thsdDummyText = "thsd: " + thsdNum;

        if(Shakin) {
            shakeDummyText = "Shaking";
        }else {
            shakeDummyText = "Not Shaking";
        }

        if(ProximityFlag) {
            ProximityDummyText = "Turn Prox Feature Off";
        }else {
            ProximityDummyText = "Turn Prox Feature On";
        }

        XYZText.setText(XYZDummyText);
        thsdOutput.setText(thsdDummyText);
        ShakinText.setText(shakeDummyText);
        CountText.setText(Integer.toString(Count));
        ProxButton.setText(ProximityDummyText);
    }



}

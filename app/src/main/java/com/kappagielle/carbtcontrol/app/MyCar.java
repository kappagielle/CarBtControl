package com.kappagielle.carbtcontrol.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.content.Context;
import android.widget.Button;
import android.widget.ToggleButton;


public class MyCar extends ActionBarActivity implements SensorEventListener {

    private Bluetooth bl = null;
    private SensorManager manager;
    private Sensor mAccel;

    //inserisco un commento




    private boolean faroAnteriore = false;
    private boolean faroPosteriore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_car);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ToggleButton accensione = (ToggleButton)findViewById(R.id.onOffBtn);
        Button luciAnteriori = (Button)findViewById(R.id.luciAnterioriBtn);
        Button luciPosteriori = (Button)findViewById(R.id.luciPosterioriBtn);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_car, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;

        Integer y = (int)values[SensorManager.AXIS_Y - 1];

        //qui invio i dati via bluetouth:
        //prima arrotondo i parametri

        if(bl != null) {

            if(y > 2) {
                bl.sendData("1"); //cmd destra
            }
            if(y < -2) { //cmd sinistra
                bl.sendData("2");
            }
            if(y >= -2 && y <= 2 ) { //cmd dritto
                bl.sendData("0");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

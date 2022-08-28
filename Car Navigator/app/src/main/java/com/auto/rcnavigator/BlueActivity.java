package com.auto.rcnavigator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static java.lang.Math.PI;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.atan2;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;
import static java.lang.StrictMath.sqrt;

public class BlueActivity extends AppCompatActivity implements SensorEventListener {
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int REQUEST_ENABLE_BT = 999;
    ArrayAdapter<String> arrayAdapter;
    BluetoothAdapter bluetoothAdapter;
    private SensorManager sensorManager;
    private final float[] magnetometerReading = new float[3];
    private OutputStream outputStream;
    private BluetoothSocket btSocket = null;
    private BluetoothDevice hc05;

    private Double dlongitude;
    private Double dlatitude;
    private Double wlongitude;
    private Double wlatitude;

    private DatabaseReference databaseReferenceDest;
    private DatabaseReference databaseReferenceWay;
    private Double clatitude;
    private Double clongitude;

    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue);
        Mapbox.getInstance(this, getString(R.string.access_token));
        locationComponent = mapboxMap.getLocationComponent();
        databaseReferenceDest = FirebaseDatabase.getInstance().getReference("Destination Location");
        databaseReferenceWay = FirebaseDatabase.getInstance().getReference("Waypoints");
        databaseReferenceDest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String databaseLatitudeString = dataSnapshot.child("Latitude").getValue().toString().substring(1, dataSnapshot.child("Latitude").getValue().toString().length() - 1);
                    String databaseLongitudeString = dataSnapshot.child("Longitude").getValue().toString().substring(1, dataSnapshot.child("Longitude").getValue().toString().length() - 1);

                    String[] stringlat = databaseLatitudeString.split(",");
                    Arrays.sort(stringlat);
                    String[] stringlong = databaseLongitudeString.split(",");
                    Arrays.sort(stringlong);

                    dlatitude = Double.parseDouble(stringlat[stringlat.length - 1].split("=")[1]);
                    dlongitude = Double.parseDouble(stringlong[stringlong.length - 1].split("=")[1]);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
        databaseReferenceWay.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String databaseLatitudeString = dataSnapshot.child("Latitude").getValue().toString().substring(1, dataSnapshot.child("Latitude").getValue().toString().length() - 1);
                    String databaseLongitudeString = dataSnapshot.child("Longitude").getValue().toString().substring(1, dataSnapshot.child("Longitude").getValue().toString().length() - 1);

                    String[] stringlat = databaseLatitudeString.split(",");
                    Arrays.sort(stringlat);
                    String[] stringlong = databaseLongitudeString.split(",");
                    Arrays.sort(stringlong);

                    wlatitude = Double.parseDouble(stringlat[stringlat.length - 1].split("=")[1]);
                    wlongitude = Double.parseDouble(stringlong[stringlong.length - 1].split("=")[1]);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor compass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(adaptorCheck())
            super.onBackPressed();
        bluetoothReq();
        //Toast.makeText( MainActivity.this,(hc05.getName()), Toast.LENGTH_SHORT).show();
        blueConnect();
    }

    private void blueDisconnect(){
        try {
            btSocket.close();
            System.out.println(btSocket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void blueConnect() {
        hc05 = bluetoothAdapter.getRemoteDevice("FC:A8:9A:00:49:B7");

        do {
            try {
                btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                Toast.makeText( BlueActivity.this,btSocket.toString() , Toast.LENGTH_SHORT).show();
                btSocket.connect();
                //System.out.println(btSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } while (!btSocket.isConnected());
        Snackbar.make(findViewById(R.id.layout),hc05.getName()+" , BondState:"+hc05.getBondState(), Snackbar.LENGTH_SHORT)
                .show();

        try{
            outputStream = btSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void bluetoothReq() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private boolean adaptorCheck() {
        if (!(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)){
            // Failure! No magnetometer.
            return false;
        }
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            //Toast.makeText(this, "Device Doesn't Support Bluetooth!\nExiting. . .", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        if (requestcode == REQUEST_ENABLE_BT) {
            if (resultcode == RESULT_OK)
                Toast.makeText(getApplicationContext(), "Bluetooth Enabled!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        assert locationComponent.getLastKnownLocation() != null;
        clatitude = locationComponent.getLastKnownLocation().getLatitude();
        clongitude = locationComponent.getLastKnownLocation().getLongitude();

        if(wlatitude!=null&&wlongitude!=null) {
            float value0 = magnetometerReading[0];  // 1st sensor value
            float value1 = magnetometerReading[1];  // 2rd sensor value if exists

            Double clat = clatitude;  // 1st sensor value
            Double clon = clongitude;  // 2rd sensor value if exists


            double heading = atan2(value0, value1);

            if (heading < 0)
                heading += 2 * PI;
            int current_heading = (int)(heading * 180 / PI);                 // assign compass calculation to variable (compass_heading) and convert to integer to remove decimal places

            float R = (float) 6371e3; // metres
            float q1 = (float) (clat*PI/180);
            float q2 = (float) (wlatitude*PI/180);
            float w = (float) ((wlatitude-clat)*PI/180);
            float y = (float) ((wlongitude-clon)*PI/180);

            float a = (float) (sin(w/2) * sin(w/2) + cos(q1) * cos(q2) * sin(y/2) * sin(y/2));
            float c = (float) (2 * atan2(sqrt(a), sqrt(1-a)));

            float dis = R * c;
            float X= (float) (cos(wlatitude)*sin(wlongitude-clon));
            float Y= (float) (cos(clat)*sin(wlatitude)-sin(clat)*cos(wlatitude)*cos(wlongitude-clon));



            float bearing= (float) atan2(X,Y);
            bearing = (float) (bearing*180/PI);
            if(bearing<0)
                bearing = 360+bearing;
            else if(bearing>0)
                bearing =360-bearing;

            bearing =360-bearing;

            float dest_heading = bearing;
            current_heading-=dest_heading;
            if(current_heading<0)
                current_heading+=360;
            // int steering_angle=map(abs(dest_heading-current_heading),0,359,0,25);
            dest_heading=0;
            if (abs(dest_heading - current_heading) > 160)
            {
                //Serial.println("Left");
                try{
                    outputStream.write(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else if (abs(dest_heading - current_heading) < 160 && abs(dest_heading - current_heading) > 20)
            {
                //Serial.println("Right");
                try{
                    outputStream.write(2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (abs(dest_heading - current_heading) <= 20)
            { //Serial.println("Stop");
                try{
                    outputStream.write(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (dis<=2) {
                databaseReferenceWay.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            dataSnapshot.child("Latitude").getRef().removeValue();
                            String databaseLatitudeString = dataSnapshot.child("Latitude").getValue().toString().substring(1, dataSnapshot.child("Latitude").getValue().toString().length() - 1);
                            String databaseLongitudeString = dataSnapshot.child("Longitude").getValue().toString().substring(1, dataSnapshot.child("Longitude").getValue().toString().length() - 1);

                            String[] stringlat = databaseLatitudeString.split(",");
                            Arrays.sort(stringlat);
                            String[] stringlong = databaseLongitudeString.split(",");
                            Arrays.sort(stringlong);

                            wlatitude = Double.parseDouble(stringlat[stringlat.length - 1].split("=")[1]);
                            wlongitude = Double.parseDouble(stringlong[stringlong.length - 1].split("=")[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        }
        else {
            wlatitude = dlatitude;
            wlongitude = dlongitude;
        }
        float R = (float) 6371e3; // metres
        float q1 = (float) (clatitude*PI/180);
        float q2 = (float) (wlatitude*PI/180);
        float w = (float) ((wlatitude-clatitude)*PI/180);
        float y = (float) ((wlongitude-clongitude)*PI/180);

        float a = (float) (sin(w/2) * sin(w/2) + cos(q1) * cos(q2) * sin(y/2) * sin(y/2));
        float c = (float) (2 * atan2(sqrt(a), sqrt(1-a)));

        float dis = R * c;


        if(dis<2) {
            try {
                outputStream.write(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            blueDisconnect();
            finish();
        }

        Toast.makeText(BlueActivity.this,(Arrays.toString(magnetometerReading)+" "+wlatitude+","+wlongitude),Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SENSOR_STATUS_ACCURACY_LOW)
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                }
            },500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
    }
}
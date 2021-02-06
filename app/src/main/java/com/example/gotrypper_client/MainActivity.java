package com.example.gotrypper_client;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class
MainActivity extends AppCompatActivity {

    private StompClient  stompClient;
    private EditText name,lat,longitude;
    private Button connect,disconnect,send;
    private TextView serverResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.name_box);
        lat = findViewById(R.id.latitude_box);
        longitude = findViewById(R.id.longitude_box);
        connect = findViewById(R.id.connect);
        disconnect = findViewById(R.id.disconnect);
        send = findViewById(R.id.send_button);
        serverResult = findViewById(R.id.result_server);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWebSocket();
            }
        });

        //Send Button
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(name.getText().toString(),lat.getText().toString(),longitude.getText().toString());
            }
        });





    }

    private void sendData(String name, String lat, String longitude) {
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("userName",name);
            JSONObject latlong = new JSONObject();
            latlong.put("latitude",lat);
            latlong.put("longitude",longitude);
            jsonObject.put("content",latlong);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        stompClient.send("/app/message",jsonObject.toString()).subscribe();

    }


    public void connectWebSocket(){
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,"http://13.234.112.78:9999/endpoints/websocket");
        stompClient.connect();
        StompUtils.lifecycle(stompClient);
        getMessage();
    }

    @SuppressLint("CheckResult")
    private void getMessage(){
        stompClient.topic("/topic/allgpsdata").subscribe(serverMessage ->{
            Log.d("MainActivtiy", serverMessage.getPayload());
            JSONObject jsonObject = new JSONObject(serverMessage.getPayload());
            runOnUiThread(()->{
                try {
                    serverResult.append(jsonObject.getString("userName")+"\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stompClient.disconnect();
    }
}

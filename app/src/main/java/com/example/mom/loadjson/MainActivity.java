package com.example.mom.loadjson;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_URL = "http://data.taipei/opendata/datalist/apiAccess?" +
            "scope=resourceAquire&rid=bf073841-c734-49bf-a97f-3757a6013812";
    private ParkAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new ParkAdapter(this);
        boolean networkConnect = isNetworkAvailable(this);
        if(networkConnect){
            new LoadTask().execute(FILE_URL);  //使用AsyncTask方法
        }
        else{
            new AlertDialog.Builder(this)
                    .setTitle("無連結網路")
                    .setMessage("直接進入紀錄頁面")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this , HistroyActivity.class));
                        }})
                    .show();
        }

        //浮動按鈕 動作事件
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.History);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(
                        new Intent( MainActivity.this, HistroyActivity.class));
            }
        });
    }

    class LoadTask extends AsyncTask < String , Void ,String >{
        @Override
        protected String doInBackground(String... params) {
            StringBuffer sb = new StringBuffer();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream()));
                String line = "";
                while ((line=in.readLine()) != null){
                    //Log.d("HTTP" , line);
                    sb.append(line+"\n");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
            return sb.toString();
        }
        //在 doInBackground() 後自動執行
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayList<ParkInfo> detailData = ParkInfo.fromJsonString(s);
            //Log.d("DetailData" , ""+detailData.get(0).getName());
            setupRecyclerView( detailData);

        }
    }

    private void setupRecyclerView( ArrayList<ParkInfo> list){
        recyclerView = findViewById(R.id.recyclerView);  //取得畫面中的RecyclerView元件
        adapter.setParks( list);
        recyclerView.setAdapter(adapter);                           //設定Adapter
        recyclerView.setLayoutManager( new LinearLayoutManager(this)); //設定RecyclerView自己的LayoutManager物件
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        Log.d("CONNECT" , String.valueOf(info.isConnected()));
        Log.d("AVAILABLE" , String.valueOf(info.isAvailable()));
        if(info!=null && info.isConnected() && info.isAvailable()){
            return true;}
        return  false;
    }
}

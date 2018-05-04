package com.example.mom.loadjson;

import android.content.ContentValues;
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
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements ParkAdapter.OnItemClickHandler{

    private static final String FILE_URL = "http://data.taipei/opendata/datalist/apiAccess?" +
            "scope=resourceAquire&rid=bf073841-c734-49bf-a97f-3757a6013812";
    private ParkAdapter adapter;
    private MyDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = MyDBHelper.getInstance(this);   //取得DB
        adapter = new ParkAdapter(this , this);  //初始設定好adapter
        boolean networkConnect = isNetworkAvailable(this);  //確認網路是否連線
        if(networkConnect){
            new LoadTask().execute(FILE_URL);  //有連線，使用AsyncTask方法：下載String , Parse Json , Set RecyclerView
        }
        else{   //沒有連線 , AlertDialog OK intent to HistroyActivity
            new AlertDialog.Builder(this)
                    .setTitle("無連結網路")
                    .setMessage("直接進入紀錄頁面")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this , HistroyActivity.class)); //跳HistoryActivity
                        }})
                    .show();
        }
        //浮動按鈕 動作事件
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.History);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(
                        new Intent( MainActivity.this, HistroyActivity.class)); //跳HistoryActivity
            }
        });
    }
    //LoadTask 下載String [ 輸入String URL , 輸出String 讀到的string buffer]
    class LoadTask extends AsyncTask < String , Void ,String >{
        @Override
        protected String doInBackground(String... params) {
            StringBuffer sb = new StringBuffer();
            try {
                URL url = new URL(params[0]);   //有傳入FILE_URL於params[0]
                BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream()));  //BufferedReader->InputStreamReader(url.openSteam())
                String line = "";
                while ((line=in.readLine()) != null){   //有讀到資料 就用StringBuffer.append()加進StringBuffer
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
        //在 doInBackground() 後自動執行 , Parse Json , Set RecyclerView
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayList<ParkInfo> detailData = ParkInfo.fromJsonString(s);    //將讀完的String , 利用ParkInfo裡的fromJsonString()方法Parse JSON
            //Log.d("DetailData" , ""+detailData.get(0).getName());
            setupRecyclerView( detailData);     //將ArrayList<ParkInfo> detailData 設定至 RecyclerView Adapter

        }
    }
    //設定RecyclerView
    private void setupRecyclerView( ArrayList<ParkInfo> list){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter.setParks( list);             //將ArrayList<ParkInfo> list資料 設定到Adapter
        recyclerView.setAdapter(adapter);     //設定Adapter 將資料傳入
        recyclerView.setLayoutManager( new LinearLayoutManager(this)); //設定RecyclerView自己的LayoutManager物件
    }
    //RecyclerView itemView Click Event 點擊事件
    @Override
    public void onItemClick(int id , String parkName , String viewSpot , String introduction , byte[] byteArray ,final boolean check_record) {
        final ContentValues values = new ContentValues();
        values.put("id" , id);
        values.put("parkname" , parkName);
        values.put("viewspot" , viewSpot);
        values.put("introduction" , introduction);
        values.put("image" , byteArray);    //要記錄的Bitmap ,已先處理成 byte[] byteArray
        new AlertDialog.Builder(this)
                .setTitle(parkName+"  |  "+viewSpot)
                .setMessage("景點介紹：\n"+introduction)
                .setNegativeButton("back"  ,null)
                .setPositiveButton("Record", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(check_record){   //已有紀錄 , 就不用寫入Database , Toast顯示已有紀錄
                            Toast.makeText(MainActivity.this,"已有紀錄",Toast.LENGTH_SHORT).show();
                        }
                        else{       //尚未有紀錄 , 寫入Database , Toast顯示紀錄成功
                            helper.getWritableDatabase().insert("record" , null, values);
                            Toast.makeText(MainActivity.this, "紀錄成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }
    //確認網路連線
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  //ConnectivityManager 取得getSystemService 的 CONNECTIVITY_SERVIECE
        NetworkInfo info = conMgr.getActiveNetworkInfo();   //當沒有連線時 , info=null
        if(info!=null && info.isConnected() && info.isAvailable()){
            return true;}
        return  false;
    }


}

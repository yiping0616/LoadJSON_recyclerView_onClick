package com.example.mom.loadjson;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HistroyActivity extends AppCompatActivity implements HistoryAdapter.OnItemClickHandler{

    private MyDBHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histroy);

        helper = MyDBHelper.getInstance(this);
        //設定RecyclerView ,已紀錄的資料
        RecyclerView recyclerView_history = findViewById(R.id.recyclerview_history);
        HistoryAdapter historyAdapter = new HistoryAdapter(this,this);
        recyclerView_history.setAdapter(historyAdapter);
        recyclerView_history.setLayoutManager( new LinearLayoutManager( this ));
    }
    //點擊事件處理
    @Override
    public void onItemClick(final int id , String viewSpot) {
        boolean check_record = false;
        Cursor cursor_check = helper.getReadableDatabase().query(
                "record", new String[]{"id"}, null, null, null, null, null);
        if (cursor_check.moveToFirst()) { //若cursor=empty return false , cursor!=empty return true
            for (int i = 0; i < cursor_check.getCount(); i++) {
                int id_check = cursor_check.getInt(cursor_check.getColumnIndex("id"));
                if (id_check == id) {
                    check_record = true;
                }
                cursor_check.moveToNext();
            }
        }
        cursor_check.close();
        if (check_record) {     //若有此資料 , 刪除資料
            helper.getWritableDatabase().delete("record", "id" + "=" + id, null);
            Toast.makeText(this, "刪除-" + viewSpot + "-紀錄資料", Toast.LENGTH_SHORT).show();
        }
        else{           //若無此資料 , 已經刪除資料
            Toast.makeText(this, "已經刪除", Toast.LENGTH_SHORT).show();
        }
    }
}

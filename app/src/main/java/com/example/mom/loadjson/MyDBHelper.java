package com.example.mom.loadjson;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    //SQLiteOpenHelper 的 Singleton（單獨） 設計
    //以免多個Activity同時存取SQLiteDatabase物件 , 造成鎖住Database之問題
    //將MyDBHelper設定為單一物件 利用static特性 , 確保整個App使用同一個MyDBHelper物件
    private static MyDBHelper instance ;    //MyDBHelper類別中新增一個封閉的static類別變數

    //公開的getInstance()方法 , 以取得MyDBHelper物件 , 在這邊控管 , FinanceActivity and AddActivity 需要MyDBHelper 都呼叫 MyDBHelper.getInstance(this)
    public static MyDBHelper getInstance(Context context){
        if(instance ==null){       //若還沒有,就new MyDBHelper , 有就回傳 instance
            instance = new MyDBHelper(context , "RecordDetail.db" , null , 1 );
        }
        return instance;
    }


    //實作建構子 constructor , 因有做Singleton , 要設為private ,只會在此class中呼叫 MyDBHelper()
    private MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //用db物件中的 execSQL()方法建立 exp 表格
        //建立exp表格的SQL語法
        //CREATE TABLE "exp" ("_id" INTEGER PRIMARY KEY NOT NULL , "cdate" DATETIME NOT NULL , "info" VARCHAR , "amount" INTEGER)
        db.execSQL("CREATE TABLE record"+
                "(id INTEGER PRIMARY KEY NOT NULL, "+
                "parkname VARCHAR NOT NULL ,"+
                "viewspot VARCHAR ,"+
                "introduction TEXT ,"+
                "image BLOB )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
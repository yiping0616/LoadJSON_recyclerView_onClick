package com.example.mom.loadjson;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

//為ParkAdapter加上繼承RecyclerView.Adapter類別 宣告裡面使用的ViewHolder
    public class ParkAdapter extends RecyclerView.Adapter< ParkAdapter.ViewHolder > { //Adapter是抽象類別 需實作三個必要方法 按alt+enter快速解決

        static private ArrayList<ParkInfo> parks;  //集合屬性
        static private HashMap<String , Bitmap > ThumbnailCache = new HashMap<>(); //HashMap做暫存 紀錄Imageurl與對應的bitmap圖片
        static private MyDBHelper helper;

        //點擊事件處理 , 建立interface , 並在裡面寫好要發生的事件
        interface OnItemClickHandler {
            void onItemClick(int id,String parkName,String viewSpot,String introduction,byte[] byteArray,boolean check_record);
        }
        static private OnItemClickHandler ClickHandler;

        //ParkAdapter Constructor 初始建立 , 設定好DBHelper , LoadImage的初始設定 , 點擊事件處理
        public ParkAdapter(Context context , OnItemClickHandler clickHandler){
            helper = MyDBHelper.getInstance(context);
            initLoadImage(context);
            ClickHandler = clickHandler;
        }
        //將fromJson處理完的ArrayList<ParkInfo 設定到ParkAdapter中
        public void setParks(ArrayList<ParkInfo> parks) {
            this.parks = parks;
         }
        //在Adapter中設計一個類別層級的ViewHolder類別 , 繼承RecyclerView.ViewHolder
        //在此類別中 設計一筆資料在畫面上的元件
        public static class ViewHolder extends RecyclerView.ViewHolder{
            TextView parkNameTextView , viewSpotTextView , openTimeTextView ;
            ImageView ThumbnailView;
            String ThumbnailUrl ;
            boolean check_record;
            int Position,position_sql;
            public ViewHolder(View itemView) { //內部類別constructor
                super(itemView);
                parkNameTextView = itemView.findViewById(R.id.parkname_history);
                viewSpotTextView = itemView.findViewById(R.id.viewSpot);
                openTimeTextView = itemView.findViewById(R.id.openTime);
                ThumbnailView = itemView.findViewById(R.id.Thumbnail);
                //點擊事件處理 , 在ViewHolder中建立ClickListener , onItemClick()在MainActivity
                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        int id= getAdapterPosition();  //getAdapterPosition為點擊的項目位置
                        //get點擊項目的資料:id,parkName,viewSpot,introduction,byteArray,check_record
                        ParkInfo parkInfo = parks.get(id);
                        String parkName = parkInfo.getParkName();
                        String viewSpot = parkInfo.getName();
                        String introduction = parkInfo.getIntroduciton();
                        //Bitmap儲存是SQLite的處理 , Bitmap轉成byte[] byteArray
                        Bitmap bitmap = ThumbnailCache.get(parkInfo.getImageUrl());
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                        byte[] byteArray= stream.toByteArray();
                        Cursor cursor_check = helper.getReadableDatabase().query(
                                "record", new String[]{"id"}, null, null, null, null, null);
                        if(cursor_check.moveToFirst()) { //若cursor=empty return false , cursor!=empty return true
                            for (int i = 0; i < cursor_check.getCount(); i++) {
                                int id_check = cursor_check.getInt(cursor_check.getColumnIndex("id"));
                                if (id_check == id) {
                                    check_record = true;
                                }
                                cursor_check.moveToNext();
                            }
                        }
                        cursor_check.close();
                        ClickHandler.onItemClick(id,parkName,viewSpot,introduction,byteArray,check_record);
                    }
                });
             }
        }
        //實作三個方法 onCreateViewHolder , onBindViewHolder , getItemCount
        //當RecyclerView需要顯示一列資料 會呼叫Adapter內的這個方法 先取得一個ViewHolder物件
        //此方法要提供可以展示資料的View物件  產生的ViewHolder會在onBindViewHolder()使用
        //透過ViewGroup parent參數取得LayoutInflater來產生View
        @Override
        public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context)
                    .inflate( R.layout.row_item , parent ,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }
        //當RecyclerView準備要展示一列特定位置(position)的紀錄時 會呼叫onBindViewHolder()
        //holder物件就是onCreateViewHolder()取得的viewHolder , 此方法只需要將holder物件中的元件設定為想要的內容
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ParkInfo park = parks.get(position);
            holder.parkNameTextView.setText(park.getParkName());
            holder.viewSpotTextView.setText(park.getName());
            holder.openTimeTextView.setText(park.getOpenTime());
            holder.ThumbnailUrl = park.getImageUrl();
            holder.Position = position;
            //檢查SQLite中 是否已有此紀錄 節省ThumbnailView存取
            Cursor cursor_image = helper.getReadableDatabase().query(
                    "record", new String[]{"id", "image"}, null, null, null, null, null);
            holder.check_record=false;
            holder.position_sql=0;
            if(cursor_image.moveToFirst()) { //若cursor=empty return false , cursor!=empty return true
                for (int i = 0; i < cursor_image.getCount(); i++) {
                    int id_check = cursor_image.getInt(cursor_image.getColumnIndex("id"));
                    if (id_check == position) {
                        holder.check_record = true;
                        holder.position_sql =i;    //在database中 此資料的position , 後面cursor_image讀取image需要
                    }
                    cursor_image.moveToNext();
                }
            }
            if(holder.check_record){  //若有此紀錄 , 讀取decode ByteArray image BLOB
                cursor_image.moveToPosition(holder.position_sql);  //移到資料庫中此筆資料的位置
                byte[] byteArray = cursor_image.getBlob(cursor_image.getColumnIndex("image"));
                Bitmap bitmap_SQL = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length); //offset:0 從0開始parse
                holder.ThumbnailView.setImageBitmap(bitmap_SQL);
                Log.d("Thumbnail", "p= " + position + "資料庫中有資料");
                ThumbnailCache.put(holder.ThumbnailUrl , bitmap_SQL);
                cursor_image.close();
            }
            else{  //SQLite中沒有記錄此資料 , 檢查HashMap中是否已記錄 , 有就讀取HashMap中對應到的Bitmap , 沒有再下載
                Bitmap bitmap_Cache = ThumbnailCache.get(holder.ThumbnailUrl);
                if (bitmap_Cache == null) {
                    holder.ThumbnailView.setImageBitmap(null);
                    loadImage(holder);
                    Log.d("Thumbnail", "p= " + position + "資料庫中無資料，HashMap中無暫存，已完成下載縮圖");
                } else {
                    holder.ThumbnailView.setImageBitmap(bitmap_Cache);
                    Log.d("Thumbnail", "p= " + position + "資料庫中無資料，HashMap中有暫存");
                }
            }
        }
        @Override
        public int getItemCount() {
            if(parks != null){
                return parks.size();
            }
            else  return 0;
        }
        //ImageLoader初始化建立
        private void initLoadImage(Context context){
            ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
            /*
            config.threadPoolSize(3);
            config.threadPriority(Thread.NORM_PRIORITY - 1);
            config.tasksProcessingOrder(QueueProcessingType.FIFO);
            config.memoryCache(new LruMemoryCache(2 * 1024 * 1024));
            config.memoryCacheSize(2*1024*1024);
            config.diskCacheSize(50 *1024*1024);
            config.diskCacheFileCount(100);
            */
            ImageLoader.getInstance().init(config.build());
        }
        //下載圖片
        private void loadImage(final ViewHolder holder ){
            ImageSize imageSize = new ImageSize(80,70);
            ImageLoader.getInstance().loadImage(holder.ThumbnailUrl , imageSize , new ImageLoadingListener(){
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    Log.d("onLoadingStarted", "p = "+holder.Position);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if(ThumbnailCache.containsKey(holder.ThumbnailUrl)){
                        return;
                    }
                    ThumbnailCache.put(holder.ThumbnailUrl , loadedImage);
                    holder.ThumbnailView.setImageBitmap(loadedImage);

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                }
            });
        }

    }

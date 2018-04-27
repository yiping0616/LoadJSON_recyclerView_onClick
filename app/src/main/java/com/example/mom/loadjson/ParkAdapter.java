package com.example.mom.loadjson;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

//為ParkAdapter加上繼承RecyclerView.Adapter類別 宣告裡面使用的ViewHolder
    public class ParkAdapter extends RecyclerView.Adapter< ParkAdapter.ViewHolder > { //Adapter是抽象類別 需實作三個必要方法 按alt+enter快速解決

        private ArrayList<ParkInfo> parks;  //集合屬性
        private HashMap<String , Bitmap > ThumbnailCache = new HashMap<>(); //HashMap 紀錄Imageurl與對應的bitmap圖片
        private Context context;
        private MyDBHelper helper;
        private Cursor cursor_before , cursor_after;

        public ParkAdapter(Context context){
            this.context = context;
            helper = MyDBHelper.getInstance(context);
            initLoadImage(context);
        }

        public void setParks(ArrayList<ParkInfo> parks) {
            this.parks = parks;
         }

        //在Adapter中設計一個類別層級的ViewHolder類別 , 繼承RecyclerView.ViewHolder
        //在此類別中 設計一筆資料在畫面上的元件
        public static class ViewHolder extends RecyclerView.ViewHolder{
            TextView parkNameTextView , viewSpotTextView , openTimeTextView ;
            ImageView ThumbnailView;
            String ThumbnailUrl ;
            int Position;
            public ViewHolder(View itemView) { //內部類別constructor
                super(itemView);
                parkNameTextView = itemView.findViewById(R.id.parkname_history);
                viewSpotTextView = itemView.findViewById(R.id.viewSpot);
                openTimeTextView = itemView.findViewById(R.id.openTime);
                ThumbnailView = itemView.findViewById(R.id.Thumbnail);
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
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            final ParkInfo park = parks.get(position);
            holder.parkNameTextView.setText(park.getParkName());
            holder.viewSpotTextView.setText(park.getName());
            holder.openTimeTextView.setText(park.getOpenTime());
            holder.ThumbnailUrl = park.getImageUrl();
            holder.Position = position;

            Bitmap bitmap = ThumbnailCache.get(holder.ThumbnailUrl);
            if(bitmap ==null){
                holder.ThumbnailView.setImageBitmap(null);
                loadImage(holder);
            }
            else{
                holder.ThumbnailView.setImageBitmap(bitmap);
            }


            holder.itemView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Bitmap bitmap = ThumbnailCache.get(holder.ThumbnailUrl);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                    byte[] byteArray= stream.toByteArray();

                    final ContentValues values = new ContentValues();
                    values.put("id" , position);
                    values.put("parkname" , park.getParkName());
                    values.put("viewspot" , park.getName());
                    values.put("introduction" , park.getIntroduciton());
                    values.put("image" , byteArray);
                    new AlertDialog.Builder(context)
                            .setTitle(park.getParkName()+"  |  "+park.getName())
                            .setMessage("景點介紹：\n"+park.getIntroduciton())
                            .setNegativeButton("back"  ,null)
                            .setPositiveButton("Record", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    cursor_before = helper.getReadableDatabase().query(
                                            "record",new String[]{"id"},null,null,null,null,null);
                                    int before = cursor_before.getCount();

                                    Log.d("RECORD" , values.getAsString("id")+"/"+values.getAsString("parkname")+"/"+values.getAsString("viewspot"));
                                    helper.getWritableDatabase().insert("record" , null, values);

                                    cursor_after = helper.getReadableDatabase().query(
                                            "record",new String[]{"id"},null,null,null,null,null);
                                    int after = cursor_after.getCount();
                                    Log.d("COMPARE" , "before:"+before+" after:"+after);
                                    if(before == after){
                                        Toast.makeText(context,"已有紀錄",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(context, "紀錄成功",Toast.LENGTH_SHORT).show();
                                    }
                                    }
                            })
                            .show();

                }
            });

        }

        @Override
        public int getItemCount() {
            if(parks != null){
                return parks.size();
            }
            else  return 0;
        }

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

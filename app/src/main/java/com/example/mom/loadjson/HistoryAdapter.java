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

    public class HistoryAdapter extends RecyclerView.Adapter< HistoryAdapter.ViewHolder>{
        //點擊事件處理 , 建立interface , 並在裡面寫好要發生的事件
        interface OnItemClickHandler {
            void onItemClick(int id , String viewSpot);
        }
        private OnItemClickHandler ClickHandler;
        private MyDBHelper helper_adapter;
        private Cursor cursor ;
        private int row_number;
        //HistoryAdapter Constructor
        public HistoryAdapter(Context context , OnItemClickHandler clickHandler){
            helper_adapter = MyDBHelper.getInstance(context);
            cursor = helper_adapter.getReadableDatabase().query(
                    "record",null,null,null,null,null,null);
            row_number= cursor.getCount();
            ClickHandler = clickHandler;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView parkNameTextView , viewSpotTextView , introductionTextView;
            ImageView ThumbnailImageView;
            public ViewHolder(View itemView) {
                super(itemView);
                parkNameTextView = itemView.findViewById(R.id.parkname_history);
                viewSpotTextView = itemView.findViewById(R.id.viewspot_history);
                introductionTextView = itemView.findViewById(R.id.introduction_history);
                ThumbnailImageView = itemView.findViewById(R.id.Thumbnail_history);
                //點擊事件處理 , 在ViewHolder中建立ClickListener , onItemClick()在HistroyActivity
                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        int position= getAdapterPosition();  //getAdapterPosition為點擊的項目位置
                        cursor.moveToPosition(position);
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        String viewSpot = cursor.getString( cursor.getColumnIndex("viewspot"));
                        ClickHandler.onItemClick(id , viewSpot);
                        //notifyItemRemoved(position);
                    }
                });
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context)
                    .inflate( R.layout.row_item_history , parent ,false);
            HistoryAdapter.ViewHolder historyViewHolder = new HistoryAdapter.ViewHolder(view);
            return historyViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            cursor.moveToPosition(position);
            holder.parkNameTextView.setText(cursor.getString(cursor.getColumnIndex("parkname")));
            holder.viewSpotTextView.setText(cursor.getString(cursor.getColumnIndex("viewspot")));
            holder.introductionTextView.setText(cursor.getString((cursor.getColumnIndex("introduction"))));
            //從database中 , 讀取出 byteArray 轉成 Bitmap
            byte[] byteArray = cursor.getBlob(cursor.getColumnIndex("image"));
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length); //offset:0 從0開始parse
            holder.ThumbnailImageView.setImageBitmap(bitmap);
        }
        @Override
        public int getItemCount() {
            return row_number;
        }

    }

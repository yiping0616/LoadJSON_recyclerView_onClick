package com.example.mom.loadjson;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

public class HistroyActivity extends AppCompatActivity {

    private RecyclerView recyclerView_history;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histroy);

        recyclerView_history = findViewById(R.id.recyclerview_history);
        historyAdapter = new HistoryAdapter(this);
        recyclerView_history.setAdapter(historyAdapter);
        recyclerView_history.setLayoutManager( new LinearLayoutManager( this ));
    }

    public class HistoryAdapter extends RecyclerView.Adapter< HistoryAdapter.ViewHolder>{

        Context context;
        MyDBHelper helper_adapter = MyDBHelper.getInstance(HistroyActivity.this);
        Cursor cursor = helper_adapter.getReadableDatabase().query(
                       "record",null,null,null,null,null,null);
        int row_number = cursor.getCount();

        public HistoryAdapter(Context context){
            this.context = context;
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
            byte[] ByteArray = cursor.getBlob(cursor.getColumnIndex("image"));
            Bitmap bitmap = BitmapFactory.decodeByteArray(ByteArray , 0 , ByteArray.length); //offset:0 從0開始parse
            holder.ThumbnailImageView.setImageBitmap(bitmap);

        }

        @Override
        public int getItemCount() {
            return row_number;
        }

    }

}

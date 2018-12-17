package com.example.huhep.litepaltest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.huhep.litepaltest.bean.RoomSet;
import com.example.huhep.litepaltest.utils.Util;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectRoomsetActivity extends AppCompatActivity {
    @BindView(R.id.select_roomset_customToolBar)
    CustomToolbar customToolbar;

    @BindView(R.id.select_roomset_recyclerView)
    RecyclerView recyclerView;


    class SelectRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<RoomSet> roomSetList=null;

        public SelectRoomAdapter() {
            roomSetList = LitePal.findAll(RoomSet.class);
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(SelectRoomsetActivity.this).inflate(R.layout.select_roomset_recyclerview_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            RoomSet roomSet = roomSetList.get(position);
            viewHolder.roomSetCheckBox.setChecked(roomSet.isSelect());
            if (roomSet.isSelect()) {
                viewHolder.roomSetTextView.setTextColor(getResources().getColor(R.color.deepDeepDark));
            } else {
                viewHolder.roomSetTextView.setTextColor(getResources().getColor(R.color.deepDark));
            }
            viewHolder.roomSetTextView.setText(roomSet.getRoomSetName());
            viewHolder.layout.setOnClickListener(v -> {
                roomSet.setSelect(!roomSet.isSelect());
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return roomSetList.size();
        }

        public void saveRoomSetState() {
            for (RoomSet roomSet : roomSetList) {
                roomSet.save();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            private final CheckBox roomSetCheckBox;
            private final TextView roomSetTextView;
            private final ConstraintLayout layout;

            public ViewHolder(View itemView) {
                super(itemView);
                roomSetCheckBox = itemView.findViewById(R.id.select_roomset_item_checkBox);
                roomSetTextView = itemView.findViewById(R.id.select_roomset_item_textview);
                layout = itemView.findViewById(R.id.select_roomset_constraintLayout);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_roomset);
        ButterKnife.bind(this);
        Util.setFullScreen(this);
        customToolbar.setTitle("组别筛选器");
        recyclerView.setAdapter(new SelectRoomAdapter());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    @OnClick(R.id.select_roomset_saveBtn)
    public void saveRoomSetState() {
        Intent intent = getIntent();
        setResult(BaseActivity.RESULT_FROM_SelectRoomSet_TO_AnalyzeManagment, intent);
        ((SelectRoomAdapter) recyclerView.getAdapter()).saveRoomSetState();
        finish();
    }
}

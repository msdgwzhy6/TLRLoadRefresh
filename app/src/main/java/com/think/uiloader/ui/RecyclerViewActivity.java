package com.think.uiloader.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.think.tlr.TLRLinearLayout;
import com.think.tlr.TLRUiHandlerAdapter;
import com.think.uiloader.App;
import com.think.uiloader.R;
import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.ui.di.components.ActivityComponent;
import com.think.uiloader.ui.di.components.DaggerActivityComponent;
import com.think.uiloader.ui.mvp.contract.ImageContract;
import com.think.uiloader.ui.mvp.presenter.ImagePresenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by borney on 5/9/17.
 */
public class RecyclerViewActivity extends AppCompatActivity implements ImageContract.View {
    private TLRLinearLayout mTLRLinearLayout;
    private RecyclerView mRecyclerView;
    private List<ImageEntity.Image> mImageList = new ArrayList<>();
    private App mApp;
    private int curIndex = 0;
    private MyAdapter mAdapter;

    @Inject
    ImagePresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (App) getApplication();
        initActivityComponent();
        setContentView(R.layout.activity_tlrrecyclerview);
        initTlrLayout();
        initRecyclerView();
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new ItemDecorationVerticalDivider(this));
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initTlrLayout() {
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mTLRLinearLayout.addTLRUiHandler(new TLRUiHandlerAdapter() {
            @Override
            public void onRefreshStatusChanged(TLRLinearLayout.RefreshStatus status) {
                if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                    mPresenter.images(curIndex, 10);
                }
            }
        });
    }

    private void initActivityComponent() {
        ActivityComponent component = DaggerActivityComponent.builder().applicationComponent(
                mApp.getApplicationComponent()).build();
        component.inject(this);
        mPresenter.setView(this);
    }

    @Override
    public void startImages() {

    }

    @Override
    public void imagesSuccess(List<ImageEntity.Image> images) {
        if (images != null) {
            mImageList.addAll(0, images);
            curIndex += images.size();
            mAdapter.notifyImages(mImageList);
            mTLRLinearLayout.finishRefresh(true);
        }
    }

    @Override
    public void endImages() {

    }

    @Override
    public void error(int errorCode) {

    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        private final List<ImageEntity.Image> mList = new ArrayList<>();

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            ImageEntity.Image image = mList.get(position);
            Glide.with(RecyclerViewActivity.this).load(image.getThumbnailUrl()).into(holder.imageView);
            holder.textView.setText(image.getDesc());
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public void notifyImages(List<ImageEntity.Image> list) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;

        public MyHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            textView = (TextView) itemView.findViewById(R.id.text);
        }
    }
}
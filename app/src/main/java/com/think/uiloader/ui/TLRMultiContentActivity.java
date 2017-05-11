package com.think.uiloader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by borney on 5/11/17.
 */
public class TLRMultiContentActivity extends AppCompatActivity implements ImageContract.View {
    private ListView mListView;
    private TextView mTextView;
    private TLRLinearLayout mTLRLinearLayout;
    private ListImageAdapter mAdapter;
    private List<ImageEntity.Image> mImageList = new ArrayList<>();
    private App mApp;
    private int refreshCount = 0;
    private int curIndex = 0;
    private Handler mHandler = new Handler();

    @Inject
    ImagePresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (App) getApplication();
        initActivityComponent();
        setContentView(R.layout.activity_tlrmulticontent);
        mListView = (ListView) findViewById(R.id.content);
        mTextView = (TextView) findViewById(R.id.text);
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mTLRLinearLayout.addTLRUiHandler(new TLRUiHandlerAdapter() {
            @Override
            public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
                if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                    if (target instanceof RelativeLayout) {
                        refreshCount += 1;
                        mTLRLinearLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText("刷新了 " + refreshCount + " 次.");
                                mTLRLinearLayout.finishRefresh(true);
                            }
                        }, 1500);
                    } else if (target instanceof ListView) {
                        mPresenter.images(curIndex, 10);
                    }
                }
            }
        });
        mAdapter = new ListImageAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(TLRMultiContentActivity.this, "onclick " + position, Toast.LENGTH_SHORT).show();
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
    public void imagesSuccess(final List<ImageEntity.Image> images) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mImageList.addAll(0, images);
                curIndex += images.size();
                mAdapter.notifyImages(mImageList);
                mTLRLinearLayout.finishRefresh(true);
            }
        }, 1500);
    }

    @Override
    public void endImages() {

    }

    @Override
    public void error(int errorCode) {

    }
}

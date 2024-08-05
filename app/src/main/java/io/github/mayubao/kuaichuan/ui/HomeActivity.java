package io.github.mayubao.kuaichuan.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.github.mayubao.kuaichuan.Constant;
import io.github.mayubao.kuaichuan.R;
import io.github.mayubao.kuaichuan.common.BaseActivity;
import io.github.mayubao.kuaichuan.core.utils.FileUtils;
import io.github.mayubao.kuaichuan.core.utils.TextUtils;
import io.github.mayubao.kuaichuan.core.utils.ToastUtils;
import io.github.mayubao.kuaichuan.ui.view.MyScrollView;
import io.github.mayubao.kuaichuan.utils.NavigatorUtils;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyScrollView.OnScrollListener, View.OnClickListener {

    private static final String TAG = HomeActivity.class.getSimpleName();


    /**
     * 左右两大块 UI
     */

    DrawerLayout mDrawerLayout;

    NavigationView mNavigationView;

    TextView tv_name;

    /**
     * top bar 相关UI
     */

    LinearLayout ll_mini_main;

    TextView tv_title;
    ImageView iv_mini_avator;

    Button btn_send;

    Button btn_receive;

    /**
     * 其他UI
     */
    MyScrollView mScrollView;
    LinearLayout ll_main;

    Button btn_send_big;

    Button btn_receive_big;


    RelativeLayout rl_device;

    TextView tv_device_desc;

    RelativeLayout rl_file;

    TextView tv_file_desc;

    RelativeLayout rl_storage;

    TextView tv_storage_desc;


    //大的我要发送和我要接受按钮的LinearLayout的高度
    int mContentHeight = 0;


    //
    boolean mIsExist = false;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        //TODO
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_FILE);
        }else{
            //初始化
            init();
        }
    }

    @Override
    protected void onResume() {
        updateBottomData();
        super.onResume();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_FILE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //初始化
                init();
            } else {
                // Permission Denied
                ToastUtils.show(this, getResources().getString(R.string.tip_permission_denied_and_not_send_file));
                finish();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 初始化
     */
    private void init() {
        ll_mini_main = (LinearLayout) findViewById(R.id.ll_mini_main);
        mScrollView = (MyScrollView) findViewById(R.id.msv_content);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_mini_avator = (ImageView) findViewById(R.id.iv_mini_avator);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_receive = (Button) findViewById(R.id.btn_receive);
        ll_main = (LinearLayout) findViewById(R.id.ll_main);

        btn_send_big = (Button) findViewById(R.id.btn_send_big);

        btn_receive_big = (Button) findViewById(R.id.btn_receive_big);


        rl_device = (RelativeLayout) findViewById(R.id.rl_device);

        tv_device_desc = (TextView) findViewById(R.id.tv_device_desc);

        rl_file = (RelativeLayout) findViewById(R.id.rl_file);

        tv_file_desc = (TextView) findViewById(R.id.tv_file_desc);

        rl_storage = (RelativeLayout) findViewById(R.id.rl_storage);

        tv_storage_desc = (TextView) findViewById(R.id.tv_storage_desc);

        btn_send.setOnClickListener(this);
        btn_send_big.setOnClickListener(this);

        btn_receive.setOnClickListener(this);
        btn_receive_big.setOnClickListener(this);

        iv_mini_avator.setOnClickListener(this);

        rl_file.setOnClickListener(this);

        rl_storage.setOnClickListener(this);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();


        mNavigationView.setNavigationItemSelectedListener(this);

        //设置设备名称
        String device = TextUtils.isNullOrBlank(android.os.Build.DEVICE) ? Constant.DEFAULT_SSID : android.os.Build.DEVICE;

        tv_name = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);
        tv_name.setText(device);



        mScrollView.setOnScrollListener(this::onScrollChanged);



        ll_mini_main.setClickable(false);
        ll_mini_main.setVisibility(View.GONE);

        updateBottomData();

//        测试bugly集成
//        testBugly();



    }

    /**
     * 测试bugly集成
     */
    private void testBugly(){
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                throw new RuntimeException("======>>>这是茄子快传的一个错误测试");
            }
        }.start();
    }

    /**
     * 更新底部 设备数，文件数，节省流量数的数据
     */
    private void updateBottomData(){
        //TODO 设备数的更新
        //TODO 文件数的更新
        tv_file_desc.setText(String.valueOf(FileUtils.getReceiveFileCount()));
        //TODO 节省流量数的更新
        tv_storage_desc.setText(String.valueOf(FileUtils.getReceiveFileListTotalLength()));

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
//                super.onBackPressed();
                if(mIsExist){
                    this.finish();
                }else{
                    ToastUtils.show(getContext(), getContext().getResources().getString(R.string.tip_call_back_agin_and_exist)
                                        .replace("{appName}", getContext().getResources().getString(R.string.app_name)));
                    mIsExist = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsExist = false;
                        }
                    }, 2 * 1000);

                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_about){
            Log.i(TAG, "R.id.nav_about------>>> click");
            showAboutMeDialog();
        }else if(id == R.id.nav_web_transfer){
            Log.i(TAG, "R.id.nav_web_transfer------>>> click");
//            NavigatorUtils.toWebTransferUI(getContext());
            NavigatorUtils.toChooseFileUI(getContext(), true);
        }else{
            ToastUtils.show(getContext(), getResources().getString(R.string.tip_next_version_update));
        }


        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.btn_send:
            case R.id.btn_send_big: {
                NavigatorUtils.toChooseFileUI(getContext());
                break;
            }
            case R.id.btn_receive:
            case R.id.btn_receive_big: {
                NavigatorUtils.toReceiverWaitingUI(getContext());
                break;
            }
            case R.id.iv_mini_avator: {
                if(mDrawerLayout != null){
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;
            }
            case R.id.rl_file:
            case R.id.rl_storage: {
                NavigatorUtils.toSystemFileChooser(getContext());
                break;
            }

        }
    }

    //自定义ScrollView的监听
    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        Log.i(TAG, "l-->" + l + ",t-->" + t + ",oldl-->" + oldl + ",oldt-->" + oldt);
        mContentHeight = ll_main.getMeasuredHeight();
//        Log.i(TAG, "content height : " + mContentHeight);
//        float alpha = t / (float)mContentHeight;
//        Log.i(TAG, "content alpha : " + alpha);
//        tv_title.setAlpha(alpha);
        //一半的位置时候
        // topbar上面的两个小按钮 跟 主页上面的两个大按钮的alpha值是对立的 即 alpha 与 1-alpha的关系
        if(t > mContentHeight / 2){
            float sAlpha = (t - mContentHeight / 2) /  (float)(mContentHeight / 2);
            ll_mini_main.setVisibility(View.VISIBLE);
            ll_main.setAlpha(1-sAlpha);
            ll_mini_main.setAlpha(sAlpha);
            tv_title.setAlpha(0);
        } else{
            float tAlpha = t / (float)mContentHeight / 2;
            tv_title.setAlpha(1 - tAlpha);
            ll_mini_main.setVisibility(View.INVISIBLE);
            ll_mini_main.setAlpha(0);
        }

    }

    /**
     * 显示对话框
     */
    private void showAboutMeDialog(){
        View contentView = View.inflate(getContext(), R.layout.view_about_me, null);
        contentView.findViewById(R.id.tv_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toProject();
            }
        });
        new AlertDialog.Builder(getContext())
                .setTitle(getResources().getString(R.string.title_about_me))
                .setView(contentView)
                .setPositiveButton(getResources().getString(R.string.str_weiguan), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toProject();
                    }
                })
                .create()
                .show();
    }

    /**
     * 跳转到项目
     */
    private void toProject() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(Constant.GITHUB_PROJECT_SITE);
        intent.setData(uri);
        getContext().startActivity(intent);
    }
}

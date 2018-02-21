package cn.atd3.code4a.view.view;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.view.KeyEvent;
import android.view.View;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import cn.atd3.code4a.Constant;
import cn.atd3.code4a.R;
import cn.atd3.code4a.presenter.SplashPresenter;
import cn.atd3.code4a.view.inter.SplashViewInterface;
import cn.qingyuyu.commom.ui.SplashAd;

import static android.view.KeyEvent.KEYCODE_BACK;
import static cn.atd3.code4a.Constant.ERROR;
import static cn.atd3.code4a.Constant.INFO;
import static cn.atd3.code4a.Constant.NORMAL;
import static cn.atd3.code4a.Constant.SUCCESS;
import static cn.atd3.code4a.Constant.WARNING;


public class SplashActivity extends AppCompatActivity implements SplashViewInterface {

    private SplashPresenter sp;
    private SplashAd sad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将window的背景图设置为空
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        setContentView(R.layout.activity_splash);
        View mContentView = findViewById(R.id.splash_view);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        sad = findViewById(R.id.splash_ad);

        sp = new SplashPresenter(this, this);//实例化，并将自身做参数

        sp.requestPermissions(this);//请求权限

        sp.requestAdInfo();//请求广告信息

        sp.setSplashAdListener(this, sad);//设置广告监听

        sp.showAd(3000);//显示广告3秒

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        sp.onRequestPermissionsResult(requestCode, grantResults);//Presenter处理请求权限结果
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onImageUpdate(final Uri imguri) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sad != null)
                    sad.setAdImageURI(imguri);
                //设置图片
            }
        });

    }


    @Override
    public void showToast(final int infotype, final String info) {

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        final  QMUITipDialog tipDialog ;
                        switch (infotype) {
                            case SUCCESS:
                                tipDialog = new QMUITipDialog.Builder(SplashActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                        .setTipWord(info)
                                        .create();
                                break;
                            case INFO:
                                tipDialog = new QMUITipDialog.Builder(SplashActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                                        .setTipWord(info)
                                        .create();
                                break;
                            case NORMAL:
                                tipDialog = new QMUITipDialog.Builder(SplashActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
                                        .setTipWord(info)
                                        .create();
                                break;
                            case WARNING:
                                tipDialog = new QMUITipDialog.Builder(SplashActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                        .setTipWord(info)
                                        .create();
                                break;
                            case ERROR:
                                tipDialog =  new QMUITipDialog.Builder(SplashActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                        .setTipWord(Constant.debugmodeinfo?info:getString(R.string.remote_error))
                                        .create();
                                break;
                            default:
                                tipDialog = new QMUITipDialog.Builder(SplashActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
                                        .setTipWord(info)
                                        .create();
                        }
                        tipDialog.show();
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(1500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        finally {
                                            tipDialog.dismiss();
                                        }

                                    }
                                }
                        ).start();
                    }
                }
        );

    }

    @Override
    public String getXmlString(int resourceId) {
        return getString(resourceId);
    }

    @Override
    public void setSplashAdListener(SplashAd.SplashAdListener sal) {
        if (sad != null)
            sad.setSplashAdListener(sal);
    }

    @Override
    public void showAd(final int showtime) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (sad != null)
                            sad.show(SplashActivity.this, showtime);
                    }
                }
        );
    }

}

package cn.atd3.code4a.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import cn.atd3.code4a.Constant;
import cn.atd3.code4a.R;
import cn.atd3.code4a.database.ArticleDatabase;
import cn.atd3.code4a.model.inter.SplashAdModelInterface;
import cn.atd3.code4a.model.model.AdInfo;
import cn.atd3.code4a.model.model.CategoryModel;
import cn.atd3.code4a.model.model.SplashAdModel;
import cn.atd3.code4a.net.Remote;
import cn.atd3.code4a.view.inter.SplashViewInterface;
import cn.atd3.code4a.view.view.MainActivity;
import cn.atd3.proxy.exception.PermissionException;
import cn.atd3.proxy.exception.ServerException;
import cn.dxkite.common.StorageData;
import cn.qingyuyu.commom.service.FileDealService;
import cn.qingyuyu.commom.ui.SplashAd;

import static cn.atd3.code4a.Constant.ERROR;

//import cn.atd3.code4a.model.model.AdInfo;

/**
 * Created by harry on 2018/1/12.
 */

public class SplashPresenter {
    private static final String TAG = "SplashPresenter";
    private SplashAdModelInterface sami;
    private SplashViewInterface svi;

    private boolean isPermission = false;
    private boolean init = false;

    private Context context;

    public SplashPresenter(SplashViewInterface s, Context context) {
        this.svi = s;
        sami = new SplashAdModel();
        this.context = context;
    }

    private void updateImage() {
        svi.onImageUpdate(sami.getImageUri());
    }

    private void setAd(Uri imguri, String url) {
        sami.setImageUri(imguri);
        sami.setUrl(url);
    }

    //供View层调用，用来请求广告数据
    public void requestAdInfo() {
        getAd();
    }

    //供ui层调用，用来请求权限

    public void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23 && !isPermission) {
            String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {//检查权限
                Log.e("请求权限", "正在请求");
                activity.requestPermissions(permission, 0);//请求
            }
            // 确认权限
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {//检查权限
                isPermission = true;
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        } else {
            isPermission = true;
        }

        if (isPermission) {

            initApp();
        }
    }

    //供ui层调用，用来处理请求权限结果
    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)// 获取到权限，作相应处理
        {
            isPermission = true;
            initApp();
        }

    }

    private void initApp() {
        if (!init) {
            Log.d(TAG, "init applications");
            onDirInit();//创建文件夹
            onInitArticleData();
            createUuid();
            init = true;
        }
    }

    //绑定广告监听
    public void setSplashAdListener(final Activity activity, final SplashAd sad) {
        SplashAd.SplashAdListener sal = new SplashAd.SplashAdListener() {
            Intent intent = new Intent(activity, MainActivity.class);

            @Override
            public void onAdImageClicked() {
                if(sami.getUrl()!=null&&!sami.getUrl().isEmpty())
                intent.putExtra("url",sami.getUrl());
                sad.dismiss();
            }

            @Override
            public void doWhenAdDismiss() {
                //跳转到主界面
                activity.startActivity(intent);
                activity.finish();
            }
        };

        svi.setSplashAdListener(sal);
    }

    public void showAd(final int showtime) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isPermission) {
                    try {
                        Thread.sleep(500);//等待授权结束
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (sami.getImageUri() == null)//图片为空
                    svi.showAd(1000);
                else
                    svi.showAd(showtime);
            }
        }).start();

    }

    private void createUuid() {
        String uuid = UUID.randomUUID().toString();
        File uuidFile = new File(Constant.getPrivateFilePath() + File.separator + "installID");
        FileDealService.putContent(uuidFile, uuid);
        Constant.setUuid(uuid);
    }

    private void onDirInit()//初始化应用文件夹
    {
        File userDir = new File(Constant.getPublicFilePath());
        File zipDir = new File(Constant.getPublicFilePath() + Constant.zipDir);
        File priDir = new File(Constant.getPrivateFilePath());

        try {
            if (!userDir.exists())
                userDir.mkdir();

            if (!zipDir.exists())
                zipDir.mkdir();

            if (!priDir.exists())
                priDir.mkdir();
        } catch (Exception e) {
            svi.showToast(ERROR, svi.getXmlString(R.string.wanning_storage));
        }
    }

    private void onInitArticleData() {
        new Thread() {
            @Override
            public void run() {
                try {
                    setName("fetch list data");
                    Object list = Remote.category.method("getCategoryListByParentId", CategoryModel.class).call(0);
                    if (list instanceof ArrayList) {
                        // 下载第一屏文章列表
                        ArticleDatabase a = new ArticleDatabase(context);
                        if (a.isEmpty()) {
                            a.fetchFirst();
                        }
                        StorageData.saveObject(new File(Constant.getCategoryListFilePath()), list);
                        Log.i(TAG, list.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getAd() {//从本地加载广告信息

        try {

            File adImg = new File(Constant.getAdImg());//本地图片文件

            File adUrl = new File(Constant.getAdUrl());//本地链接文件

            if (adImg.exists() && adUrl.exists())//设置Uri
            {
                String u = FileDealService.getInstance().readFile(adUrl.getAbsolutePath());//读取链接
                setAd(Uri.fromFile(adImg), u);
                //通知View层改变视图
                updateImage();
            }

        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }

    }
}

package com.app.ui.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.LoadPicture;
import com.app.LoadPicture.ImageDownloadedCallBack;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.ftp.Ftp;
import com.app.ftp.FtpListener;
import com.app.http.ToastUtils;
import com.app.model.Constant;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.ActivityCollector;
import com.app.tools.VersionXmlParse;
import com.app.ui.FamilyCircle;
import com.app.ui.MyCouponActivity;
import com.app.ui.SaomaActivity;
import com.app.ui.ServiceCallSet;
import com.app.ui.Setting;
import com.app.ui.UploadPictureActivity;
import com.app.videoAndPictureUpload.SelectVideoActivity;
import com.app.view.CircleImageView;
import com.app.view.CustomProgressDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static com.app.camera.FileOperateUtil.TAG;
import static com.app.sip.SipInfo.sipUser;

public class PersonFragment extends Fragment implements View.OnClickListener{
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

private CircleImageView iv_avatar;
    private TextView tv_name;
    TextView tv_fxid;
    TextView title;
    //手机内存卡路径
    String SdCard;
    //当前版本
    String version;
    //FTP上的版本
    String FtpVersion;
    //用于版本xml解析
    HashMap<String, String> versionHashMap = new HashMap<>();
    //进度条
    CustomProgressDialog loading;
    //进度条消失类型
    String result;
    //下载进度条
    ProgressDialog downloadDialog;
    //apk路径
    String apkPath;
    String avaPath;
    private LoadPicture avatarLoader;
    private String avatar = "";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, container, false);

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RelativeLayout lay =(RelativeLayout) getView().findViewById(
                R.id.main1);
        title=(TextView)lay.findViewById(R.id.title);
        title.setText("个人中心");
        TextPaint tp=title.getPaint();
        tp.setFakeBoldText(true);
        SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
        apkPath = SdCard + "/fanxin/download/apk/";
        avaPath = SdCard + "/fanxin/Files/Camera/Image/";
        avatarLoader = new LoadPicture(getActivity(), avaPath);
        RelativeLayout re_myinfo = (RelativeLayout) getView().findViewById(
                R.id.re_myinfo);
        re_myinfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),
                        FamilyCircle.class));
            }

        });
        iv_avatar = (CircleImageView) re_myinfo.findViewById(R.id.iv_avatar);
        tv_name = (TextView) re_myinfo.findViewById(R.id.tv_name);
        tv_fxid = (TextView) re_myinfo.findViewById(R.id.tv_fxid);
        avatar = LocalUserInfo.getInstance(getActivity()).getUserInfo("avatar");
        tv_name.setText("昵称: " + Constant.nick);
        tv_fxid.setText("手机号：" + Constant.phone);
        try {
            showUserAvatar(iv_avatar, avatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RelativeLayout re_xaingce = (RelativeLayout) getView().findViewById(
                R.id.re_xiangce);
        RelativeLayout re_addev = (RelativeLayout) getView().findViewById(
                R.id.re_adddev);
        RelativeLayout re_servicecall = (RelativeLayout) getView().findViewById(
                R.id.re_servicecall);
        RelativeLayout re_order=(RelativeLayout)getView().findViewById(
                R.id.re_order);
        RelativeLayout re_coupon=(RelativeLayout)getView().findViewById(
                R.id.re_coupon);
        RelativeLayout re_shoppingcart=(RelativeLayout)getView().findViewById(
                R.id.re_shoppingcart);
        RelativeLayout re_collection=(RelativeLayout)getView().findViewById(
                R.id.re_collection);
        RelativeLayout re_settings=(RelativeLayout)getView().findViewById(
                R.id.re_settings);
        re_xaingce.setOnClickListener(this);
        re_addev.setOnClickListener(this);
        re_servicecall.setOnClickListener(this);
        re_order.setOnClickListener(this);
        re_coupon.setOnClickListener(this);
        re_shoppingcart.setOnClickListener(this);
        re_collection.setOnClickListener(this);
        re_settings.setOnClickListener(this);

//        Window window = getActivity().getWindow();
//        //如果系统5.0以上
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(R.color.white));
//        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write PermissionUtils
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {// We don't have PermissionUtils so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.re_xiangce:
//                verifyStoragePermissions(getActivity());
                /**
                 * Checks if the app has PermissionUtils to write to device storage
                 * If the app does not has PermissionUtils then the user will be prompted to
                 * grant permissions
                 * @param activity
                 */
                ToastUtils.makeShortText("该功能即将上线",getActivity());
            showPhotoDialog();
                break;
//            case R.id.re_psd:
//                startActivity(new Intent(getActivity(), ChangePassword.class));
//                break;
            case R.id.re_adddev:
                startActivity(new Intent(getActivity(), SaomaActivity.class));
                break;
            case R.id.re_servicecall:
                startActivity(new Intent(getActivity(), ServiceCallSet.class));
                break;
            case R.id.re_order:
                ToastUtils.makeShortText("该功能即将上线",getActivity());
                break;
            case R.id.re_coupon:
                startActivity(new Intent(getActivity(),MyCouponActivity.class));
                break;
            case R.id.re_shoppingcart:
                ToastUtils.makeShortText("该功能即将上线",getActivity());
                break;
            case R.id.re_collection:
                ToastUtils.makeShortText("该功能即将上线",getActivity());
                break;
//            case R.id.re_instruction:
//                startActivity(new Intent(getActivity(),SoftwareIntruct.class));
//                break;
            case R.id.re_settings:
                startActivity(new Intent(getActivity(),Setting.class));
                break;
        }
    }
    @SuppressLint("NewApi")
    private void requestReadExternalPermission() {

        if (ContextCompat.checkSelfPermission(getContext()
                ,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "READ PermissionUtils IS NOT granted...");

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Log.d(TAG, "11111111111111");
            } else {
                // 0 是自己定义的请求coude
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                Log.d(TAG, "222222222222");
            }
        } else {
            Log.d(TAG, "READ PermissionUtils is granted...");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "requestCode=" + requestCode + "; --->" + permissions.toString()
                + "; grantResult=" + grantResults.toString());
        switch (requestCode) {
            case 0: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // PermissionUtils was granted
                    // request successfully, handle you transactions

                } else {

                    // PermissionUtils denied
                    // request failed
                }

                return;
            }
            default:
                break;

        }
    }


    private void showPhotoDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(getActivity()).create();

        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.alertdialog);
//        WindowManager.LayoutParams attributes = window.getAttributes();
//        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
//        attributes.gravity = Gravity.BOTTOM ;
//        // 一定要重新设置, 才能生效
//        window.setAttributes(attributes);
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.BOTTOM);
        // 为确认按钮添加事件,执行退出应用操作
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("照片");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {

                startActivity(new Intent(getActivity(),UploadPictureActivity.class));
                dlg.cancel();
            }
        });
        TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
        tv_xiangce.setText("视频");
        tv_xiangce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(getActivity(), SelectVideoActivity.class));

                dlg.cancel();
            }
        });
        TextView tv_quxiao = (TextView) window.findViewById(R.id.tv_content3);
        tv_quxiao.setText("取消");
        tv_quxiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.cancel();
            }
        });
        dlg.show();

    }
    private void showUserAvatar(ImageView iamgeView, String avatar) {
        final String url_avatar = Constant.URL_Avatar +Constant.id+"/"+ avatar;
        //iamgeView.setTag(url_avatar);
        if (avatar != null && !avatar.equals("")) {
            Bitmap bitmap = avatarLoader.loadImage(iamgeView, url_avatar,
                    new ImageDownloadedCallBack() {

                        @Override
                        public void onImageDownloaded(ImageView imageView,
                                                      Bitmap bitmap) {
                            //if (imageView.getTag() == url_avatar) {
                                imageView.setImageBitmap(bitmap);

                            //}
                        }

                    });
            if (bitmap != null)
                iamgeView.setImageBitmap(bitmap);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String vatar_temp = LocalUserInfo.getInstance(getActivity())
                .getUserInfo("avatar");
        if (!vatar_temp.equals(Constant.avatar)&&vatar_temp!=null&&!vatar_temp.equals("")) {
            showUserAvatar(iv_avatar, vatar_temp);
        }else {
            showUserAvatar(iv_avatar,Constant.avatar);
        }
        String nick_temp = LocalUserInfo.getInstance(getActivity())
                .getUserInfo("nick");
        if (!nick_temp.equals(Constant.nick)&&nick_temp!=null&&!nick_temp.equals("")) {
            tv_name.setText("昵称："+nick_temp);
        }else {
            tv_name.setText("昵称："+Constant.nick);
        }
        tv_fxid.setText("手机号：  " + SipInfo.userAccount);
    }

    //Ftp对象
    Ftp mFtp;
    //版本信息下载监听器
    FtpListener Dversion = new FtpListener() {
        @Override
        public void onStateChange(String currentStep) {
            Log.i(TAG, currentStep);
        }

        @Override
        public void onUploadProgress(String currentStep, long uploadSize, File targetFile) {

        }

        @Override
        public void onDownLoadProgress(String currentStep, long downProcess, File targetFile) {
            if (currentStep.equals(Constant.FTP_DOWN_SUCCESS)) {
                Log.i(TAG, currentStep);
            } else if (currentStep.equals(Constant.FTP_DOWN_LOADING)) {
                Log.i(TAG, "-----下载---" + downProcess + "%");
            }
        }

        @Override
        public void onDeleteProgress(String currentStep) {

        }
    };
    //版本apk下载监听器
    FtpListener Dapk = new FtpListener() {
        @Override
        public void onStateChange(String currentStep) {

        }

        @Override
        public void onUploadProgress(String currentStep, long uploadSize, File targetFile) {

        }

        @Override
        public void onDownLoadProgress(String currentStep, long downProcess, File targetFile) {
            if (currentStep.equals(Constant.FTP_DOWN_SUCCESS)) {
                Log.d(TAG, currentStep);
                downloadDialog.dismiss();
                Message message = new Message();
                message.what = 0x0002;
                handler.sendMessage(message);
            }
            if (currentStep.equals(Constant.FTP_DOWN_LOADING)) {
                downloadDialog.setProgress((int) downProcess);
                Log.i(TAG, "-----下载---" + downProcess + "%");
            }
        }

        @Override
        public void onDeleteProgress(String currentStep) {

        }
    };
    Runnable checkVersion = new Runnable() {
        @Override
        public void run() {
            try {
                //下载版本信息xml文件
                mFtp.download("/apk/version_fanxin.xml", SdCard + "/fanxin/version/");
                File xml = new File(SdCard + "/fanxin/version/version_fanxin.xml");
                InputStream inputStream = new FileInputStream(xml);
                //解析xml文件
                versionHashMap = VersionXmlParse.parseXml(inputStream);
            } catch (Exception e) {
                result = e.getMessage();
            }
            //获取ftp上的版本号
            FtpVersion = versionHashMap.get("version");
            //根据result显示相应的对话框
            showVersionDialog(version, FtpVersion, result);
        }
    };

    private void showVersionDialog(String currentVersion, final String FtpVersion, final String result) {
        //取消进度条
        loading.dismiss();
        if (result.equals("Finished")) {
            Log.i(TAG, "当前版本为 " + version + "FTP上版本为 " + FtpVersion);
            if (!currentVersion.equals(FtpVersion)) {
                //版本不一致
                Message message = new Message();
                message.what = 0x0001;
                handler.sendMessage(message);
            } else {
                //版本一致
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                .setTitle("当前为最新版本")
                                .setPositiveButton("确定", null)
                                .create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                });
            }
        } else {
            //失败
            showTip(result);
        }
    }

    //下载完成
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: "+msg.what);
            switch (msg.what) {
                case 0x0001:
                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("有新版本")
                            .setMessage("当前版本为" + version + ",新版本为" + FtpVersion)
                            .setPositiveButton("下载并安装", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadDialog = new ProgressDialog(getActivity());
                                    downloadDialog.setTitle("下载进度");
                                    downloadDialog.setMessage("已经下载了");
                                    downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    downloadDialog.setCancelable(false);
                                    downloadDialog.setIndeterminate(false);
                                    downloadDialog.setMax(100);
                                    downloadDialog.show();
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mFtp.setListener(Dapk);
                                            try {
                                                mFtp.download(versionHashMap.get("path"), apkPath);
                                            } catch (final Exception e) {
                                                downloadDialog.dismiss();
                                                showTip("网络连接失败,请检查网络或重试");
                                            }
                                        }
                                    }.start();
                                }
                            })
                            .setNegativeButton("取消", null).create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    break;
                case 0x0002:
                    //apk文件路径
                    Log.d(TAG, "handleMessage: "+msg.what);
                    String localApkPath = apkPath + versionHashMap.get("name")+".apk";
                    Log.d(TAG, "handleMessage: "+localApkPath);
                    File file = new File(localApkPath);
                    if (file.exists()) {
                        Log.d(TAG, "handleMessage: "+localApkPath);
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //设置intent的Action属性
                        intent.setAction(Intent.ACTION_VIEW);
                        //设置intent的data和Type属性。
                        intent.setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                        //注销
                        sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                                SipInfo.user_from, BodyFactory.createLogoutBody()));
//                        SipInfo.sipDev.sendMessage(SipMessageFactory.createNotifyRequest(SipInfo.sipDev, SipInfo.dev_to,
//                                SipInfo.dev_from, BodyFactory.createLogoutBody()));
                        //界面回到登录状态
                        ActivityCollector.finishToFirstView();
                        //跳转到安装界面
                        startActivity(intent);
                    }
                    break;
            }
            return true;
        }
    });

    private void showTip(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

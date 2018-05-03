package com.app.ui;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.app.groupvoice.GroupInfo;
import com.app.model.Constant;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.ActivityCollector;
import com.app.tools.VersionXmlParse;
import com.app.videoAndPictureUpload.SelectVideoActivity;
import com.app.view.CircleImageView;
import com.app.view.CustomProgressDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static com.app.camera.FileOperateUtil.TAG;
import static com.app.model.Constant.groupid1;
import static com.app.sip.SipInfo.sipUser;

public class PersonFragment extends Fragment implements View.OnClickListener{
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
                        MyUserInfoActivity.class));
            }

        });
        iv_avatar = (CircleImageView) re_myinfo.findViewById(R.id.iv_avatar);
        tv_name = (TextView) re_myinfo.findViewById(R.id.tv_name);
        tv_fxid = (TextView) re_myinfo.findViewById(R.id.tv_fxid);
        avatar = LocalUserInfo.getInstance(getActivity()).getUserInfo("avatar");
        tv_name.setText("昵称: " + Constant.nick);
        tv_fxid.setText("手机号：" + Constant.phone);
            showUserAvatar(iv_avatar, avatar);
        RelativeLayout re_xaingce = (RelativeLayout) getView().findViewById(
                R.id.re_xiangce);
        RelativeLayout re_addev = (RelativeLayout) getView().findViewById(
                R.id.re_adddev);
        RelativeLayout re_psd = (RelativeLayout) getView().findViewById(
                R.id.re_psd);
        RelativeLayout re_jiqun = (RelativeLayout) getView().findViewById(
                R.id.re_jiqun);
        RelativeLayout re_setting = (RelativeLayout) getView().findViewById(
                R.id.re_video);
        RelativeLayout re_update = (RelativeLayout) getView().findViewById(
                R.id.re_update);
        re_xaingce.setOnClickListener(this);
        re_addev.setOnClickListener(this);
        re_psd.setOnClickListener(this);
        re_jiqun.setOnClickListener(this);
        re_setting.setOnClickListener(this);
        re_update.setOnClickListener(this);
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.re_xiangce:
                showPhotoDialog();
                break;
            case R.id.re_psd:
                startActivity(new Intent(getActivity(), ChangePassword.class));
                break;
            case R.id.re_adddev:
                startActivity(new Intent(getActivity(), saomaActivity.class));
                break;
            case R.id.re_jiqun:
                startActivity(new Intent(getActivity(), ChsChange.class));
                break;
            case R.id.re_video:
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setCancelable(false)
                        .setMessage("注销账户?")
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                                        SipInfo.user_from, BodyFactory.createLogoutBody()));
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    SipInfo.sipDev.sendMessage(SipMessageFactory.createNotifyRequest(SipInfo.sipDev, SipInfo.dev_to,
                                            SipInfo.dev_from, BodyFactory.createLogoutBody()));
                                }
                                if ((groupid1 != null) && !("".equals(groupid1))) {
                                    GroupInfo.groupUdpThread.stopThread();
                                    GroupInfo.groupKeepAlive.stopThread();
                                }
                                dialog.dismiss();
                                SipInfo.running=false;
                                ActivityCollector.finishToFirstView();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                break;
            case R.id.re_update:
                result = "Finished";
                loading = new CustomProgressDialog(getActivity());
                loading.setCancelable(false);
                loading.setCanceledOnTouchOutside(false);
                loading.show();
                //初始化FTP
                mFtp = new Ftp("101.69.255.132", 21, "ftpall", "123456", Dversion);
                //获取当前版本号
                PackageManager packageManager = getActivity().getPackageManager();
                try {
                    PackageInfo pi = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
                    version = pi.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                new Thread(checkVersion).start();
        }
    }

    private void showPhotoDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(getActivity()).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.alertdialog);
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
                    String localApkPath = apkPath + versionHashMap.get("name");
                    File file = new File(localApkPath);
                    if (file.exists()) {
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

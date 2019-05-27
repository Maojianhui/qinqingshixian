package com.app.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.LoadPicture;
import com.app.LoadPicture.ImageDownloadedCallBack;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.http.GetPostUtil;
import com.app.http.ToastUtils;
import com.app.model.Constant;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.ActivityCollector;
import com.app.utils.ProviderUtil;
import com.app.view.CircleImageView;

import org.w3c.dom.Text;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

import java.io.File;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.app.model.Constant.URL_updateSex;
import static com.app.model.Constant.id;
import static com.app.model.Constant.sex;
import static com.app.sip.SipInfo.devName;

@SuppressLint("SdCardPath")
public class MyUserInfoActivity extends Activity implements View.OnClickListener {

//    private static class MyHandler extends Handler{
//        private final WeakReference<MyUserInfoActivity> mActivity;
//
//        public MyHandler(MyUserInfoActivity activity){
//            mActivity=new WeakReference<MyUserInfoActivity>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg){
//            MyUserInfoActivity activity=mActivity.get();
//            if(activity!=null){
////                activity.handleMessage(msg);
//            }
//        }
//
//
//
//        private static final Runnable sRunnable=new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        };
//    }
//    private final MyHandler mHandler=new MyHandler(this);

    private static final int CAMERA_REQUEST_CODE = 1;
    private RelativeLayout re_avatar;
    private RelativeLayout re_name;
    private RelativeLayout re_sex;
    private TextView tv_sex1;
    private ImageView back;
    private TextView titleset;
    private CircleImageView iv_avatar;
    private TextView tv_name;
    private ProgressDialog dialog;
    private static String imageName;
    private String response = "";
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private static final int UPDATE_FXID = 4;// 结果
    private static final int UPDATE_NICK = 5;// 结果
    private LoadPicture avatarLoader;
    private SharedPreferences sp;
    String hxid;
    String nick;
    String SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
    String avaPath = SdCard + "/fanxin/Files/Camera/Image/";
    private String picPath;
    private View inflate;
    private Uri imageUri;
    private int background;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);
        avatarLoader = new LoadPicture(this, avaPath);
        initView();
        ActivityCollector.addActivity(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.image_bar));
        }
    }

    private void initView() {
        hxid = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo(
                "hxid");
        nick = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo(
                "nick");
        String vatar_temp = LocalUserInfo.getInstance(MyUserInfoActivity.this)
                .getUserInfo("avatar");
        Log.w("uuuuuuuu.....", "头像为" + vatar_temp);
        re_avatar = (RelativeLayout) this.findViewById(R.id.re_avatar);
        re_name = (RelativeLayout) this.findViewById(R.id.re_name);
        re_sex = (RelativeLayout) this.findViewById(R.id.re_sex);
        re_avatar.setOnClickListener(this);
        re_name.setOnClickListener(this);
        re_sex.setOnClickListener(this);
        // 头像
        iv_avatar = (CircleImageView) this.findViewById(R.id.iv_avatar);
        tv_name = (TextView) this.findViewById(R.id.ttv_name);
        tv_sex1 = (TextView) this.findViewById(R.id.tv_sex1);
        tv_sex1.setText(sex);
        tv_name.setText(nick);
        titleset = (TextView) this.findViewById(R.id.titleset);
        TextPaint tp = titleset.getPaint();
        tp.setFakeBoldText(true);
        //获取sharedPreferences对象
//        SharedPreferences sharedPreferences = getSharedPreferences("sex", MODE_PRIVATE);
        sp= getSharedPreferences("sex", MODE_PRIVATE);
        String sex = sp.getString("sex", "");

        if (!(sex.equals(""))) {
            tv_sex1.setText(sex);
        }
        showUserAvatar(iv_avatar, vatar_temp);
        back = (ImageView) this.findViewById(R.id.iv_back9);
        back.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_avatar:
                showPhotoDialog();
                break;
            case R.id.re_name:
                startActivityForResult(new Intent(MyUserInfoActivity.this,
                        UpdateNickActivity.class), UPDATE_NICK);
                break;
            case R.id.re_sex:
                showChooseDialog();
                break;
            case R.id.iv_back9:
                ActivityCollector.removeActivity(this);
                finish();
                break;
            default:
                break;
        }
    }


    String[] sexArray = new String[]{"男", "女"};

    /*性别选择*/
    private void showChooseDialog() {
        background=sp.getInt("background",0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(sexArray,background, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获取sharedPreferences对象
                //获取editor对象
                SharedPreferences.Editor editor = sp.edit();//获取编辑器
                //存储键值对
                editor.putString("sex", sexArray[which]);
                editor.putInt("background",which);
                editor.apply();//提交修改
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        response=GetPostUtil.sendGet1111(Constant.URL_updateSex,"id="+Constant.id+"&gender="+
                                sexArray[which]);
                        if ((response != null) && !("".equals(response))) {
                            JSONObject obj2 = JSON.parseObject(response);
                            String msg = obj2.getString("msg");
                            if (msg.equals("success")){
                                Log.i("MyUserInfo","成功");
                            }
                            else{
                                Log.i("MyUserInfo","失败");
                            }

                        }
                    }
                }).start();
                tv_sex1.setText(sexArray[which]);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String nick_temp = LocalUserInfo.getInstance(this)
                .getUserInfo("nick");
        if (!nick_temp.equals(Constant.nick) && !nick_temp.equals("")) {
            tv_name.setText("昵称：" + nick_temp);
        } else {
            tv_name.setText("昵称：" + Constant.nick);
        }
    }

    private void showPhotoDialog() {
        final Dialog dlg = new Dialog(this, R.style.ActionSheetDialogStyle);
        inflate = LayoutInflater.from(this).inflate(R.layout.alertdialog, null);
        dlg.setContentView(inflate);
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
//        window.setContentView(R.layout.alertdialog);
        Display display = getWindowManager().getDefaultDisplay();
        //设置Dialog从窗体底部弹出
        window.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) display.getWidth();
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        window.setAttributes(lp);
        dlg.show();
        ;//显示对话框
        // 为确认按钮添加事件,执行退出应用操作
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("拍照");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
////                        requestPermission();
//                        requestCameraPermission();
//                    }
//                }).start();
                if (ContextCompat.checkSelfPermission(MyUserInfoActivity.this,Manifest.permission.CAMERA)
                        !=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MyUserInfoActivity.this,new String[]
                            {Manifest.permission.CAMERA},1001);
                }else{
                    imageName = getNowTime() + ".jpg";
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File file = new File(Environment.getExternalStorageDirectory()
//                        + "/fanxin/Files/Camera/Image/", String.valueOf(System.currentTimeMillis())
//                        + ".jpg");
                    File file = new File(avaPath, imageName);

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                        imageUri = Uri.fromFile(file);
                    } else {
                        imageUri = FileProvider.getUriForFile(MyUserInfoActivity.this, ProviderUtil.getFileProviderName(MyUserInfoActivity.this), file);
                    }
                    //Intent intent = new Intent(MyUserInfoActivity.this, MyCamera.class);
                    //intent.putExtra("type", 1);
                    // 指定调用相机拍照后照片的储存路径
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(new File(avaPath, imageName)));
//                startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                    dlg.cancel();

                }
               dlg.cancel();

            }
        });
        TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
        tv_xiangce.setText("相册");
        tv_xiangce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                getNowTime();
                imageName = getNowTime() + ".jpg";
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                dlg.cancel();
            }
        });
        TextView tv_quxiao = (TextView) window.findViewById(R.id.tv_content3);
        tv_quxiao.setText("取消");
        tv_quxiao.setTextColor(0xff7f7f7f);
        tv_quxiao.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.cancel();
            }
        });

    }


    @SuppressLint("SdCardPath")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:
                if (resultCode == RESULT_OK) {
//                Uri localUri = Uri.fromFile( new File("/sdcard/fanxin/", imageName));
//                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
//
//                sendBroadcast(localIntent);
                    //picPath = data.getStringExtra("picpath");
                    //Uri uri = Uri.parse(picPath);
                    //picPath = data.getStringExtra("picpath");
//                    startPhotoZoom(Uri.fromFile(new File(avaPath, imageName)), 480);
                    startPhotoZoom(imageUri, 480);
//                    if (resultCode == RESULT_OK) {
//                        if (data != null)
//                            startPhotoZoom(data.getData(), 480);
//                    }
//                    startPhotoZoom(Uri.fromFile(new File(picPath)), 480);
                }
                break;

            case PHOTO_REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    if (data != null)
                        startPhotoZoom(data.getData(), 480);
                }
                break;

            case PHOTO_REQUEST_CUT:
                if (resultCode == RESULT_OK) {
                    // BitmapFactory.Options options = new BitmapFactory.Options();
                    //
                    // /**
                    // * 最关键在此，把options.inJustDecodeBounds = true;
                    // * 这里再decodeFile()，返回的bitmap为空
                    // * ，但此时调用options.outHeight时，已经包含了图片的高了
                    // */
                    // options.inJustDecodeBounds = true;
//                    Bitmap bitmap = BitmapFactory.decodeFile(avaPath
//                            + imageName);
                    Bitmap bitmap = BitmapFactory.decodeFile(avaPath + imageName);
                    iv_avatar.setImageBitmap(bitmap);
                    updateAvatarInServer(imageName);
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    @SuppressLint("SdCardPath")
    private void startPhotoZoom(Uri uri1, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri1, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        //需要加上这两句话：  uri权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", false);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(avaPath,imageName))
//                );
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(avaPath + imageName)));
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    @SuppressLint("SimpleDateFormat")
    private String getNowTime() {
        //Date date = new Date(System.currentTimeMillis());
        //SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSS");
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        // 转换为字符串
        String formatDate = format.format(new Date());
        return formatDate;
    }

    public void back(View view) {
        finish();
    }


    private void showUserAvatar(ImageView iamgeView, String avatar) {
        final String url_avatar = Constant.URL_Avatar + id + "/" + avatar;
        iamgeView.setTag(url_avatar);
        if (avatar != null && !avatar.equals("")) {
            Bitmap bitmap = avatarLoader.loadImage(iamgeView, url_avatar,
                    new ImageDownloadedCallBack() {

                        @Override
                        public void onImageDownloaded(ImageView imageView,
                                                      Bitmap bitmap) {
                            if (imageView.getTag() == url_avatar) {
                                imageView.setImageBitmap(bitmap);

                            }
                        }

                    });
            if (bitmap != null)
                iamgeView.setImageBitmap(bitmap);

        }
    }

    Handler myhandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 111) {
                dialog.dismiss();
                ToastUtils.showShort(MyUserInfoActivity.this, "头像上传成功");
                String devId = SipInfo.paddevId;
                SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                SipInfo.toDev = new NameAddress(devName, sipURL);
                org.zoolu.sip.message.Message query = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                        SipInfo.user_from, BodyFactory.createListUpdate("addsuccess"));
                SipInfo.sipUser.sendMessage(query);
                finish();
            } else if (msg.what == 222) {
                dialog.dismiss();
                ToastUtils.showShort(MyUserInfoActivity.this, "头像上传失败");
//                return;
            }
        }
    };

    @SuppressLint("SdCardPath")
    private void updateAvatarInServer(final String image) {
        dialog = new ProgressDialog(MyUserInfoActivity.this);
        dialog.setMessage("正在更新...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        new Thread() {
            @Override
            public void run() {
                //httpurlconnection简单封装版
                response = GetPostUtil.uploadFile(Constant.URL_UPDATE_Avatar, avaPath + imageName, id,
                        LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("avatar"));

                 /*okhttp的封装可以有选择的使用
                 try {
                 response= OkHttpUtils.post().addFile("pic",imageName,new File(avaPath + imageName)).
                 addParams("id",id).
                 addParams("avatar",LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("avatar")).
                 url(Constant.URL_UPDATE_Avatar).
                 build().
                 execute().body().string();
                 } catch (IOException e) {
                 e.printStackTrace();
                 }*/


                Log.i("jonsresponse", response);
                JSONObject obj = JSON.parseObject(response);
                String msg = obj.getString("msg");
                String tip = obj.getString("tip");
                if (msg.equals("success") && tip.equals("ok")) {
                    String avatarname = obj.getString("avatar");
                    Log.w("qqq.....", "上传后的avatar为" + avatarname);

                    LocalUserInfo.getInstance(MyUserInfoActivity.this)
                            .setUserInfo("avatar", avatarname);

                    File oldfile = new File(avaPath + imageName);
                    File newfile = new File(SdCard + "/fanxin/Files/Camera/Image/" + avatarname);
                    oldfile.renameTo(newfile);
                    //这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(newfile);
                    intent.setData(uri);
                    getApplicationContext().sendBroadcast(intent);
                    myhandle.sendEmptyMessage(111);
                } else {
                    myhandle.sendEmptyMessage(222);
                }
            }
        }.start();


    }

    private void requestCameraPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                    !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]
                        {Manifest.permission.CAMERA},1001);
            }else{

            }
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
            // 向用户解释为什么需要这个权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setMessage("申请相机权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //申请相机权限
                                ActivityCompat.requestPermissions(MyUserInfoActivity.this,
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                            }
                        })
                        .show();
            } else {
                //申请相机权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, "相机权限已申请", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "相机权限已申请", Toast.LENGTH_SHORT).show();
            } else {
                //用户勾选了不再询问
                //提示用户手动打开权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "相机权限已被禁止", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}


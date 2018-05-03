package com.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.app.LoadPicture;
import com.app.LoadPicture.ImageDownloadedCallBack;
import com.app.LocalUserInfo;
import com.app.R;
import com.app.model.Constant;
import com.app.tools.ActivityCollector;
import com.app.utils.GetPostUtil;
import com.app.utils.ToastUtils;
import com.app.view.CircleImageView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.app.model.Constant.id;

@SuppressLint("SdCardPath")
public class MyUserInfoActivity extends Activity {

    private RelativeLayout re_avatar;
    private RelativeLayout re_name;


    private CircleImageView iv_avatar;
    private TextView tv_name;
    private ProgressDialog dialog;
    private static String imageName;
    private String response="";
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private static final int UPDATE_FXID = 4;// 结果
    private static final int UPDATE_NICK = 5;// 结果
    private LoadPicture avatarLoader;
    String hxid;
    String nick;
    String SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
    String avaPath = SdCard + "/fanxin/Files/Camera/Image/";
    private String picPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);
        avatarLoader = new LoadPicture(this, avaPath);
        initView();
        ActivityCollector.addActivity(this);
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
        re_avatar.setOnClickListener(new MyListener());
        re_name.setOnClickListener(new MyListener());
        // 头像
        iv_avatar = (CircleImageView) this.findViewById(R.id.iv_avatar);
        tv_name = (TextView) this.findViewById(R.id.ttv_name);
        tv_name.setText(nick);
        showUserAvatar(iv_avatar, vatar_temp);
        ImageView back = (ImageView) this.findViewById(R.id.iv_back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCollector.finishToMain();
            }
        });
    }

    class MyListener implements OnClickListener {

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
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String nick_temp = LocalUserInfo.getInstance(this)
                .getUserInfo("nick");
        if (!nick_temp.equals(Constant.nick) && nick_temp != null && !nick_temp.equals("")) {
            tv_name.setText("昵称：" + nick_temp);
        } else {
            tv_name.setText("昵称：" + Constant.nick);
        }
    }
    private void showPhotoDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.alertdialog);
        // 为确认按钮添加事件,执行退出应用操作
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("拍照");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                imageName = getNowTime() + ".jpg";
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //Intent intent = new Intent(MyUserInfoActivity.this, MyCamera.class);
                //intent.putExtra("type", 1);
                // 指定调用相机拍照后照片的储存路径
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(avaPath, imageName)));
                startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
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
                    startPhotoZoom(Uri.fromFile(new File(avaPath, imageName)), 480);
                    //startPhotoZoom(Uri.fromFile(new File(picPath)), 480);
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
        final String url_avatar = Constant.URL_Avatar +id+ "/"+avatar;
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
                finish();
            } else if (msg.what == 222) {
                dialog.dismiss();
                ToastUtils.showShort(MyUserInfoActivity.this, "头像上传失败");
                return;
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
                response = GetPostUtil.uploadFile(Constant.URL_UPDATE_Avatar, avaPath + imageName, id,
                        LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("avatar"));
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
}

package com.app.friendCircleMain.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.app.LoadPicture;
import com.app.R;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * 描述:
 * 作者：HMY
 * 时间：2016/5/10
 */
public abstract class NineGridLayout extends ViewGroup {
    PopupMenu popup = null;
    private LoadPicture avatarLoader;
    private static final float DEFUALT_SPACING = 3f;
    private static final int MAX_COUNT = 9;
    String SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
    String avaPath = SdCard + "/fanxin/Files/Camera/Image/";
    protected Context mContext;
    private float mSpacing = DEFUALT_SPACING;
    private int mColumns;
    private int mRows;
    private int mTotalWidth;
    private int mSingleWidth;

    private boolean mIsShowAll = false;
    private boolean mIsFirst = true;
    private List<String> mUrlList = new ArrayList<>();

    public NineGridLayout(Context context) {
        super(context);
        init(context);
    }

    public NineGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NineGridLayout);
        avatarLoader = new LoadPicture(context, avaPath);
        mSpacing = typedArray.getDimension(R.styleable.NineGridLayout_sapcing, DEFUALT_SPACING);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        if (getListSize(mUrlList) == 0) {
            setVisibility(GONE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        System.out.println("测量。。。");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        System.out.println("onlayout。。。");
        mTotalWidth = right - left;
        mSingleWidth = (int) ((mTotalWidth - mSpacing * (3 - 1)) / 3);
        if (mIsFirst) {
            notifyDataSetChanged();
            mIsFirst = false;
        }
    }

    /**
     * 设置间隔
     *
     * @param spacing
     */
    public void setSpacing(float spacing) {
        mSpacing = spacing;
    }

    /**
     * 设置是否显示所有图片（超过最大数时）
     *
     * @param isShowAll
     */
    public void setIsShowAll(boolean isShowAll) {
        mIsShowAll = isShowAll;
    }

    public void setUrlList(List<String> urlList) {
        if (getListSize(urlList) == 0) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);

        mUrlList.clear();
        mUrlList.addAll(urlList);

        if (!mIsFirst) {
            notifyDataSetChanged();
        }
    }

    public void notifyDataSetChanged() {
        post(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    private void refresh() {
        removeAllViews();
        int size = getListSize(mUrlList);
        if (size > 0) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }

        if (size == 1) {
            String url = mUrlList.get(0);
            RatioImageView imageView = createImageView(0, url);

            //避免在ListView中一张图未加载成功时，布局高度受其他item影响
            LayoutParams params = getLayoutParams();
            params.height = mSingleWidth;
            setLayoutParams(params);
            imageView.layout(0, 0, mSingleWidth, mSingleWidth);

            boolean isShowDefualt = displayOneImage(imageView, url, mTotalWidth);
            //boolean isShowDefualt = false;
            if (isShowDefualt) {
                layoutImageView(imageView, 0, url, false);
            } else {

                addView(imageView);
            }
            return ;
        }

        generateChildrenLayout(size);
        layoutParams();

        for (int i = 0; i < size; i++) {
            String url = mUrlList.get(i);
            RatioImageView imageView;
            if (!mIsShowAll) {
                if (i < MAX_COUNT - 1) {
                    imageView = createImageView(i, url);
                    layoutImageView(imageView, i, url, false);
                } else { //第9张时
                    if (size <= MAX_COUNT) {//刚好第9张
                        imageView = createImageView(i, url);
                        layoutImageView(imageView, i, url, false);
                    } else {//超过9张
                        imageView = createImageView(i, url);
                        layoutImageView(imageView, i, url, true);
                        break;
                    }
                }
            } else {
                imageView = createImageView(i, url);
                layoutImageView(imageView, i, url, false);
            }
        }
    }

    private void layoutParams() {
        int singleHeight = mSingleWidth;

        //根据子view数量确定高度
        LayoutParams params = getLayoutParams();
        params.height = (int) (singleHeight * mRows + mSpacing * (mRows - 1));
        setLayoutParams(params);
    }
//显示朋友圈的每一张图片
    private RatioImageView createImageView(final int i, final String url) {
        RatioImageView imageView = new RatioImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> aaa=(ArrayList<String>)mUrlList;
                onClickImage(i, url, aaa);//显示大图

            }
        });

        imageView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                showPopMenu(v);
                popup = new PopupMenu(mContext, v);
                // 将R.menu.popup_menu菜单资源加载到popup菜单中
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                // 为popup菜单的菜单项单击事件绑定事件监听器
                popup.setOnMenuItemClickListener(
                        new PopupMenu.OnMenuItemClickListener()
                        {
                            @Override
                            public boolean onMenuItemClick(MenuItem item)
                            {
                                switch (item.getItemId())
                                {
                                    case R.id.share:
                                        //发送图片url分享图片
                                        String devId = SipInfo.paddevId;
                                        String devName = "pad";
                                        final String devType2 = "2";
                                        SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
                                        SipInfo.toDev = new NameAddress(devName, sipURL);
                                        org.zoolu.sip.message.Message query = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
                                                SipInfo.user_from, BodyFactory.createImageShareNotify(url));
                                        SipInfo.sipUser.sendMessage(query);

                                        break;
                                }
                                return true;
                            }
                        });
                popup.show();

                return false;
            }
        });
        return imageView;
    }

//    private void showShareDialog() {
//        AlertDialog dialog = new AlertDialog.Builder(mContext).create();//创建一个AlertDialog对象
//        dialog.show();
//        Window window = dialog.getWindow();
//        window.setContentView(R.layout.alertdialog1);
//        TextView bt_share = (TextView) findViewById(R.id.tv_share);
////        bt_share.setOnClickListener(new OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Toast.makeText(mContext, "分享照片", Toast.LENGTH_SHORT).show();
////            }
////        });
//    }
//    public void showPopMenu(View view){
//        // 创建PopupMenu对象
//        popup = new PopupMenu(mContext, view);
//        // 将R.menu.popup_menu菜单资源加载到popup菜单中
//        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
//        // 为popup菜单的菜单项单击事件绑定事件监听器
//        popup.setOnMenuItemClickListener(
//                new PopupMenu.OnMenuItemClickListener()
//                {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item)
//                    {
//                        switch (item.getItemId())
//                        {
//                            case R.id.share:
//                                //发送url
//                                String devId = SipInfo.paddevId;
////                devId = devId2.substring(0, devId2.length() - 4).concat("0160");//设备id后4位替换成0160
//                                String devName = "pad";
//                                final String devType2 = "2";
//                                SipURL sipURL = new SipURL(devId, SipInfo.serverIp, SipInfo.SERVER_PORT_USER);
//                                SipInfo.toDev = new NameAddress(devName, sipURL);
//                                org.zoolu.sip.message.Message query = SipMessageFactory.createNotifyRequest(SipInfo.sipUser, SipInfo.toDev,
//                                        SipInfo.user_from, BodyFactory.createImageShareNotify(url));
//                                SipInfo.sipUser.sendMessage(query);
//
//                                break;
//                        }
//                        return true;
//                    }
//                });
//        popup.show();
//    }

//    private void showUserAvatar(ImageView iamgeView, String avatar) {
//        final String url_avatar = Constant.URL_Avatar + avatar;
//        iamgeView.setTag(url_avatar);
//        if (url_avatar != null && !url_avatar.equals("")) {
//            Bitmap bitmap = avatarLoader.loadImage(iamgeView, url_avatar,
//                    new ImageDownloadedCallBack() {
//
//                        @Override
//                        public void onImageDownloaded(ImageView imageView,
//                                                      Bitmap bitmap) {
//                            if (imageView.getTag() == url_avatar) {
//                                imageView.setImageBitmap(bitmap);
//
//                            }
//                        }
//
//                    });
//            if (bitmap != null)
//                iamgeView.setImageBitmap(bitmap);
//
//        }
//    }

    /**
     * @param imageView
     * @param url
     * @param showNumFlag 是否在最大值的图片上显示还有未显示的图片张数
     */
    private void layoutImageView(RatioImageView imageView, int i, String url, boolean showNumFlag) {
        final int singleWidth = (int) ((mTotalWidth - mSpacing * (3 - 1)) / 3);
        int singleHeight = singleWidth;

        int[] position = findPosition(i);
        int left = (int) ((singleWidth + mSpacing) * position[1]);
        int top = (int) ((singleHeight + mSpacing) * position[0]);
        int right = left + singleWidth;
        int bottom = top + singleHeight;

        imageView.layout(left, top, right, bottom);

        addView(imageView);
//        if (showNumFlag) {//添加超过最大显示数量的文本
//            int overCount = getListSize(mUrlList) - MAX_COUNT;
//            if (overCount > 0) {
//                float textSize = 30;
//                final TextView textView = new TextView(mContext);
//                textView.setText("+" + String.valueOf(overCount));
//                textView.setTextColor(Color.WHITE);
//                textView.setPadding(0, singleHeight / 2 - getFontHeight(textSize), 0, 0);
//                textView.setTextSize(textSize);
//                textView.setGravity(Gravity.CENTER);
//                textView.setBackgroundColor(Color.BLACK);
//                textView.getBackground().setAlpha(120);
//
//                textView.layout(left, top, right, bottom);
//                addView(textView);
//            }
//        }
        //showUserAvatar(imageView, url);
        displayImage(imageView,url);

    }

    private int[] findPosition(int childNum) {
        int[] position = new int[2];
        for (int i = 0; i < mRows; i++) {
            for (int j = 0; j < mColumns; j++) {
                if ((i * mColumns + j) == childNum) {
                    position[0] = i;//行
                    position[1] = j;//列
                    break;
                }
            }
        }
        return position;
    }

    /**
     * 根据图片个数确定行列数量
     *
     * @param length
     */
    private void generateChildrenLayout(int length) {
        if (length <= 3) {
            mRows = 1;
            mColumns = length;
        } else if (length <= 6) {
            mRows = 2;
            mColumns = 3;
            if (length == 4) {
                mColumns = 2;
            }
        } else {
            mColumns = 3;
            if (mIsShowAll) {
                mRows = length / 3;
                int b = length % 3;
                if (b > 0) {
                    mRows++;
                }
            } else {
                mRows = 3;
            }
        }

    }

    protected void setOneImageLayoutParams(RatioImageView imageView, int width, int height) {
        imageView.setLayoutParams(new LayoutParams(width, height));
        imageView.layout(0, 0, width, height);

        LayoutParams params = getLayoutParams();
//        params.width = width;
        params.height = height;
        setLayoutParams(params);
    }

    private int getListSize(List<String> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        return list.size();
    }

    private int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.ascent);
    }

    /**
     * @param imageView
     * @param url
     * @param parentWidth 父控件宽度
     * @return true 代表按照九宫格默认大小显示，false 代表按照自定义宽高显示
     */
    protected abstract boolean displayOneImage(RatioImageView imageView, String url, int parentWidth);

    protected abstract void displayImage(RatioImageView imageView, String url);

    protected abstract void onClickImage(int position, String url, ArrayList<String> urlList);
}

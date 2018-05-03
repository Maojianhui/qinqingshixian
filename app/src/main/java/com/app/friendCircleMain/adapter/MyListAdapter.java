package com.app.friendCircleMain.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.LoadPicture;
import com.app.LoadPicture.ImageDownloadedCallBack;
import com.app.R;
import com.app.friendCircleMain.domain.FirendMicroListDatas;
import com.app.friendCircleMain.domain.FirstMicroListDatasFirendcomment;
import com.app.friendCircleMain.domain.FirstMicroListDatasFirendimage;
import com.app.friendCircleMain.domain.FirstMicroListDatasFirendpraise;
import com.app.friendCircleMain.util.MyCustomDialog;
import com.app.model.Constant;
import com.app.view.CircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static android.media.CamcorderProfile.get;
import static com.app.R.id.view;


public class MyListAdapter extends BaseAdapter {
    private static String avatar;
    private static String id;
    private LoadPicture avatarLoader;
    String SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
    String avaPath = SdCard + "/fanxin/Files/Camera/Image/";
    private static final String TAG = "MyListAdapter";
    private LayoutInflater mInflater;
    private Context mContext;//上下文
    String replyid;//回复人id
    String replyname;//回复人姓名
    private boolean praise = false;//是否已经点赞了   true:已经点赞了，这样textView上面应该显示“取消”；false:没有点赞，textView上面应该显示“点赞”；默认为false
    private int[] picUrl;//图片地址1
    private String[] expressionAllImgNames;//图片名1
    // 定义操作面板状态常量
    public static final int PANEL_STATE_GONE = 0;
    public static final int PANEL_STATE_VISIABLE = 1;
    //操作面板状态
    public static int panelState = PANEL_STATE_GONE;
    private List<FirendMicroListDatas> mList = new ArrayList<FirendMicroListDatas>();//json数据
    private FirendMicroListDatas bean = new FirendMicroListDatas();//总的实体类
    private List<FirstMicroListDatasFirendimage> fImage = new ArrayList<FirstMicroListDatasFirendimage>();//图片
    private List<FirstMicroListDatasFirendcomment> fConnent = new ArrayList<FirstMicroListDatasFirendcomment>();//评论
    private List<FirstMicroListDatasFirendpraise> friendpraise = new ArrayList<FirstMicroListDatasFirendpraise>();//点赞
    private FirstMicroListDatasFirendcomment f = new FirstMicroListDatasFirendcomment();//评论完了暂时存到这里

    private static String[] mUrls = new String[9];
    private static List<String> list9 = new ArrayList<>();
    String postid = "";//post表示消息的id
    String sImages = "";
    int indexOf = -1;
    private String praiseflag = "";//点赞标示，判断这个人有没有点过

    public MyListAdapter(Context context, List<FirendMicroListDatas> list) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        this.mList = list;
        avatarLoader = new LoadPicture(mContext, avaPath);
    }

//	public void notifyDataSetChangedEx(List<FirendMicroListDatas> mLists){
//		this.mList.clear();
//		this.mList=mLists;
//
//		mContext.mAdapter.notifyDataSetChanged();
//	}

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public FirendMicroListDatas getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt(getItem(position).getId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.micro_list_item, null);
            holder = new ViewHolder();
            holder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            holder.layoutParise = (LinearLayout) convertView.findViewById(R.id.layoutParise);
            holder.layout01 = (LinearLayout) convertView.findViewById(R.id.layout01);
            holder.layout9 = (NineGridTestLayout) convertView.findViewById(R.id.layout_nine_grid);//九宫格图片
            holder.liearLayoutIgnore = (LinearLayout) convertView.findViewById(R.id.liearLayoutIgnore);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.view = (TextView) convertView.findViewById(view);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.avator = (CircleImageView) convertView.findViewById(R.id.avator);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.btnIgnore = (Button) convertView.findViewById(R.id.btnIgnore);
            holder.btnComment = (Button) convertView.findViewById(R.id.btnComment);
            holder.btnPraise = (Button) convertView.findViewById(R.id.btnPraise);
//			holder.btnComment.setTag(position);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.btnComment.setTag(getItem(position).getPostid());
        //holder.btnPraise.setTag(getItem(position).getPraiseflag());//点赞标示，用来判断是否点过
        bean = getItem(position);//总的实体类
        fImage = bean.getPost_pic();//图片
        //fConnent=bean.getFriendcomment();//评论
        //friendpraise=bean.getFriendpraise();//点赞
//		}	

		
		/*
		 * 显示时间
		 * 服务器返回的时间是：年-月-日 时：分，所以获取的时候应该是yyyy-MM-dd HH:mm
		 */
        String strTime = bean.getCreate_time().trim();
//		Log.i(TAG, "服务器传过来的时间"+strTime);
        if (!"".equals(strTime)) {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date = sDateFormat.format(new java.util.Date());
//			Log.i(TAG, "手机当前的时间"+date);
            String t = getTimes(date, strTime);
            Log.i(TAG, "时间差" + t);
            holder.time.setText(t);
        }
		/*
		 * 显示头像
		 */
        avatar = bean.getAvatar();
        id = bean.getId();
        //showUserAvatar(holder.avator, avatar);
        DisplayImageOptions options = new DisplayImageOptions.Builder()//
                .showImageOnLoading(R.drawable.empty_photo) // 加载中显示的默认图片
                .showImageOnFail(R.drawable.empty_photo) // 设置加载失败的默认图片
                .cacheInMemory(true) // 内存缓存
                .cacheOnDisk(true) // sdcard缓存
                .build();//
        ImageLoader.getInstance().displayImage(Constant.URL_Avatar + id + "/" + avatar, holder.avator, options);

		/*
		 * 显示姓名和内容
		 */
        holder.name.setText(bean.getNickname());//姓名
		/*
		 * 显示图片
		 */
        if (fImage.size() == 0) {
            Log.w("111111.........", "null");
        }
        if (fImage.size() != 0) {
            for (int i = 0; i < fImage.size(); i++) {
                mUrls[i] = Constant.URL_Avatar + id + "/" + fImage.get(i).getPic_name().toString();
                //list9.add(fImage.get(i).getPic_name().toString());
                list9.add(Constant.URL_Avatar + id + "/" + fImage.get(i).getPic_name().toString());
                Log.w("111111.........", mUrls[i].toString());
            }
        }

        holder.layout9.setIsShowAll(true);
        holder.layout9.setUrlList(list9);
        list9.clear();
//				if(null!=f.getId()){
//		for (int i = 0; i < aa.length(); i++) {//循环json数组
//			JSONObject ob  = (JSONObject) array.get(i);//得到json对象
//			String  name= ob.getString("name");//name这里是列名称，获取json对象中列名为name的值
        //加载内容（文字和表情）
        String strExpression = bean.getContent();
        holder.content.setText(strExpression);//如果要表情的话，把这个去掉，然后把下面的加上就行了
		/*
		 * 引入表情
		expressionAllImgs = Expressions.expressionAllImgs;
		expressionAllImgNames = Expressions.expressionAllImgNames;
		int i=0;
		String c="";
		String s="";
		Bitmap bitmap = null;
		holder.content.setText("");
		if(UtilTool.isProperHTML(strExpression)){
			holder.content.append(Html.fromHtml(strExpression));
		}
		while(i<strExpression.length()){
			c=strExpression.substring(i, i+1);
			if("[".equals(c)){
				s=strExpression.substring(i, i+7);
				for(int j=0;j<expressionAllImgNames.length;j++){
					if(s.equals(expressionAllImgNames[j])){
						i+=7;
						bitmap = null;
						bitmap = BitmapFactory.decodeResource(mContext.getResources(),
								expressionAllImgs[j % expressionAllImgs.length]);
						ImageSpan imageSpan = new ImageSpan(mContext, bitmap);
						SpannableString spannableString = new SpannableString(
								expressionAllImgNames[j]);
						spannableString.setSpan(imageSpan, 0,
								expressionAllImgNames[j].length() ,
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						// 编辑框设置数据
						
						holder.content.append(spannableString);
					}
				}
			}else{
				i++;
				holder.content.append(Html.fromHtml(c));
			}
		}
		*/

        //显示评论、点赞按钮
        holder.btnIgnore.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                postid = holder.btnComment.getTag().toString();
                //praiseflag=holder.btnPraise.getTag().toString();
                praiseflag = "N";
                if ("Y".equals(praiseflag)) {
                    praise = true;
                    holder.btnPraise.setText("取消");
                } else if ("N".equals(praiseflag)) {
                    praise = false;
                    holder.btnPraise.setText("点赞");
                }

                if (1 == panelState) {
                    panelState = PANEL_STATE_GONE;
                    switchPanelState(holder.liearLayoutIgnore, holder.btnComment, holder.btnPraise);
                } else {
                    panelState = PANEL_STATE_VISIABLE;
                    switchPanelState(holder.liearLayoutIgnore, holder.btnComment, holder.btnPraise);
                }
            }
        });

        //评论按钮
        holder.btnComment.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                //显示评论的对话框
                MyCustomDialog dialog = new MyCustomDialog(mContext, R.style.add_dialog, "评论" + bean.getNickname() + "的说说", new MyCustomDialog.OnCustomDialogListener() {
                    //点击对话框'提交'以后
                    public void back(String content) {
                        //先隐藏再提交评论
                        panelState = PANEL_STATE_GONE;
                        switchPanelState(holder.liearLayoutIgnore, holder.btnComment, holder.btnPraise);
                        submitComment(bean.getId(), bean.getNickname(), content);//提交评论
                    }
                });
                dialog.setCanceledOnTouchOutside(true);//设置点击Dialog外部任意区域关闭Dialog
                dialog.show();
            }
        });

        //点赞按钮       praise:是否已经点赞了   true:已经点赞了，这样textView上面应该显示“取消”；false:没有点赞，textView上面应该显示“点赞”；默认为false
        holder.btnPraise.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                //先隐藏再提交评论
                panelState = PANEL_STATE_GONE;
                switchPanelState(holder.liearLayoutIgnore, holder.btnComment, holder.btnPraise);
                submitPraise(bean.getId(), bean.getNickname());//提交赞
            }
        });

        //显示点赞holder.layoutParise   friendpraise
//		holder.layoutParise.removeAllViews();
//		holder.view.setVisibility(View.GONE);
//		holder.layout01.setVisibility(View.GONE);
//		if(0!=friendpraise.size()){//有数据，控件显示
//			holder.layout01.setVisibility(View.VISIBLE);
//			holder.layoutParise.setVisibility(View.VISIBLE);
//
//			LinearLayout ll=new LinearLayout(mContext);
//			ll.setOrientation(LinearLayout.HORIZONTAL);
//			ll.layout(3, 3, 3, 3);
//
//			ImageView i1=new ImageView(mContext);
//			i1.setBackgroundResource(R.drawable.micro_praise_button);
//			i1.setLayoutParams(new LayoutParams(20,18));
//			TextView t2=new TextView(mContext);
//			t2.setTextColor(0xff2C78B8);
//			t2.setTextSize(11);
//			ll.addView(i1);
//
//			StringBuffer uName=new StringBuffer();
//			uName.append(" ");
//			for(FirstMicroListDatasFirendpraise p:friendpraise){
//				if(null!=p){
//					uName.append(p.getUname()+" ,");
//				}
//			}
//			uName.deleteCharAt(uName.length()-1);
//			t2.setText(uName);
//			ll.addView(t2);
//			holder.layoutParise.addView(ll);
//		}

        //显示评论
//		holder.layout.removeAllViews();
//		if(0!=fConnent.size()){
//			holder.layout01.setVisibility(View.VISIBLE);
//			holder.layout.setVisibility(View.VISIBLE);
//			if(0!=friendpraise.size()){
//				holder.view.setVisibility(View.VISIBLE);
//			}
//			for(FirstMicroListDatasFirendcomment f:fConnent){
//				if(null!=f.getId()){
//					LinearLayout ll=new LinearLayout(mContext);
//					ll.setOrientation(LinearLayout.HORIZONTAL);
//					ll.layout(3, 3, 3, 3);
//					TextView t1=new TextView(mContext);
//					TextView t2=new TextView(mContext);
//					t1.setText(" "+f.getReplyName()+":");
//					t1.setTextColor(0xff2C78B8);
//					t1.setTextSize(13);
//					t2.setTextSize(13);
//					t2.setText(f.getComment());
//					ll.addView(t1);
//					ll.addView(t2);
//					holder.layout.addView(ll);
//				}
//			}
//		}
        return convertView;
    }

    private void showUserAvatar(ImageView iamgeView, String avatar) {
        final String url_avatar = Constant.URL_Avatar + avatar;
        iamgeView.setTag(url_avatar);
        if (url_avatar != null && !url_avatar.equals("")) {
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

    /**
     * 提交评论
     * replyid; 回复人id
     * replyname; 回复人姓名
     *
     * @param isreplyid   被回复人ID
     * @param isreplyname 被回复人姓名
     * @param content     评论内容
     */
    private void submitComment(final String isreplyid, final String isreplyname, final String content) {
        // TODO Auto-generated method stub
        Toast.makeText(mContext, "提交评论", 0).show();
    }

    /**
     * 点赞
     * praise:是否已经点赞了   true:已经点赞了，这样textView上面应该显示“取消”；false:没有点赞，textView上面应该显示“点赞”；默认为false
     *
     * @param 被点赞人sid        消息主键
     * @param 被点赞人companykey 公司标识位
     * @param uid            点赞人用户ID
     * @param uname          被点赞人用户名
     */
    private void submitPraise(String uid, String uname) {
        // TODO Auto-generated method stub
        Toast.makeText(mContext, "点赞/取消点赞", 0).show();
    }

    /**
     * 评论点赞，隐藏显示
     * 操作面板显示状态
     */
    private void switchPanelState(LinearLayout liearLayoutIgnore, Button btnComment, Button btnPraise) {
        // TODO Auto-generated method stub
        switch (panelState) {
            case PANEL_STATE_GONE:

                liearLayoutIgnore.setVisibility(View.GONE);
                btnComment.setVisibility(View.GONE);
                btnPraise.setVisibility(View.GONE);
                break;
            case PANEL_STATE_VISIABLE:
//			holder.liearLayoutIgnore.startAnimation(animation);//评论的显示动画
                liearLayoutIgnore.setVisibility(View.VISIBLE);
                btnComment.setVisibility(View.VISIBLE);
                btnPraise.setVisibility(View.VISIBLE);

                break;
        }
    }

    /**
     * 仿qq或微信的时间显示
     * 时间比较
     * date 当前时间
     * strTime 获取的时间
     */
    private String getTimes(String date, String strTime) {
        // TODO Auto-generated method stub
        String intIime = "";
        long i = -1;//获取相差的天数
        long i1 = -1;//获取相差的小时
        long i2 = -1;//获取相差的分
        long i3 = -1;//获取相差的
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            ParsePosition pos = new ParsePosition(0);
            ParsePosition pos1 = new ParsePosition(0);
            Date dt1 = formatter.parse(date, pos);
            Date dt2 = formatter.parse(strTime, pos1);
            long l = dt1.getTime() - dt2.getTime();

            i = l / (1000 * 60 * 60 * 24);//获取的如果是0，表示是当天的，如果>0的话是以前发的
            if (0 == i) {//今天发的
                i1 = l / (1000 * 60 * 60);
                if (0 == i1) {//xx分之前发的
                    i2 = l / (1000 * 60);
                    if (0 == i2) {//xx秒之前发的
                        i3 = l / (1000);
                        intIime = i3 + "秒钟以前";
                    } else {
                        intIime = i2 + "分钟以前";
                    }
                } else {
                    intIime = i1 + "小时以前";//xx小时之前发的
                }

            } else {//以前发的
                intIime = i + "天以前";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return intIime;
    }

    private static class ViewHolder {
        public TextView name, text, view, time;
        public CircleImageView avator;
        public Button btnIgnore, btnComment, btnPraise;
        public TextView content;
        public LinearLayout liearLayoutIgnore, layout, layoutParise, layout01;
        public NineGridTestLayout layout9;
    }
}

package com.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.R;
import com.app.model.Constant;
import com.app.model.Friend;
import com.app.ui.ChatActivity;
import com.app.ui.PhoneCall;
import com.app.view.CircleImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author chzjy
 * Date 2016/12/19.
 */

public class FriendAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Friend> list;
private String avatar;
    private String id;
    public FriendAdapter(ArrayList<Friend> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_friendlist, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
      avatar=list.get(position).getAvatar();
        id=list.get(position).getId();

        Glide.with(mContext).load(Constant.URL_Avatar+id+"/"+avatar).error(R.drawable.empty_photo).into(holder.devIcon);
        holder.check.setVisibility(View.INVISIBLE);
            holder.check.setImageResource(R.drawable.icon_btncall);

        holder.devName.setText(list.get(position).getNickName());
        holder.l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Friend friend=list.get(position);
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("friend", friend);
                Constant.currentfriendavatar=friend.getAvatar();
                Constant.currentfriendid=friend.getId();
                mContext.startActivity(intent);
            }
        });
        holder.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNum = list.get(position).getPhoneNum();
                PhoneCall.actionStart(mContext, phoneNum, 1);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.devIcon)
        CircleImageView devIcon;
        @Bind(R.id.devName)
        TextView devName;
        @Bind(R.id.check)
        ImageView check;
        @Bind(R.id.l1)
        LinearLayout l1;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

package com.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.R;
import com.app.model.Addressitem;
import com.app.model.MessageEvent;
import com.app.sip.SipInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.app.sip.SipInfo.addressList;

/**
 * Created by maojianhui on 2019/3/21.
 */

public class AddressItemAdapter extends RecyclerView.Adapter<AddressItemAdapter.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private List<Addressitem>  mAddressList;
    Object obj = new Object();
    static class ViewHolder extends RecyclerView.ViewHolder{
        View addressView;
        RelativeLayout rlUserAddress;
        ImageView addressEdit;
        TextView userAddress;
        TextView detailAddress;
        TextView userName;
        TextView userPhoneNum;
        TextView isDefault;
        public ViewHolder(View v){
            super(v);
            addressView=v;
            rlUserAddress=(RelativeLayout)v.findViewById(R.id.rl_userAddress);
            addressEdit=(ImageView)v.findViewById(R.id.iv_addressEdit);
            userAddress=(TextView)v.findViewById(R.id.tv_userAddress);
            detailAddress=(TextView)v.findViewById(R.id.tv_detailAddress);
            userName=(TextView)v.findViewById(R.id.tv_userName);
            userPhoneNum=(TextView)v.findViewById(R.id.tv_userPhone);
            isDefault=(TextView)v.findViewById(R.id.tv_isdefault);

        }
    }

    public AddressItemAdapter(Context context,List<Addressitem> addressList){
        mLayoutInflater=LayoutInflater.from(context);
        mAddressList=addressList;
    }


    public void appendData(List<Addressitem> address) {
        synchronized (obj) {
            if (address.isEmpty()) return;
            mAddressList.clear();
            mAddressList.addAll(address);
            notifyDataSetChanged();
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= mLayoutInflater.from(parent.getContext())
                .inflate(R.layout.addressitem,parent,false);
        final AddressItemAdapter.ViewHolder holder=new AddressItemAdapter.ViewHolder(view);
        holder.addressEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SipInfo.listPosition=holder.getAdapterPosition();
//                SipInfo.addressPosition=position+1;
                Addressitem addressitem=addressList.get(SipInfo.listPosition);
                SipInfo.addressPosition=addressitem.getPosition();
                SipInfo.isDefault=addressitem.getIsDefault();
//                Toast.makeText(v.getContext(),"you clicked address:  "+
//                        addressitem.getUserName()+"id是    "+position,Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new MessageEvent("编辑"));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(AddressItemAdapter.ViewHolder holder, int position) {
        Addressitem addressitem=mAddressList.get(position);
        holder.userAddress.setText(addressitem.getUserAddress());
        holder.detailAddress.setText(addressitem.getDetailAddress());
        holder.userName.setText(addressitem.getUserName());
        holder.userPhoneNum.setText(changePhoneNum(addressitem.getUserPhoneNum()));
        holder.isDefault.setText(textChange(addressitem.getIsDefault()));
    }

    @Override
    public int getItemCount() {
        return mAddressList.size();
    }
    public String textChange(int isdefault){
        String state="";
        if(isdefault==1){
            state=" 默认 ";
        }else if(isdefault==2){
            state="";
        }
        return state;
    }

    public static String changePhoneNum(String mobile) {
        String maskNumber = mobile.substring(0, 3) + "****" + mobile.substring(7, mobile.length());
        return maskNumber;
    }
}

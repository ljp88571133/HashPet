package com.debla.hashpet.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.debla.hashpet.Model.User;
import com.debla.hashpet.R;
import com.debla.hashpet.Utils.AppContext;
import com.debla.hashpet.Utils.SelectDialog;
import com.debla.hashpet.activities.ConsoleActivity;
import com.debla.hashpet.activities.LoginActivity;
import com.debla.hashpet.activities.NewSellerActivity;
import com.debla.hashpet.activities.PetCircleActivity;
import com.debla.hashpet.activities.ShopCarActivity;
import com.debla.hashpet.activities.ShowOrderActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Dave-PC on 2017/10/27.
 */

public class MineFragment extends Fragment{
    private TextView tvNickName;        //昵称标签
    public static LoginReceiver broadcastReceiver;    //广播接收
    private LinearLayout onIamSaler;
    private AppContext appContext;
    private View mRootView;
    private User user;

    private Unbinder unbinder;

    @BindView(R.id.mine_shopcar)
    LinearLayout shopcar;

    @BindView(R.id.mine_myorder)
    LinearLayout myorder;

    @BindView(R.id.mine_petcircle)
    LinearLayout petCircle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mRootView != null){
            return mRootView;
        }
        mRootView = inflater.inflate(R.layout.mine_fragment,container,false);
        ButterKnife.bind(getActivity());
        tvNickName = (TextView) mRootView.findViewById(R.id.text_nickname);
        onIamSaler = (LinearLayout) mRootView.findViewById(R.id.mine_onIamSaler);
        appContext =(AppContext)getActivity().getApplicationContext();
        onIamSaler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=null;
                User globle_user = appContext.getUser();
                if(globle_user==null){
                    Toast.makeText(getContext(),"您还未登陆，请先登录",Toast.LENGTH_SHORT).show();
                    intent = new Intent(getContext(),LoginActivity.class);
                }else {
                    if("0".equals(globle_user.getUsertype())) {
                        Toast.makeText(getContext(),"您还没有创建店铺，请先注册店铺",Toast.LENGTH_SHORT).show();
                        intent = new Intent(getContext(), NewSellerActivity.class);
                    }
                    else if("1".equals(globle_user.getUsertype()))
                        intent = new Intent(getContext(), ConsoleActivity.class);
                }
                startActivity(intent);
            }
        });
        unbinder = ButterKnife.bind(this, mRootView);
        shopcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ShopCarActivity.class);
                startActivity(intent);
            }
        });
        myorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                user = appContext.getUser();
                if(user==null){
                    Toast.makeText(getContext(),"您需要登陆才能查看订单",Toast.LENGTH_SHORT).show();
                    intent = new Intent(getContext(),LoginActivity.class);
                }else {
                    intent = new Intent(getContext(), ShowOrderActivity.class);
                }
                startActivity(intent);
            }
        });
        petCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PetCircleActivity.class);
                startActivity(intent);
            }
        });
        user = appContext.getUser();
        if(user!=null)
            tvNickName.setText(user.getNickname());
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View me_top = getActivity().findViewById(R.id.mine_top);
        me_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = appContext.getUser();
                if(user==null) {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                }else{
                    List<String> names = new ArrayList<>();
                    names.add("退出用户");
                    showDialog(new SelectDialog.SelectDialogListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            user = null;
                            appContext.setUser(user);
                            Map map = appContext.getInnerMap();
                            map.clear();
                            appContext.setInnerMap(map);
                            tvNickName.setText("请先登录");
                        }
                    },names);
                }
            }
        });

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LoginActivity.action);
        broadcastReceiver = new LoginReceiver(new Handler());
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }


    private SelectDialog showDialog(SelectDialog.SelectDialogListener listener, List<String> names) {
        SelectDialog dialog = new SelectDialog(getActivity(), R.style.transparentFrameWindowStyle, listener, names);
        dialog.show();
        return dialog;
    }

    /**
     * 登陆广播接收通知修改昵称
     */
    class LoginReceiver extends BroadcastReceiver{
        private final Handler handler;

        public LoginReceiver(Handler handler){
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            if(intent.getExtras().getBundle("data")!=null){
                User user =(User) intent.getExtras().getBundle("data").getSerializable("user");
                if(user!=null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            User user =(User) intent.getExtras().getBundle("data").getSerializable("user");
                            appContext.setUser(user);
                            tvNickName.setText(user.getNickname());
                        }
                    });
                }
            }
        }
    }



}

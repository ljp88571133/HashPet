package com.debla.hashpet.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.debla.hashpet.Model.BaseJsonObject;
import com.debla.hashpet.Model.Order;
import com.debla.hashpet.Model.OrderItem;
import com.debla.hashpet.Model.SellerInfo;
import com.debla.hashpet.Model.User;
import com.debla.hashpet.R;
import com.debla.hashpet.Utils.AppContext;
import com.debla.hashpet.Utils.HttpUtil;
import com.debla.hashpet.Utils.ImageLoader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Dave-PC on 2018/4/21.
 */

public class OrderManageActiity extends Activity{

    @BindView(R.id.show_order_list)
    ListView orderListView;

    private AppContext appContext;
    private SellerInfo seller;
    private User user;
    private String url;
    private OrderAdapter adapter;
    public ImageLoader imageLoader; //用来下载图片的类
    private List<Order> orders;

    private String ORDER_STATUS [] = {"已付款","已发货","已完成"};
    private String ORDER_OPTION [] = {"发货","查看","查看"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order);
        ButterKnife.bind(this);
        initData();
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            url = HttpUtil.getUrl(getApplicationContext())+"getGoodsImage";
            orders = (List<Order>) msg.obj;
            if(orders==null||orders.size()==0) {
                Toast.makeText(OrderManageActiity.this, "您当前还未有任何订单可以处理", Toast.LENGTH_SHORT).show();
                return false;
            }
            if(adapter==null) {
                adapter = new OrderAdapter(url);
                orderListView.setAdapter(adapter);
            }else{
                adapter.notifyDataSetChanged();
            }
            return false;
        }
    });

    /**
     * 获取订单数据
     */
    public void initData(){
        appContext = (AppContext) getApplicationContext();
        user = appContext.getUser();
        seller = (SellerInfo)appContext.getInnerMap().get("seller");
        HttpUtil client = new HttpUtil();
        String url = HttpUtil.getUrl(getApplicationContext());
        Map<String,String> params = new HashMap<>();
        params.put("sellerId",String.valueOf(seller.getShopId()));
        imageLoader = new ImageLoader(getApplicationContext());
        client.postRequest(url + "/showSellerOreder", params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("debug","获取订单信息失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Gson gson = new Gson();
                Type jsonType = new TypeToken<BaseJsonObject<List<Order>>>(){}.getType();
                BaseJsonObject<List<Order>> jsonObject = null;
                jsonObject =  gson.fromJson(json, jsonType);
                List<Order> orders = jsonObject.getResult();
                Message msg = new Message();
                msg.obj=orders;
                handler.sendMessage(msg);
            }
        });
    }



    public class OrderAdapter extends BaseAdapter implements View.OnClickListener{

        public ImageLoader imageLoader; //用来下载图片的类
        private String url;

        public OrderAdapter(String imgUrl){
            imageLoader = new ImageLoader(getApplicationContext());
            url = imgUrl;
        }


        @Override
        public int getCount() {
            return orders.size();
        }

        @Override
        public Object getItem(int position) {
            if(position==-1)
                position=0;
            return orders.get(position);
        }

        @Override
        public long getItemId(int position) {
            if(position==-1)
                position=0;
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v;
            if(convertView==null){
                v = View.inflate(OrderManageActiity.this,R.layout.order_list_item,null);
            }else{
                v=convertView;
            }
            TextView tv_name = (TextView) v.findViewById(R.id.order_item_name);
            TextView tv_num = (TextView) v.findViewById(R.id.order_item_num);
            TextView tv_addr = (TextView) v.findViewById(R.id.order_item_address);
            TextView tv_time = (TextView) v.findViewById(R.id.order_item_time);
            TextView tv_order_stuatus = (TextView)v.findViewById(R.id.order_item_status);
            TextView tv_order_price = (TextView) v.findViewById(R.id.show_order_price);
            Button btn_option = (Button)v.findViewById(R.id.order_item_option);
            ImageView srcImage = (ImageView) v.findViewById(R.id.show_order_img);
            Integer proId = orders.get(position).getItems().get(0).getOrdProId();
            imageLoader.DisplayImage(url+"?goodsId="+proId,srcImage);
            tv_name.setText(String.valueOf(orders.get(position).getOrdGoodsName()));
            tv_order_price.setText("￥"+orders.get(position).getOrdPrice());
            tv_num.setText("单号："+orders.get(position).getOrdNum());
            tv_addr.setText("地址："+orders.get(position).getOrdAddr());
            tv_time.setText("日期："+orders.get(position).getOrdTime());
            tv_order_stuatus.setText(ORDER_STATUS[orders.get(position).getOrdStatus()]);
            btn_option.setText(ORDER_OPTION[orders.get(position).getOrdStatus()]);
            btn_option.setTag(position);
            btn_option.setOnClickListener(this);
            return v;
        }

        @Override
        public void onClick(View v) {
            final int position = (Integer)v.getTag();
            int order_status = orders.get(position).getOrdStatus();
            switch(order_status){
                case 0:
                    AlertDialog.Builder sendOffDialog =
                            new AlertDialog.Builder(OrderManageActiity.this);
                    final View dialogView = LayoutInflater.from(OrderManageActiity.this)
                            .inflate(R.layout.dialog_sendoff,null);
                    sendOffDialog.setTitle("填写发货信息");
                    sendOffDialog.setView(dialogView);
                    sendOffDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppContext context = (AppContext) getApplicationContext();
                            String url = HttpUtil.getUrl(context);
                            HttpUtil client = new HttpUtil();
                            Map params = new HashMap();
                            String ordNum = orders.get(position).getOrdNum();
                            params.put("orderNum",ordNum);
                            client.postRequest(url + "/sendOffOrder", params, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e("debug","发货失败");
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    Log.e("debug","发货成功");
                                    initData();
                                }
                            });

                        }
                    });

                    sendOffDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    sendOffDialog.show();
                    break;
                default:
                    Intent intent = new Intent(OrderManageActiity.this,ShowOrderDetailActivity.class);
                    List<OrderItem> items =  orders.get(position).getItems();
                    intent.putExtra("items",(Serializable) items);
                    intent.putExtra("ordNum",orders.get(position).getOrdNum());
                    intent.putExtra("address",orders.get(position).getOrdAddr());
                    startActivity(intent);
            }
        }
    }
}

package com.debla.hashpet.fragments;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.debla.hashpet.Model.BaseJsonObject;
import com.debla.hashpet.Model.ProductSellDetail;
import com.debla.hashpet.Model.SellerInfo;
import com.debla.hashpet.R;
import com.debla.hashpet.Utils.HttpUtil;
import com.debla.hashpet.activities.ProDetailActivity;
import com.debla.hashpet.activities.ShowGoodsResultActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Dave-PC on 2017/10/25.
 */

public class IndexFragment extends Fragment implements View.OnClickListener{
    private ViewPager mAdvViewPager;
    private PagerAdapter mAdapter;
    private static final int MAX_VIEW = 10000;
    private int mCount = 5000;
    //private Handler mHandler = new Handler();

    private SearchView searchView;

    private List<ImageView> mViews = new ArrayList<ImageView>();

    private int mImageArrays[] = {R.drawable.shop_parent2, R.drawable.shop_parent3,R.drawable.zhuliang1,
                            R.drawable.jx_header_5};

    // 开始
    public static final int START = -1;
    // 停止
    public static final int STOP = -2;
    // 更新
    public static final int UPDATE = -3;
    // 接受传过来的当前页面数
    public static final int RECORD = -4;

    //接受首页推荐商品信息
    public static final int RECOMMAND = 1;

    private Drawable [] mDrawable = new Drawable[3];


    private View mRootView;

    private Gson gson;

    private List<ProductSellDetail> data_list;

    private ImageView[] indexImage = new ImageView[3];
    private TextView[] indexTitle = new TextView[3];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (mRootView != null) {
            return mRootView;
        }
        mRootView = inflater.inflate(R.layout.index_fragment, container, false);
        searchView = (SearchView) mRootView.findViewById(R.id.index_search);
        initAdvView(mRootView);
        initEvent();
        initDataList();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getContext(),ShowGoodsResultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("proName",query);
                intent.putExtra("params",bundle);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return mRootView;
    }


    /**
     * 处理轮播事件
     */
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case START:
                    handler.sendEmptyMessageDelayed(UPDATE, 3000);
                    break;
                case STOP:
                    handler.removeMessages(UPDATE);
                    break;
                case UPDATE:
                    mCount++;
                    Log.e("debug","mCount:"+mCount);
                    mAdvViewPager.setCurrentItem(mCount);
                    break;
                case RECORD:
                    mCount = msg.arg1;
                    break;
                case RECOMMAND:
                    initImage();
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 加载广告轮播图片
     */
    public void initAdvView(View view) {
        mAdvViewPager = (ViewPager) view.findViewById(R.id.adv_viewpage);
        //lv_index = (IndexListView) view.findViewById(R.id.index_lv);
        ImageView imageView1 = new ImageView(getContext());
        ImageView imageView2 = new ImageView(getContext());
        ImageView imageView3 = new ImageView(getContext());
        ImageView imageView4 = new ImageView(getContext());
        imageView1.setBackgroundResource(R.drawable.shop_parent2);
        imageView2.setBackgroundResource(R.drawable.shop_parent3);
        imageView3.setBackgroundResource(R.drawable.zhuliang1);
        imageView4.setBackgroundResource(R.drawable.jx_header_5);
        mViews.add(imageView1);
        mViews.add(imageView2);
        mViews.add(imageView3);
        mViews.add(imageView4);
        mAdapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return MAX_VIEW;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViews.get(position % mViews.size()));
            }


            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                int i = position % mViews.size();
                // 预防负值
                position = Math.abs(i);
                ImageView imageView = mViews.get(position);
                ViewParent parent = imageView.getParent();
                // remove掉View之前已经加到一个父控件中，否则报异常
                if (parent != null) {
                    ViewGroup group = (ViewGroup) parent;
                    group.removeView(imageView);
                }
                container.addView(imageView);
                return imageView;
            }


            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };
        mAdvViewPager.setAdapter(mAdapter);
        int i = MAX_VIEW / 2 % mViews.size();
        // 默认在中间，让用户看不到边界
        mAdvViewPager.setCurrentItem(MAX_VIEW / 2 - i);
        //mAdvViewPager.setOffscreenPageLimit(4);
        handler.sendEmptyMessage(START);
        //setListViewHeightBasedOnChildren(lv_index);

        indexImage[0] = (ImageView) view.findViewById(R.id.index_rec_one);
        indexImage[1] = (ImageView) view.findViewById(R.id.index_rec_two);
        indexImage[2] = (ImageView) view.findViewById(R.id.index_rec_three);
        indexTitle[0] = (TextView) view.findViewById(R.id.index_title_one);
        indexTitle[1] = (TextView) view.findViewById(R.id.index_title_two);
        indexTitle[2] = (TextView) view.findViewById(R.id.index_title_three);
    }


    /**
     * 设置图片轮播
     */
    public void initEvent() {
        //mCount = mViews.size();
        mAdvViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.e("debug","第"+position+"页");
            }

            @Override
            public void onPageSelected(int position) {
                handler.sendMessage(Message.obtain(handler, RECORD, position, 0));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    // 当滑动时让当前轮播停止
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        handler.sendEmptyMessage(STOP);
                        break;
                    // 滑动停止时继续轮播
                    case ViewPager.SCROLL_STATE_IDLE:
                        handler.sendEmptyMessage(START);
                        break;
                }
            }
        });
        indexImage[0].setOnClickListener(this);
        indexImage[1].setOnClickListener(this);
        indexImage[2].setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getContext(),ProDetailActivity.class);
        for(int i=0;i<3;i++){
            if(indexImage[i] == v){
                if(mDrawable[i]!=null){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("product",data_list.get(i));
                    intent.putExtra("data",bundle);
                    startActivity(intent);
                }
            }
        }
    }

    //从服务器获取宝贝资源
    public void initDataList() {
        gson = new Gson();
        HttpUtil httpUtil = new HttpUtil();
        String url = HttpUtil.getUrl(getContext()) + "getIndexProduct";
        Map<String, String> params = new HashMap<String, String>();
        params.put("limitNum", "3");
        httpUtil.postRequest(url, params, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("debug", "从服务器读取首页信息失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String JSON = response.body().string();
                try {
                    Log.e("debug", JSON);
                    Type jsonType = new TypeToken<BaseJsonObject<List<ProductSellDetail>>>() {
                    }.getType();
                    BaseJsonObject<SellerInfo> jsonObject = null;
                    jsonObject = (BaseJsonObject) gson.fromJson(JSON, jsonType);
                    data_list = (List<ProductSellDetail>) jsonObject.getResult();
                    Message msg = new Message();
                    msg.what = RECOMMAND;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void initImage() {

        if (data_list.size() > 0) {
            //ImageLoader imageLoader = new ImageLoader(getContext());
            String url = HttpUtil.getUrl(getContext()) + "/getImage";
            for (int i = 0; i < 3; i++) {
                DisplayImage(url + "?urlId=" + data_list.get(i).getProSrc(), i);
                indexTitle[i].setText(data_list.get(i).getProName());
            }

        }
    }


    public void DisplayImage(final String url, final int position) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDrawable[position] = Drawable.createFromStream(
                            new URL(url).openStream(), "image.jpg");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            indexImage[position].setImageDrawable(mDrawable[position]);
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("debug", "加载图片异常" + e.getMessage());
                }
            }
        }).start();
    }

}

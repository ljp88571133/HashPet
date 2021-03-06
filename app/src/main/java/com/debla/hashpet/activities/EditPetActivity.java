package com.debla.hashpet.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.debla.hashpet.Model.SellerInfo;
import com.debla.hashpet.Model.URLImage;
import com.debla.hashpet.R;
import com.debla.hashpet.Utils.AppContext;
import com.debla.hashpet.services.imps.PetService;

import java.util.HashMap;
import java.util.Map;



/**
 * Created by Dave-PC on 2017/12/22.
 */

public class EditPetActivity extends Activity {
    private URLImage urlImage;
    private String localImg;
    private ImageView imageView;
    private Button submit;
    private EditText et_name;
    private EditText et_price;
    private EditText et_stock;
    private Spinner sp_variety;
    private Spinner sp_age;
    private RadioButton rb_mm;
    private String str_variety;     //宠物种类
    private int pet_age;
    private String gender;
    private PetService petService;
    private String brief;
    public static final String action="com.debla.hashpet.EditPetAction";
    private AppContext appContext;
    private String proTags[] = {"cat","dog","mammalian","strange"};
    private int proTagIndex;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.obj!=null) {
                Toast.makeText(EditPetActivity.this, "新宠上架成功", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(EditPetActivity.this, "新宠上架过程出现意外，请重试", Toast.LENGTH_SHORT).show();
            }
            finish();
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editpet);
        appContext = (AppContext) getApplicationContext();
        getURLImg();
        init();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (check()){
                    case 0:
                        //验证通过
                        break;
                    case 1:
                        Toast.makeText(EditPetActivity.this,"请填写品种名称",Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(EditPetActivity.this,"请填写售价",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(EditPetActivity.this,"请填写库存",Toast.LENGTH_SHORT).show();
                        break;
                }
                if(check()==0){
                    //上传表单
                    if(rb_mm.isChecked()){
                        gender="MM";//
                    }else{
                        gender="GG";
                    }
                    petService = new PetService();
                    petService.init(getApplicationContext());
                    petService.setHandler(handler);
                    Map params = new HashMap();
                    params.put("petName",et_name.getText().toString());
                    params.put("urlId",urlImage.getUrlId());
                    params.put("gender",gender);
                    params.put("petType",proTags[proTagIndex]);
                    params.put("petAge",String.valueOf(pet_age));
                    params.put("price",et_price.getText().toString());
                    params.put("brief",brief);
                    params.put("proStock",et_stock.getText().toString());
                    //此处上传id为商家店铺id
                    SellerInfo seller = (SellerInfo)(appContext.getInnerMap().get("seller"));
                    Integer shopId = seller.getShopId();
                    params.put("shopId",String.valueOf(shopId));
                    petService.createNewPet(params);
                }
            }
        });
    }

    /**
     * 获取之前的图片信息
     */
    public void getURLImg(){
        Intent intent =getIntent();
        Bundle bd = intent.getBundleExtra("data");
        urlImage = (URLImage) bd.get("img");
        localImg = bd.getString("imgurl");
        brief = bd.getString("brief");
    }

    public void init(){
        imageView = (ImageView) findViewById(R.id.edit_civ_head);
        Bitmap img = BitmapFactory.decodeFile(localImg);
        imageView.setImageBitmap(img);
        submit = (Button) findViewById(R.id.editpet_submit);
        et_name = (EditText) findViewById(R.id.pet_nanme);
        et_price = (EditText) findViewById(R.id.edit_pet_price);
        et_stock = (EditText) findViewById(R.id.edit_et_stock);
        sp_variety = (Spinner) findViewById(R.id.sp_variety);
        sp_age = (Spinner) findViewById(R.id.sp_age);
        rb_mm = (RadioButton) findViewById(R.id.rd_mm);
        sp_variety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String []values = getResources().getStringArray(R.array.petvarity);
                str_variety=values[position];
                proTagIndex=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                String []values = getResources().getStringArray(R.array.petvarity);
                str_variety=values[0];
                proTagIndex=0;
            }
        });
        sp_age.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pet_age= position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pet_age=0;
            }
        });
    }

    public int check(){
        if("".equals(et_name.getText().toString()))
            return 1;
        if("".equals(et_price.getText().toString()))
            return 2;
        if("".equals(et_stock.getText().toString()))
            return 3;
        return 0;
    }

}

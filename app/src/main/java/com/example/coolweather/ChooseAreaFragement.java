package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Insert;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragement  extends Fragment {
    public static  final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView title_text;
    private ListView display_content;
    private Button bt_back;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> display_data =new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private  List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view=inflater.inflate(R.layout.choose_area,container,false);
        title_text=view.findViewById(R.id.title_text);
        bt_back=view.findViewById(R.id.back_button);
        display_content=view.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,display_data);
        display_content.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        display_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    Log.d("TAG1", "当前的页面等级为： "+currentLevel);
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }
                else if(currentLevel==LEVEL_CITY){
                    Log.d("TAG2", "当前的页面等级为： "+currentLevel);
                    selectedCity=cityList.get(position);
                    queryCounties();
                } else if (currentLevel==LEVEL_COUNTY) {
                    String weatherId=countyList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();

                }
            }
        });
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces() {
        title_text.setText("中国");
        bt_back.setVisibility(View.GONE);
        provinceList=MyApplication.getInstance().getWeatherdatabase().provinceDao().getProvinceList();

        if(provinceList.size()>0){
            display_data.clear();
            for(Province province:provinceList){
                display_data.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            display_content.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String url="http://guolin.tech/api/china";
            queryFromServer(url,"province");
        }
    }

    private void queryFromServer(String url, String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NonNull  Call call,@NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String response_text=response.body().string();
                boolean result=false;
                if ("province".equals(type)) {
                    result= Utility.handleProvinceResponse(response_text);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(response_text,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(response_text,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }




    private void queryCounties() {
        title_text.setText(selectedCity.getCityName());
        bt_back.setVisibility(View.VISIBLE);
        countyList=MyApplication.getInstance().getWeatherdatabase().countyDao().getCountyList(selectedCity.getId());
        if(countyList.size()>0){
            Toast.makeText(getContext(), "country", Toast.LENGTH_SHORT).show();
            display_data.clear();
            for(County county:countyList){
                display_data.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            display_content.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String url="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(url,"county");
        }

    }

    private void queryCities() {
        title_text.setText(selectedProvince.getProvinceName());
        bt_back.setVisibility(View.VISIBLE);
        cityList=MyApplication.getInstance().getWeatherdatabase().cityDao().getCityList(selectedProvince.getId());
        if(cityList.size()>0){
            Toast.makeText(getContext(), "city", Toast.LENGTH_SHORT).show();
            display_data.clear();
            for(City city:cityList){
                display_data.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            display_content.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String url="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(url,"city");
        }
    }


    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}

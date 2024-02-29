package com.allinwon;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Callback;
import okhttp3.Call;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class Bus_nosActivity extends AppCompatActivity {
    private ArrayList<RouteData> routeList;
    private ArrayList<String> routes;
    private ListView routeListView;
    private TextView stationNmTextView;
    private TextView str_edBus;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_nos);

        routeList = new ArrayList<>();
        routeListView = findViewById(R.id.routeList);
        routes = new ArrayList<>();

        str_edBus = findViewById(R.id.str_edBus);
        stationNmTextView = findViewById(R.id.stationNmTextView);


//        TextView busRouteIdTextView = findViewById(R.id.busRouteIdTextView);
        Intent intent = getIntent();

        if (intent != null) {
            //넘어온 버스번호
            String busRouteId = intent.getStringExtra("BusRouteId");
            fetchDataFromAPI(busRouteId);


            String bus_Number = intent.getStringExtra("bus_Number");

            String bus_start = intent.getStringExtra("strBus");
            String bus_ed = intent.getStringExtra("edBus");


            CustomAdapter adapter = new CustomAdapter(this, routes, routeList);
            routeListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            stationNmTextView.setText(bus_Number);

            str_edBus.setText(bus_start + "<->" + bus_ed);



        }

    }






    private void fetchDataFromAPI(String busRouteId) {
        String apiKey = "srX%2Fa0LKczFtKCTpcWKJog68YI0wgZfgMwl6NFIZweEgPnIZbTMyHkV%2BzL4qDNMcWs1RBJmmgF6nP8EoGVymJQ%3D%3D";
        String fullurl = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?" +
                "ServiceKey=" + apiKey + "&busRouteId=" + busRouteId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullurl,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // API 응답을 XML 파싱하여 버스 정보를 가져옴
                        parseRouteInfo(response);
                        Log.e("Response OK", "OK");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("BusSearchActivity", "Error: " + error.toString());
                    }
                }
        );

        // RequestQueue에 요청 추가
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void parseRouteInfo(String xmlResponse) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlResponse));

            int eventType = parser.getEventType();
            String currentTag = "";
            RouteData currentRoute = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if ("itemList".equals(currentTag)) {
                            currentRoute = new RouteData();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        String text = parser.getText();
                        if ("station".equals(currentTag) && currentRoute != null) {
                            currentRoute.setStation(text);
                            Log.d("BusNosActivity", "station: " + text);
                        } else if ("stationNm".equals(currentTag) && currentRoute != null) {
                            currentRoute.setStationNm(text);
                            Log.d("BusNosActivity", "stationNm: " + text);
                        } else if ("stationNo".equals(currentTag) && currentRoute != null) {

                            currentRoute.setStationNo(text);
                            Log.d("BusNosActivity", "stationNo: " + text);
                        } else if ("transYn".equals(currentTag) && currentRoute != null){
                            currentRoute.setTransYn(text);
                            Log.d("BusNosActivity", "transYn: " + text);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("itemList".equals(parser.getName()) && currentRoute != null) {

                            routeList.add(currentRoute);
                            routes.add(currentRoute.getStationNm());
                            currentRoute = null;
                        }
                        break;
                }
                eventType = parser.next();
            }

            Log.d("RouteList", routes.toString());

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }


    private class CustomAdapter extends ArrayAdapter<String> {
        private Context context;
        private ArrayList<String> data;
        private ArrayList<RouteData> routeDataList;

        public CustomAdapter(Context context, ArrayList<String> data, ArrayList<RouteData> routeDataList) {
            super(context, android.R.layout.simple_list_item_1, data);
            this.context = context;
            this.data = data;
            this.routeDataList = routeDataList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            TextView textView = rowView.findViewById(android.R.id.text1);

            RouteData currentRoute = routeDataList.get(position);

            if (currentRoute.getTransYn().equals("Y")) {

                // 아래 라인 추가: 선을 긋기 위한 코드
                textView.setBackgroundResource(R.drawable.custom_line_drawable);

                //"회차" 정보 입력
                textView.setText(data.get(position)+ "  [회차]");
            } else{
                //아닐시 일반표시
                textView.setText(data.get(position));
            }



            return rowView;
        }
    }



    private class RouteData{
        private String station;
        private String stationNm;
        private String stationNo;

        private String transYn;

        public String getTransYn(){return transYn;}
        public void setTransYn(String transYn){this.transYn = transYn;}

        public String getStation() {
            return station;
        }

        public void setStation(String station) {
            this.station = station;
        }

        public String getStationNm() {
            return stationNm;
        }

        public void setStationNm(String stationNm) {
            this.stationNm = stationNm;
        }

        public String getStationNo() {
            return stationNo;
        }

        public void setStationNo(String stationNo) {
            this.stationNo = stationNo;
        }

    }

}
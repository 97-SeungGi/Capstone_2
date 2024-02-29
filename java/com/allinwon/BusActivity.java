package com.allinwon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

public class BusActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private BusApiHelper adapter;
    private ArrayList<BusData> busList;
    private ArrayList<String> BusTypeNameArr = new ArrayList<>(Arrays.asList("공용", "공항", "마을", "간선", "지선", "순환", "광역", "인천", "경기", "폐지"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        recyclerView = (RecyclerView) findViewById(R.id.bus_search_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        busList = new ArrayList<>();
        adapter = new BusApiHelper(busList);
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.bus_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색 버튼이 눌렸을 때 호출되는 메서드
                // 여기에서 실제 검색을 수행하고 결과를 표시
                busList.clear();
                fetchBusInfo(query);
                return true; // true 반환으로 이벤트 소비
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // 검색어가 변경될 때 호출되는 메서드
                // 이 메서드에서 검색어가 변경되는 동안의 동작을 추가할 수 있음
                return false;
            }
        });
    }
    public class BusApiHelper extends RecyclerView.Adapter<BusApiHelper.ViewHolder> {
        private ArrayList<BusActivity.BusData> busList;

        public BusApiHelper(ArrayList<BusActivity.BusData> list) {
            busList = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("Holder", "Ok");
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_bus_line, parent, false);
            return new BusApiHelper.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BusActivity.BusData item = busList.get(position);
            holder.bind(item);
            holder.busNumber.setText(item.getBusNumber());
            // busType이 null이 아닌 경우에만 처리
            if (item.getBusType() != null) {
                try {
                    int busType = Integer.parseInt(item.getBusType());
                    holder.busType.setText(BusTypeNameArr.get(busType));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            holder.region.setText(item.getRegionName());
            holder.startPoint.setText(item.getStartPoint());
            holder.endPoint.setText(item.getEndPoint());
        }

        @Override
        public int getItemCount() {
            return busList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView busNumber,busType,region,startPoint,endPoint;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                busNumber =(TextView) itemView.findViewById(R.id.busNumberTextView);
                busType =(TextView) itemView.findViewById(R.id.busTypeTextView);
                region =(TextView) itemView.findViewById(R.id.regionTextView);
                startPoint =(TextView) itemView.findViewById(R.id.startPointTextView);
                endPoint =(TextView) itemView.findViewById(R.id.endPointTextView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            BusActivity.BusData clickedItem = busList.get(position);


                            Intent intent = new Intent(BusActivity.this, Bus_nosActivity.class);
                            // 필요한 경우 데이터를 넘겨줄 수 있음
                            intent.putExtra("BusRouteId", clickedItem.getBusRouteId());
                            intent.putExtra("bus_Number", clickedItem.getBusNumber());
                            intent.putExtra("strBus", clickedItem.getStartPoint());
                            intent.putExtra("edBus", clickedItem.getEndPoint());



                            // startActivity(intent); // 다른 액티비티 시작
                            itemView.getContext().startActivity(intent); // 다른 액티비티 시작


                            Toast.makeText(itemView.getContext(), "Clicked on bus number: " + clickedItem.getBusNumber(), Toast.LENGTH_SHORT).show();


                        }
                    }
                });
            }

            public void bind(BusActivity.BusData item) {
            }
        }
    }
    private void parseBusInfo(String xmlResponse) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlResponse));

            int eventType = parser.getEventType();
            String currentTag = "";
            BusData currentBus = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if ("itemList".equals(currentTag)) {
                            currentBus = new BusData();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        String text = parser.getText();
                        if ("busRouteNm".equals(currentTag) && currentBus != null) {
                            currentBus.setBusNumber(text);
                            Log.d("BusActivity", "busNumber: " + text);
                        } else if ("routeType".equals(currentTag) && currentBus != null) {
                            currentBus.setBusType(text);
                            Log.d("BusActivity", "busType: " + text);
                        } else if ("stStationNm".equals(currentTag) && currentBus != null) {
                            currentBus.setStartPoint(text);
                            Log.d("BusActivity", "startPoint: " + text);
                        } else if ("edStationNm".equals(currentTag) && currentBus != null) {
                            currentBus.setEndPoint(text);
                            Log.d("BusActivity", "endPoint: " + text);
                        } else if ("busRouteId".equals(currentTag) && currentBus != null) {
                            currentBus.setBusRouteId(text); // busRouteId를 설정
                            Log.d("BusActivity", "busRouteId: " + text);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("itemList".equals(parser.getName()) && currentBus != null) {
                            busList.add(currentBus);
                            currentBus = null;
                        }
                        break;
                }
                eventType = parser.next();
            }

            Log.d("BusList", busList.toString());
            // 어댑터에게 데이터 세트가 변경되었음을 알림
            adapter.notifyDataSetChanged();

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchBusInfo(String query) {
        String baseUrl = "http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList?serviceKey=srX%2Fa0LKczFtKCTpcWKJog68YI0wgZfgMwl6NFIZweEgPnIZbTMyHkV%2BzL4qDNMcWs1RBJmmgF6nP8EoGVymJQ%3D%3D";  // 공공데이터포털의 경기도버스정보 API URL로 변경
        String fullUrl = baseUrl + "&strSrch=" + query + "&resultType=xml";
        Log.e("SEULGI SEARCH API URL", fullUrl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // API 응답을 XML 파싱하여 버스 정보를 가져옴
                        parseBusInfo(response);
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
    public class BusData {
        private String busNumber;
        private String regionName = "서울";
        private String busType;
        private String startPoint;
        private String endPoint;
        private String busRouteId; // 새로운 필드 추가


        // 생성자, getter 및 setter 메서드 추가


        public String getBusRouteId() {return busRouteId;}

        public void setBusRouteId(String busRouteId) {this.busRouteId = busRouteId;}

        public String getBusNumber() {
            return busNumber;
        }

        public void setBusNumber(String busNumber) {
            this.busNumber = busNumber;
        }

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public String getBusType() {
            return busType;
        }

        public void setBusType(String busType) {
            this.busType = busType;
        }

        public String getStartPoint() {
            return startPoint;
        }

        public void setStartPoint(String startPoint) {
            this.startPoint = startPoint;
        }

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }
    }
}

package com.example.weathercontrol;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView temp, hum, press, humMax, tempMax; // инициализация всех текствью
    DatabaseReference dref, reference; // зависимости с бд
    FirebaseDatabase database; // экземпляр бд
    String status; // статусы сенсоров
    String status1;
    String status2;
    Double data, data2; // переменные для привидения строк к типу Double
    SwitchCompat switchCompat; // переключатель
    String humTemp, tempTemp; // переменные для вывода максимальных значений температуры и влажности
    ImageView imageView; // рисунок лампы
    private double lastXPoint = 1; // последняя точка по оси х
    private LineGraphSeries<DataPoint> series, series2; // два графика
    ListView listViewSensors;
    List<SensorModel> sensorList; // список сенсоров
    private ProgressBar pgsBar; // индикатор загрузки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pgsBar = (ProgressBar) findViewById(R.id.pBar);
       humMax = (TextView) findViewById(R.id.textView8);
        tempMax = (TextView) findViewById(R.id.textView9);
        switchCompat = findViewById(R.id.switchButton);
        imageView = findViewById(R.id.imageView);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
        temp = (TextView) findViewById(R.id.textView3);
        hum = (TextView) findViewById(R.id.textView4);
        press = (TextView) findViewById(R.id.textView7);
        dref = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("sensor"); // указание пути до сына
        listViewSensors = (ListView) findViewById(R.id.listViewSensors);
        sensorList = new ArrayList<>();

        Query query = reference.orderByChild("hum"); // запрос к бд для нахождения максимального значения влажности
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) // цикл по всем сыновьям sensor
                {
                    humTemp = myDataSnapshot.child("hum").getValue().toString(); // берем значение влажности и приводим к типу String
                    humMax.setText(humTemp + "%"); // выводим в текствью

                }
            }
            public void onCancelled(DatabaseError firebaseError) {

            }

        });

        Query query2 = reference.orderByChild("temp"); // запрос к бд для нахождения максимального значения температуры
        query2.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) // цикл по всем сыновьям sensor
                {
                    tempTemp = myDataSnapshot.child("temp").getValue().toString(); // берем значение температуры и приводим к типу String
                    tempMax.setText(tempTemp + " \u2103"); // выводим в текствью

                }
            }
            public void onCancelled(DatabaseError firebaseError) {

            }

        });

        Query queryRecycler = reference.limitToLast(50); // запрос к бд для вывода последних 50 значений
        queryRecycler.addChildEventListener(new ChildEventListener() {


            public void onChildAdded(DataSnapshot dataSnapshot, String previousKey) {
                SensorModel sensor = dataSnapshot.getValue(SensorModel.class); // берем значения сенсоров из соответствующего класса
                sensorList.add(sensor); // добавляем в список
                SensorList adapter = new SensorList(MainActivity.this, sensorList);
                listViewSensors.setAdapter(adapter);
            }



            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String previousKey) {
                lastXPoint += 1d; // прибавляем каждый раз 1 к оси х для рисование графика
                double temp = dataSnapshot.child("temp").getValue(Double.class); // берем значение температуры и влажности
                double hum = dataSnapshot.child("hum").getValue(Double.class);
                series.appendData(new DataPoint(lastXPoint, temp), false, 5000); // назначаем данные значения графикам
                series2.appendData(new DataPoint(lastXPoint, hum), false, 5000);
                pgsBar.setVisibility(View.GONE); // после отображения графика индикатор загрузки пропадает
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        dref.addValueEventListener(new ValueEventListener() { // вывод текущих значений сенсоров
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status = dataSnapshot.child("temp").getValue().toString();

                temp.setText(status +" \u2103");
                status1 = dataSnapshot.child("hum").getValue().toString();
                hum.setText(status1+"%");
                data = Double.valueOf(status1);
                data2 = Double.valueOf(status);
                status2 = dataSnapshot.child("pressure").getValue().toString();
                press.setText(status2 + " MB");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>(); // создаем объекты графиков
        series2 = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        graph.addSeries(series2);

        series.setColor(Color.BLUE); // настройка графиков
        series.setThickness(7);
        series.setAnimated(true);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) { // обработчик нажатия на точки для вывода значения точки
                String msg = "Temperature: " + dataPoint.getY() +" \u2103";
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
            }
        });
        series2.setColor(Color.RED);
        series2.setThickness(7);
        series2.setAnimated(true);
        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                String msg = "Humidity: " + dataPoint.getY() + "%";
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
            }
        });
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScalable(true);

        graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);
        series.setTitle("Temperature");
        series2.setTitle("Humidity");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setTextSize(30);
        graph.getLegendRenderer().setBackgroundColor(Color.argb(80, 230, 230, 230));

        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // включение/выключение светодиода и отправка текущего значения светодиода на сервер
                if(switchCompat.isChecked()){
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("LED_STATUS");
                myRef.setValue(1);}
                else{
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("LED_STATUS");
                    myRef.setValue(0);
                }
            }
        });

    }
}

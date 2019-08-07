package com.example.weathercontrol;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
public class SensorList extends ArrayAdapter<SensorModel> {
    private Activity context;
    private List<SensorModel> sensorList;

    public SensorList(Activity context, List<SensorModel> sensorList){
        super(context, R.layout.sensorlist, sensorList);
        this.context = context;
        this.sensorList = sensorList;
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) { // выводим значения датчиков в представление ListView
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.sensorlist, null, true);
        TextView textViewTemp = (TextView) listViewItem.findViewById(R.id.text_temp);
        TextView textViewHum = (TextView) listViewItem.findViewById(R.id.text_hum);
        TextView textViewPress = (TextView) listViewItem.findViewById(R.id.text_press);
        TextView textViewDate = (TextView) listViewItem.findViewById(R.id.textData);

        SensorModel sensor = sensorList.get(position);
        String result = String.valueOf(sensor.getTemp());
        String result2 = String.valueOf(sensor.getHum());
        String result3 = String.valueOf(sensor.getPressure());
        textViewTemp.setText(result + " \u2103");
        textViewHum.setText(result2 + "%");
        textViewPress.setText(result3 + " MB");
        textViewDate.setText(sensor.getTime());

        return listViewItem;
    }
}

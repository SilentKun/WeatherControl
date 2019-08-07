package com.example.weathercontrol;

public class SensorModel {

   public Double  temp, hum, pressure; // названия переменных в БД
    public  String time;
    public SensorModel()
    {

    }
    public SensorModel(Double  temp, Double  hum, Double  pressure, String  time) { // конструктор
        this.temp = temp;
        this.hum = hum;
        this.pressure = pressure;
        this.time = time;
    }

    public Double getTemp() { // геттеры
        return temp;
    }

    public Double getHum() {
        return hum;
    }

    public Double getPressure() {
        return pressure;
    }

    public String getTime() {
        return time;
    }
}

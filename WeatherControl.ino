#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <Adafruit_BMP085.h>  
#include "DHT.h"
#include <Wire.h>  
#include <NTPClient.h>
#include <WiFiUdp.h>
#define DHTPIN 4 // номер пина AM2320
#define FIREBASE_HOST "iot2019-27cf7.firebaseio.com" // ссылка на хост БД
#define FIREBASE_AUTH "rxIMALsD08mnpOco13Doc41TNSn2sMm0wugZ4Tf7" // ключ авторизации в БД
#define WIFI_SSID "POCOPHONE" // название точки доступа
#define WIFI_PASSWORD "1234567890" // пароль точки доступа
#define I2C_SCL 12                    //Номера SCL SDA
#define I2C_SDA 13 

DHT dht(DHTPIN, DHT22); //Инициация датчика
Adafruit_BMP085 bmp; // Объект BMP180
WiFiUDP ntpUDP; // для получения текущего времени
NTPClient timeClient(ntpUDP);
float dst,bt,bp,ba; // иниацилазция переменных для хранения данных с датчика BMP180
bool bmp085_present=true;
String formattedDate; // дата в общем виде
String dayStamp; // дата
String timeStamp; // время
String allDate; // форматированная дата + время
void setup() {
  Serial.begin(9600);
  dht.begin(); // инициирует am2320 датчик
  timeClient.begin();
  timeClient.setTimeOffset(10800); // смещение времени в секундах для установки часового пояса
  Wire.begin(I2C_SDA, I2C_SCL); // для взаимодействия с I2C
  pinMode(16, OUTPUT);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD); // подключение к вай фаю
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
  Serial.print(".");
  delay(500);
if (!bmp.begin()) 
     {
       Serial.println("Could not find a valid BMP085 sensor, check wiring!"); // если датчик bmp180 не найден
       while (1) {}
     }
}
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH); // подключение к БД Firebase
  Firebase.set("LED_STATUS", 0); // выключение светодиода по умолчанию
}
int n = 0;
int num = 0;
void loop() {
 delay(400); 
 while(!timeClient.update()) {
    timeClient.forceUpdate(); // обновление даты
  }
  float h = dht.readHumidity(); //Измеряем влажность
  float t = dht.readTemperature(); //Измеряем температуру
  if (isnan(h) || isnan(t)) {  // Проверка. Если не удается считать показания, выводится «Ошибка считывания», и программа завершает работу
    Serial.println("Ошибка считывания");
    return;
  }
  Serial.print("Влажность: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Температура: ");
  Serial.print(t);
  Serial.println(" *C "); 
  float bp =  bmp.readPressure()/100; // получение значение давления и перевод давления в миллибары
  float ba =  bmp.readAltitude(); // получение высоты над уровнем моря
  float bt =  bmp.readTemperature(); // получение температуры
  float dst = bmp.readSealevelPressure()/100; // давление над уровнем моря
if (num++ > 5){ // пропускаем первые 5 итерация для задержки в ~10 секунд
  num = 0;
  Firebase.setFloat("temp",t); // отправка значения температуры на сервер
  Firebase.setFloat("hum",h); // отправка значение влажности на сервер
  Firebase.setFloat("pressure",bp); // отправка значения давления на сервер
  Firebase.setFloat("altitude",ba); // отправка высоты над уровнем моря на сервер
  Firebase.setFloat("temp_bmp180",bt); // отправка температуры с датчика bmp180 на сервер
  Firebase.setFloat("sealevel",dst); // отправка давления над уровнем моря на сервер
  formattedDate = timeClient.getFormattedDate(); // получение даты в общем виде
  int splitT = formattedDate.indexOf("T"); // удаляем лишние символы
  dayStamp = formattedDate.substring(0, splitT);
  timeStamp = formattedDate.substring(splitT+1, formattedDate.length()-1);
  allDate = dayStamp + " " + timeStamp; // объединяем дату и время
  StaticJsonBuffer<200> jsonBuffer; // буфер для отправки json
        JsonObject& root = jsonBuffer.createObject(); // отправка значений в отдельные ноды
        root["temp"] = t;
        root["hum"] = h;
        root["pressure"] = bp;
        root["time"] = allDate;
        String name = Firebase.push("/sensor", root);
    }

delay(400);

n = Firebase.getInt("LED_STATUS"); // прослушка переменной LED_STATUS на сервере
if (n==1) {
Serial.println("LED ON");
digitalWrite(16,HIGH);  
return;
//delay(10);
}
else {
Serial.println("LED OFF");
digitalWrite(16,LOW);  
}
}



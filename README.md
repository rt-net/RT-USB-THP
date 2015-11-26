# RT-USB-THP

![Alt Text](https://github.com/{taryo}/{RT-USB-THP}/raw/dev/web/RT-WB-TH.png)


本リポジトリはUSB出力温湿気圧センサモジュールのサンプルプログラムおよびマニュアル
をまとめたものです.  各種ファイルの内容は以下になります.  

## circuit 
USB出力温室気圧センサモジュールの回路図です.

## datasheet
温室気圧センサbme280およびlpc1343マイコンのデータシートです.

## firmware 
出荷時にlpc1343マイコンに書き込まれているファームウェアです.  
このファームウェアでは1秒ごとにセンサの書く測定データをUSBおよびUART経由で送信し続けます.  

## manual
USB出力温室気圧センサモジュールのマニュアルです.

## driver.zip
Windows環境用のUSBドライバです.  Windows環境でUSBを使用する際は
本ファイルを解凍し中のドライバをインストールして下さい. 
ドライバのインストール方法についてはmanualフォルダ内のマニュアルを
参照してください.
Linux環境においてはドライバーのインストールは必要ありません.  

## 動作の様子

各環境における動作の様子の動画です.  

### Windows環境での動作確認
デバイスドライバをインストールした後には本センサは仮想COMポートで認識されます.  
以下の動画は仮想COMポートをteratermを用いて開いたときの様子です.

[![](http://img.youtube.com/vi/i_fkRaMwUJE/0.jpg)](https://www.youtube.com/watch?v=i_fkRaMwUJE)

### Linux環境での動作確認
Linux環境で本センサは/dev配下のデバイスファイルとして認識されます. 
以下の動画はデバイスファイルとして認識したファイルを開いたときの様子です.

[![](http://img.youtube.com/vi/0P6QNqg0ExA/0.jpg)](https://www.youtube.com/watch?v=0P6QNqg0ExA)









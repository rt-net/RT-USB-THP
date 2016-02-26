#include "bme280.h"
#include "i2c.h"
#include "type.h"
#include "SystemTickTimer.h"

//i2cの操作に必要なもの達のextern宣言.  実体はi2c.cにある
extern volatile uint8_t I2CMasterBuffer[BUFSIZE];
extern volatile uint8_t I2CSlaveBuffer[BUFSIZE];
extern volatile uint32_t I2CReadLength, I2CWriteLength;




//Define
#define BME280_ADDR     0x76
//BME280の設定レジスタのアドレス bme280_init（）で使用
#define CONFIG_ADDR     0xF5
#define CTRL_MEAS_ADDR  0xF4
#define CTRL_HUM_ADDR   0xF2



//TRIMデータ(キャリブレーション用）
//元々の型は、データシートのTable 16 参照
uint16_t       dig_T1;
int16_t        dig_T2;
int16_t        dig_T3;
unsigned short dig_P1;
short          dig_P2;
short          dig_P3;
short          dig_P4;
short          dig_P5;
short          dig_P6;
short          dig_P7;
short          dig_P8;
short          dig_P9;
unsigned char  dig_H1;
short          dig_H2;
unsigned char  dig_H3;
short          dig_H4;
short          dig_H5;
char           dig_H6;

int32_t t_fine;
int32_t t_fine_;



void bme280_init(void){


	uint8_t osrs_t = 1;             //Temperature oversampling x 1
	uint8_t osrs_p = 1;             //Pressure oversampling x 1
	uint8_t osrs_h = 1;             //Humidity oversampling x 1
	uint8_t mode = 3;               //Normal mode
	uint8_t t_sb = 5;               //Tstandby 1000ms
	uint8_t filter = 0;             //Filter off
	uint8_t spi3w_en = 0;           //3-wire SPI Disable

	uint8_t ctrl_meas_reg = (osrs_t << 5) | (osrs_p << 2) | mode;
	uint8_t config_reg    = (t_sb << 5) | (filter << 2) | spi3w_en;
	uint8_t ctrl_hum_reg  = osrs_h;

	wait1msec(100);

	I2CWriteLength = 3;
	I2CReadLength = 0;
	I2CMasterBuffer[0] = BME280_W;
	I2CMasterBuffer[1] = 0xF2;
	I2CMasterBuffer[2] = ctrl_hum_reg;
	I2CEngine();

	wait1msec(100);
    I2CWriteLength = 3;
	I2CReadLength = 0;
	I2CMasterBuffer[0] = BME280_W;
	I2CMasterBuffer[1] = 0xF5;
	I2CMasterBuffer[2] = config_reg;
	I2CEngine();

	wait1msec(100);
	I2CWriteLength = 3;
	I2CReadLength = 0;
	I2CMasterBuffer[0] = BME280_W;
	I2CMasterBuffer[1] = 0xF4;
	I2CMasterBuffer[2] = ctrl_meas_reg;
	I2CEngine();
	wait1msec(100);

}


void get_rawdata(int *_t_data,int *_p_data ,int *_h_data){

    volatile uint8_t i;

	I2CWriteLength = 2;
	I2CReadLength = 8;
	I2CMasterBuffer[0] = BME280_W;
	I2CMasterBuffer[1] = 0xf7;
	I2CMasterBuffer[2] = BME280_R;
	I2CEngine();

    *_p_data = 0;
	*_t_data = 0;
	*_h_data = 0;

	//受信データをまとめる_p_data,_t_data は20bit  _h_dataは16bit それぞれビットシフトで代入
    *_p_data = ((I2CSlaveBuffer[0] << 12) |(I2CSlaveBuffer[1] << 4) |(I2CSlaveBuffer[2] >> 4));
    *_t_data = ((I2CSlaveBuffer[3] << 12) |((I2CSlaveBuffer[4] << 4) |I2CSlaveBuffer[5] >> 4));
    *_h_data = ((I2CSlaveBuffer[6] << 8)  | I2CSlaveBuffer[7] );

    for ( i = 0; i < BUFSIZE; i++ ) //clear I2CSlaveBuffer
	{
		I2CSlaveBuffer[i] = 0x00;
	}

}

//キャリブレーション用データ取得
void get_trimdata(void){

    uint8_t trim_tmp1[24],trim_tmp2[1],trim_tmp3[7];   //レジスタごとのデータ一時格納用 レジスタの場所が分かれてるのでそれぞれreadするよう
    uint8_t read_addr ;                                //読み込みアドレスの先頭
    uint8_t device_addr = (BME280_ADDR << 1);          //デバイスアドレス
    uint8_t i;


    //データ受信
    read_addr = 0x88;                       //dig_T1-dig_P9

	I2CWriteLength = 2;
	I2CReadLength = 24;
	I2CMasterBuffer[0] = device_addr;
	I2CMasterBuffer[1] = read_addr;
	I2CMasterBuffer[2] = 0xED;
	I2CEngine();

	for(i=0;i<24;i++) trim_tmp1[i] = I2CSlaveBuffer[i];

	wait1msec(100);
    read_addr = 0xA1;                       //dig_H1

	I2CWriteLength = 2;
	I2CReadLength = 1;
	I2CMasterBuffer[0] = device_addr;
	I2CMasterBuffer[1] = read_addr; //Address 14byte (AccXYZ + GyroXYZ + temp)
	I2CMasterBuffer[2] = 0xED;
	I2CEngine();

	for(i=0;i<1;i++) trim_tmp2[i] = I2CSlaveBuffer[i];
	wait1msec(100);

    read_addr = 0xE1;                       //dig_H2-dig_H6

	I2CWriteLength = 2;
	I2CReadLength = 7;
	I2CMasterBuffer[0] = device_addr;
	I2CMasterBuffer[1] = read_addr; //Address 14byte (AccXYZ + GyroXYZ + temp)
	I2CMasterBuffer[2] = 0xED;
	I2CEngine();

	for(i=0;i<7;i++) trim_tmp3[i] = I2CSlaveBuffer[i];

	dig_T1 = 0;
	dig_T2 = 0;
	dig_T3 = 0;

	dig_P1 = 0;
	dig_P2 = 0;
	dig_P3 = 0;
	dig_P4 = 0;
	dig_P5 = 0;
	dig_P6 = 0;
	dig_P7 = 0;
	dig_P8 = 0;
	dig_P9 = 0;

	dig_H1 = 0;
	dig_H2 = 0;
	dig_H3 = 0;
	dig_H4 = 0;
	dig_H5 = 0;
	dig_H6 = 0;

    //受信データを指定された型で変数にする
    //T
    dig_T1 = trim_tmp1[0]  | (trim_tmp1[1] << 8);
    dig_T2 = trim_tmp1[2]  | (trim_tmp1[3] << 8);
    dig_T3 = trim_tmp1[4]  | (trim_tmp1[5] << 8);

    //P
    dig_P1 = trim_tmp1[6]  | (trim_tmp1[7] << 8);
    dig_P2 = trim_tmp1[8]  | (trim_tmp1[9] << 8);
    dig_P3 = trim_tmp1[10] | (trim_tmp1[11] << 8);
    dig_P4 = trim_tmp1[12] | (trim_tmp1[13] << 8);
    dig_P5 = trim_tmp1[14] | (trim_tmp1[15] << 8);
    dig_P6 = trim_tmp1[16] | (trim_tmp1[17] << 8);
    dig_P7 = trim_tmp1[18] | (trim_tmp1[19] << 8);
    dig_P8 = trim_tmp1[20] | (trim_tmp1[21] << 8);
    dig_P9 = trim_tmp1[22] | (trim_tmp1[23] << 8);

    //H
    dig_H1 = trim_tmp2[0];
    dig_H2 = trim_tmp3[0]  |(trim_tmp3[1] << 8);
    dig_H3 = trim_tmp3[2];
    dig_H4 = (trim_tmp3[4]&0x0F) | (trim_tmp3[3] << 4);  //3-0bit,11-4bit というちょっと変則な感じで入ってる
    dig_H5 = ( ((trim_tmp3[4]&0xF0)>>4) ) | (trim_tmp3[5] <<4 );  //〃
    dig_H6 = trim_tmp3[6];

}

//温度キャリブレーション
int32_t calib_temparture(int32_t _t_rawdata){

   int32_t var1,var2,temparture;

   var1 = ((((_t_rawdata >> 3) - ((int32_t)dig_T1<<1))) * ((int32_t)dig_T2))>>11;
   var2 = (((((_t_rawdata >> 4) - ((int32_t)dig_T1)) * ((_t_rawdata >> 4) - ((int32_t)dig_T1))) >> 12) * ((int32_t)dig_T3)) >> 14;

   t_fine = var1 + var2;
   temparture = (t_fine * 5 +128) >> 8;

    return temparture;
}

//気圧キャリブレーション。
uint32_t calib_pressure(int _p_rawdata){

    int64_t var1,var2,pressure;        //64ビット

    var1 = ((int64_t)t_fine) - 128000;
    var2 = var1 * var1 * (int64_t)dig_P6;
    var2 = var2 + ((var1 * (int64_t)dig_P5)<<17);
    var2 = var2 + (((int64_t)dig_P4)<<35);
    var1 = ((var1 * var1 * (int64_t)dig_P3)>>8) + ((var1 * (int64_t)dig_P2)<<12);
    var1 = (((((int64_t)1)<<47) + var1)) * ((int64_t)dig_P1)>>33;

    if(var1 == 0){
        return 0;
    }

    pressure = 1048576 - _p_rawdata;
    pressure = (((pressure<<31)-var2)*3125)/var1;
    var1 = (((int64_t)dig_P9) * (pressure >> 13) * (pressure >> 13)) >> 25;
    var2 = (((int64_t)dig_P8) * pressure) >> 19;

    pressure = ((pressure + var1 + var2) >> 8) + (((int64_t)dig_P7)<<4);
    return (int32_t)pressure;
}


double BME280_compensate_T_double(int32_t adc_T)
{
	double var1, var2, T;
	var1 = (((double)adc_T)/16384.0 - ((double)dig_T1)/1024.0) * ((double)dig_T2);
	var2 = ((((double)adc_T)/131072.0 - ((double)dig_T1)/8192.0) *
	(((double)adc_T)/131072.0 - ((double) dig_T1)/8192.0)) * ((double)dig_T3);
	t_fine_ = (int32_t)(var1 + var2);
	T = (var1 + var2) / 5120.0;
	return T;
}

// Returns pressure in Pa as double. Output value of “96386.2” equals 96386.2 Pa = 963.862 hPa
double BME280_compensate_P_double(int32_t adc_P)
{
	double var1, var2, p;
	var1 = ((double)t_fine_/2.0) - 64000.0;
	var2 = var1 * var1 * ((double)dig_P6) / 32768.0;
	var2 = var2 + var1 * ((double)dig_P5) * 2.0;
	var2 = (var2/4.0)+(((double)dig_P4) * 65536.0);
	var1 = (((double)dig_P3) * var1 * var1 / 524288.0 + ((double)dig_P2) * var1) / 524288.0;
	var1 = (1.0 + var1 / 32768.0)*((double)dig_P1);
	if (var1 == 0.0)
	{
		return 0; // avoid exception caused by division by zero
	}
	p = 1048576.0 - (double)adc_P;
	p = (p - (var2 / 4096.0)) * 6250.0 / var1;
	var1 = ((double)dig_P9) * p * p / 2147483648.0;
	var2 = p * ((double)dig_P8) / 32768.0;
	p = p + (var1 + var2 + ((double)dig_P7)) / 16.0;
	return p;
}
// Returns humidity in %rH as as double. Output value of “46.332” represents 46.332 %rH
double bme280_compensate_H_double(int32_t adc_H)
{
	double var_H;
	var_H = (((double)t_fine_) - 76800.0);
	var_H = (adc_H - (((double)dig_H4) * 64.0 + ((double)dig_H5) / 16384.0 * var_H)) *
	(((double)dig_H2) / 65536.0 * (1.0 + ((double)dig_H6) / 67108864.0 * var_H *
	(1.0 + ((double)dig_H3) / 67108864.0 * var_H)));
	var_H = var_H * (1.0 - ((double)dig_H1) * var_H / 524288.0);

	if (var_H > 100.0) var_H = 100.0;
	else if (var_H < 0.0) var_H = 0.0;

	return var_H;
}

//湿度キャリブレーション
unsigned int calib_humidity(int h_rawdata){

    int v_x1_u32r;

    v_x1_u32r = (t_fine - ((int)76800));
    v_x1_u32r = (((((h_rawdata << 14) - (((int)dig_H4) << 20) - (((int)dig_H5) * v_x1_u32r)) +
                ((int)16384)) >> 15) * (((((((v_x1_u32r * ((int)dig_H6)) >> 10) * (((v_x1_u32r *
                ((int)dig_H3)) >> 11) + ((int)32768))) >> 10) + ((int)2097152)) *
                ((int)dig_H2) + 8192) >> 14));

    v_x1_u32r = (v_x1_u32r - (((((v_x1_u32r >> 15) * (v_x1_u32r >> 15)) >> 7) * ((int)dig_H1)) >> 4));
    v_x1_u32r = (v_x1_u32r < 0 ? 0 : v_x1_u32r);
    v_x1_u32r = (v_x1_u32r > 419430400 ? 419430400 : v_x1_u32r);
    return (unsigned int)(v_x1_u32r>>12);

}

void whoAmIBME280()
{
	uint16_t i;


	myPrintfUSB("//////////////// \n");

	I2CWriteLength = 2;
	I2CReadLength = 1;
	I2CMasterBuffer[0] = BME280_W;
	I2CMasterBuffer[1] = 0xD0;
	I2CMasterBuffer[2] = BME280_R;
	I2CEngine();

	myPrintfUSB("Who Am I BME280 : %d \n", I2CSlaveBuffer[0]);

	for ( i = 0; i < BUFSIZE; i++ ) //clear I2CSlaveBuffer
	{
		I2CSlaveBuffer[i] = 0x00;
	}

	myPrintfUSB("//////////////// \n");
}



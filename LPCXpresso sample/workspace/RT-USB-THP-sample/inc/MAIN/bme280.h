#ifndef __BME280_H__
#define __BME280_H__

#include "type.h"

void bme280_init(void); //BME初期化
void get_rawdata(int *_t_data,int *_p_data ,int *_h_data);   //生のレジスタデータ取得
void get_trimdata(void);    //キャリブレーション用の値取得

int32_t  calib_temparture(int32_t _t_rawdata);  //温度キャリブレーション
uint32_t calib_pressure(int32_t _p_rawdata);   //気圧キャリブレーション
unsigned int calib_humidity(int h_rawdata);    //湿度キャリブレーション

double BME280_compensate_T_double(int32_t adc_T);
double BME280_compensate_P_double(int32_t adc_P);
double bme280_compensate_H_double(int32_t adc_H);

void whoAmIBME280(void);

#define BME280_W	(0xec) //0b11101100
#define BME280_R	(0xed) //0b11101101






#endif

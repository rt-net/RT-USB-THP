/**
 * @file   main.c
 * @brief  温湿気圧センサのサンプルプログラム<br>
 *
 * @author RTCorp. Ryota Takahashi
 */

//OHTER
#include "LPC13xx.h"
#include "core_cm3.h"
#include "type.h"
//PERIHERAL
#include "debug.h"
#include "i2c.h"
#include "io.h"
//#include "ssp.h"
#include "SystemTickTimer.h"
#include "timer.h"
#include "usbTransmission.h"
#include "uart.h"
//MAIN
#include "MainFunction.h"
#include "UserInterface.h"
#include "bme280.h"

//USB
#include "usb.h"
#include "usbcfg.h"
#include "usbhw.h"
#include "usbcore.h"
#include "cdc.h"
#include "cdcuser.h"

/////プロトタイプ宣言//////////////
void init(void);
/////////////////////////////

/**
 * main関数
 *
 * @param void
 * @return 0
 */
int main(void) {

	init();
	tryUSBConnect();        //USB接続を試みる

	setSendDataEnable(1);
	if (isUSBConfig() == 1)  flashLED(300, 300); //USBの接続使用
	else flashLED(100, 100); //USBの接続不使用

	//bme280の初期化
	get_trimdata();
	bme280_init();

	while (1);

	return 0;
}

/**
 * 各種ペリフェラルの初期化
 *
 * @param  void
 * @return void
 */
void init(void) {
	//IOポートの初期化
	initIO();
	//LEDを一回だけ点滅させる
	turnLED(1);
	wait1msec(500);
	turnLED(0);
	wait1msec(500);
	//各ペリフェラルの初期化
	USBIOClkConfig();
	initTimer32_0();
	initTimer32_1();
	USB_Init();
	UARTInit(115200*4);
	while (!I2CInit((uint32_t) I2CMASTER)); // I2Cの初期化が終わるまで待つ
	//UIの初期化
	initUI();
	initMainFunction();

}

/******************************************************************************
 **                            End Of File
 ******************************************************************************/

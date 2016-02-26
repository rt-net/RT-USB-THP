################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/MODE/mode_BluetoothSetting.c 

OBJS += \
./src/MODE/mode_BluetoothSetting.o 

C_DEPS += \
./src/MODE/mode_BluetoothSetting.d 


# Each subdirectory must supply rules for building sources it contributes
src/MODE/%.o: ../src/MODE/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MCU C Compiler'
	arm-none-eabi-gcc -DDEBUG -D__CODE_RED -DCORE_M3 -D__USE_CMSIS=CMSISv1p30_LPC13xx -D__LPC13XX__ -D__REDLIB__ -I"C:\Users\ryota\OneDrive\RT working\RT-USB-THP\workspace_1010_takahashi\CMSISv1p30_LPC13xx\inc" -I"C:\Users\ryota\OneDrive\RT working\RT-USB-THP\workspace_1010_takahashi\RT-USB-THP-sample\inc\PERIPHERAL" -I"C:\Users\ryota\OneDrive\RT working\RT-USB-THP\workspace_1010_takahashi\RT-USB-THP-sample\inc\USB" -I"C:\Users\ryota\OneDrive\RT working\RT-USB-THP\workspace_1010_takahashi\RT-USB-THP-sample\inc\OTHER" -I"C:\Users\ryota\OneDrive\RT working\RT-USB-THP\workspace_1010_takahashi\RT-USB-THP-sample\inc\MODE" -I"C:\Users\ryota\OneDrive\RT working\RT-USB-THP\workspace_1010_takahashi\RT-USB-THP-sample\inc\MAIN" -Og -g3 -Wall -c -fmessage-length=0 -fno-builtin -ffunction-sections -fdata-sections -mcpu=cortex-m3 -mthumb -D__REDLIB__ -specs=redlib.specs -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



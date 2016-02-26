################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/MAIN/MainFunction.c \
../src/MAIN/UserInterface.c \
../src/MAIN/bme280.c \
../src/MAIN/main.c 

OBJS += \
./src/MAIN/MainFunction.o \
./src/MAIN/UserInterface.o \
./src/MAIN/bme280.o \
./src/MAIN/main.o 

C_DEPS += \
./src/MAIN/MainFunction.d \
./src/MAIN/UserInterface.d \
./src/MAIN/bme280.d \
./src/MAIN/main.d 


# Each subdirectory must supply rules for building sources it contributes
src/MAIN/%.o: ../src/MAIN/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MCU C Compiler'
	arm-none-eabi-gcc -DDEBUG -D__CODE_RED -DCORE_M3 -D__USE_CMSIS=CMSISv1p30_LPC13xx -D__LPC13XX__ -D__REDLIB__ -I"C:\Users\ryota\OneDrive\RT working\github\RT-USB-THP\LPCXpresso sample\workspace_exp\CMSISv1p30_LPC13xx\inc" -I"C:\Users\ryota\OneDrive\RT working\github\RT-USB-THP\LPCXpresso sample\workspace_exp\RT-USB-THP-sample\inc\PERIPHERAL" -I"C:\Users\ryota\OneDrive\RT working\github\RT-USB-THP\LPCXpresso sample\workspace_exp\RT-USB-THP-sample\inc\USB" -I"C:\Users\ryota\OneDrive\RT working\github\RT-USB-THP\LPCXpresso sample\workspace_exp\RT-USB-THP-sample\inc\OTHER" -I"C:\Users\ryota\OneDrive\RT working\github\RT-USB-THP\LPCXpresso sample\workspace_exp\RT-USB-THP-sample\inc\MAIN" -Og -g3 -Wall -c -fmessage-length=0 -fno-builtin -ffunction-sections -fdata-sections -mcpu=cortex-m3 -mthumb -D__REDLIB__ -specs=redlib.specs -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



package com.ecotechmarine.ecosmartlive_android;

/**
 * Created by EJ Mann on 10/7/13.
 */

public class Device {

    public Device(){}

    private int deviceId;
    private String serialNo;
    private String name;
    private String osRev;
    private String bootRev;
    private String rfRev;
    private int rfFrequency;
    private int parentGroupId;
    private String operatingMode;
    private String errorState;
    private int lunarPhaseCurrentDay;
    private float lunarPhaseCurrentScalar;
    private int timeHour;
    private int timeMinute;
    private int timeSecond;
    private String formattedTime;
    private int acDay;
    private float acIntensity;
    private int acPeriod;
    private String deviceType;
    private String model;
    private int modelNumber;

    private boolean isPro;
    private boolean isPump;
    protected boolean isSelected;
    private String parentGroupName;

    private String currentOperatingMode;
    private Boolean localControl;
    private int subnet;
    private String rfStatus;
    private String powerState;
    private String operatingState;
    private int displayIndex;
    private Boolean needsFirmwareUpdate;

    //Pump Specific Properties
    private String syncType;
    private int motorTemp;
    private int slaveTo;


    // ==== General Setters & Getters for all Devices

    public void setFormattedTime(String formattedTime){this.formattedTime = formattedTime;}

    public String getFormattedTime(){return formattedTime;}

    public void setModel(String model) {this.model = model;}

    public String getModel() {return model;}

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentGroupName() {
        return parentGroupName;
    }

    public void setParentGroupName(String parentGroupName) {
        this.parentGroupName = parentGroupName;
    }

    public int getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(int parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public boolean isPro() {
        return isPro;
    }

    public void setPro(boolean pro) {
        isPro = pro;
    }

    public boolean isPump() { return isPump; }

    public void setIsPump(boolean isPump) { this.isPump = isPump; }

    public String getOsRev() {
        return osRev;
    }

    public void setOsRev(String osRev) {
        this.osRev = osRev;
    }

    public String getBootRev() {
        return bootRev;
    }

    public void setBootRev(String bootRev) {
        this.bootRev = bootRev;
    }

    public String getRfRev() {
        return rfRev;
    }

    public void setRfRev(String rfRev) {
        this.rfRev = rfRev;
    }

    public int getRfFrequency() {
        return rfFrequency;
    }

    public void setRfFrequency(int rfFrequency) {
        this.rfFrequency = rfFrequency;
    }

    public String getOperatingMode() {
        return operatingMode;
    }

    public void setOperatingMode(String operatingMode) {
        this.operatingMode = operatingMode;
    }

    public String getErrorState() {
        return errorState;
    }

    public void setErrorState(String errorState) {
        this.errorState = errorState;
    }

    // ==== Setters & Getters for Radion Device Details List

    public void setCurrentOperatingMode(String currentOperatingMode){this.currentOperatingMode = currentOperatingMode;}

    public String getCurrentOperatingMode(){return currentOperatingMode;}

    public void setLocalControl(Boolean localControl){this.localControl = localControl;}

    public Boolean getLocalControl(){return localControl;}

    public void setSubnet(int subnet){this.subnet = subnet;}

    public int getSubnet(){return subnet;}

    public void setRfStatus(String rfStatus){this.rfStatus = rfStatus;}

    public String getRfStatus(){return rfStatus;}

    public void setPowerState(String powerState){this.powerState = powerState;}

    public String getPowerState(){return powerState;}

    public void setOperatingState(String operatingMode){this.operatingState = operatingState;}

    public String getOperatingState(){return operatingState;}


    // ========== Setters and getters for Settings View ==========

    public int getAcDay() {return acDay;}

    public void setAcclimationDay(int acDay) {this.acDay = acDay;}

    public float getAcIntensity() {return acIntensity;}

    public void setAcIntensity(int acIntensity) {this.acIntensity = acIntensity;}

    public int getAcPeriod() {return acPeriod;}

    public void setAcPeriod(int acPeriod) {this.acPeriod = acPeriod;}

    public int getLunarPhaseCurrentDay(){return lunarPhaseCurrentDay;}

    public void setLunarPhaseCurrentDay(int lunarPhaseCurrentDay){this.lunarPhaseCurrentDay = lunarPhaseCurrentDay;}

    public float getLunarPhaseCurrentScalar(){return lunarPhaseCurrentScalar;}

    public void setLunarPhaseCurrentScalar(float lunarPhaseCurrentScalar){this.lunarPhaseCurrentScalar = lunarPhaseCurrentScalar;}

    public int getTimeHour(){return timeHour;}

    public void setTimeHour(int timeHour){this.timeHour = timeHour;}

    public int getTimeMinute(){return timeMinute;}

    public void setTimeMinute(int timeMinute){this.timeMinute = timeMinute;}

    public int getTimeSecond(){return timeSecond;}

    public void setTimeSecond(int timeSecond){this.timeSecond = timeSecond;}


    // ========== Settings & Getters for new Aquarium Wizard ===========

    public String getDeviceType(){return deviceType;}

    public void setDeviceType(String deviceType){this.deviceType = deviceType;}

    public int getModelNumber(){return modelNumber;}

    public void setModelNumber(int modelNumber){this.modelNumber = modelNumber;}


    // ==== Setters & Getters for Pump Stuff

    public void setSyncType(String syncType){this.syncType = syncType;}

    public String getSyncType(){return syncType; }

    public void setMotorTemp(int motorTemp){this.motorTemp = motorTemp;}

    public int getMotorTemp(){return motorTemp;}

    public void setSlaveTo(int slaveTo){this.slaveTo = slaveTo;}

    public int getSlaveTo(){return slaveTo;}

    public void setDisplayIndex(int displayIndex){this.displayIndex = displayIndex;}

    public int getDisplayIndex(){return displayIndex;}

    public void setNeedsFirmwareUpdate(Boolean needsFirmwareUpdate){this.needsFirmwareUpdate = needsFirmwareUpdate;}

    public Boolean getNeedsFirmwareUpdate(){return needsFirmwareUpdate;}
}

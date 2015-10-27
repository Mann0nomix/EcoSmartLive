package com.ecotechmarine.ecosmartlive_android;

/**
 * Created by EJ Mann on 10/7/13.
 */

public class DeviceGroup {

    public DeviceGroup(){

    }

    private String name;
    private int groupId;
    private int radionCount;
    private int radionProCount;
    private int pumpCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getRadionCount() {
        return radionCount;
    }

    public void setRadionCount(int radionCount) {
        this.radionCount = radionCount;
    }

    public int getRadionProCount() {
        return radionProCount;
    }

    public void setRadionProCount(int radionProCount) {
        this.radionProCount = radionProCount;
    }

    public int getPumpCount(){ return pumpCount; }

    public void setPumpCount(int pumpCount){ this.pumpCount = pumpCount; }

}
package com.ecotechmarine.ecosmartlive_android;

/**
 * Created by EJ Mann on 10/9/13.
 */

public class Preset {
    public Preset(){}

    int presetId;
    String name;
    float uv;
    float royalBlue;
    float blue;
    float white;
    float green;
    float red;
    float brightness;

    public void setPresetId(int presetId){
        this.presetId = presetId;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUv(float uv){
        this.uv = uv;
    }

    public void setRoyalBlue(float royalBlue){
        this.royalBlue = royalBlue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void setWhite(float white) {
        this.white = white;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public int getPresetId(){
        return presetId;
    }

    public String getName(){
        return name;
    }

    public float getUv(){
        return uv;
    }

    public float getRoyalBlue(){
        return royalBlue;
    }

    public float getBlue(){
        return blue;
    }

    public float getWhite(){
        return white;
    }

    public float getGreen(){
        return green;
    }

    public float getRed(){
        return red;
    }

    public float getBrightness(){
        return brightness;
    }
}

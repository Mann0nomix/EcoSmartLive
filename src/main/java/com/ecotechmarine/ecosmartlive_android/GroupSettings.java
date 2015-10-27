package com.ecotechmarine.ecosmartlive_android;

/**
 * Created by emann on 2/27/14.
 */
public class GroupSettings {

    public enum Type{
        EcoSmartParticipation(0), OverrideTimer(1), AcclimateTimer(2), LunarPhases(3);

        public final int index;
        public boolean enabled;
        public int duration;
        public boolean travelingSun;
        public float intensity;

        Type(int index){
            this.index = index;
        }

        public int getIndex(){
            return this.index;
        }

    }

    public GroupSettings(){}

}

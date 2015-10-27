package com.ecotechmarine.ecosmartlive_android.websocket;
public class ESLWebSocketMessage implements IESLWebSocketMessage {

    public enum ESLWebSocketMessageType {
        ConnectedToEsl("ui_connected"),
        WebSocketError("WebSocketError"),
        ProgramSchedule("program_schedule"),
        SetGroupSettingComplete("set_advanced_setting"),
        DiscoverDevices("discover_and_query"),
        AssignAndGroup("assign_and_group"),
        ProcessAndSetRT("bridge_rt_update_complete"),
        GetDeviceStatus("get_device_status"),
        LegacyLightFirmwareUpdate("light_firmware"),
        LegacyPumpFirmwareUpdate("pump_firmware"),
        DeviceFirmwareUpdate("device_firmware"),
        USBDeviceConnected("bridge_status");


        private final String messageType;

        ESLWebSocketMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getValue() {
            return messageType;
        }
    }


    private final Object messageType;
    private final Object message;

    public ESLWebSocketMessage(Object messageType, Object message){
        this.messageType = messageType;
        this.message = message;
    }

    @Override
    public Object getMessageType(){
        return messageType;
    }

    @Override
    public Object getMessage(){
        return message;
    }
}

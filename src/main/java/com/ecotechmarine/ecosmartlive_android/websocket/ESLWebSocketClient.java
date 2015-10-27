package com.ecotechmarine.ecosmartlive_android.websocket;

import com.ecotechmarine.ecosmartlive_android.LegacyRadion;
import com.ecotechmarine.ecosmartlive_android.LegacyVortech;
import com.ecotechmarine.ecosmartlive_android.websocket.ESLWebSocketMessage.ESLWebSocketMessageType;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

/**
 * Created by sobrien on 11/25/13.
 */
public class ESLWebSocketClient extends ESLWebSocketMessageSender {
    private static ESLWebSocketClient instance = null;

    private WebSocketClient wsClient;
    private ESLWebSocketMessage eslWebSocketMessage;

    public static ESLWebSocketClient sharedInstance(String wsHost, Long userAccountId) throws Exception{
        if(instance == null){
            instance = new ESLWebSocketClient(wsHost, userAccountId);
        }
        return instance;
    }

    protected ESLWebSocketClient(String wsHost, Long userAccountId) throws Exception{
         URI serverUri = new URI(String.format("ws://%s/%s.123456789.web", wsHost, String.valueOf(userAccountId)));
         wsClient = new WebSocketClient(serverUri, new Draft_17()){

            @Override
            public void onOpen(ServerHandshake arg0) {
                 System.out.println("WebSocket opened");
             }

            @Override
            public void onClose(int arg0, String arg1, boolean arg2) {
                this.close();

                System.out.println("WebSocket closed");
            }

            @Override
            public void onError(Exception arg0) {
                notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.WebSocketError.getValue(), arg0.getMessage()));
            }

            @Override
            public void onMessage(String message) {
                try {
                    System.out.println("Received websocket message: " + message);

                    JSONObject msgJson = new JSONObject(message);
                    if(msgJson != null){
                        if(msgJson.has("cmd")){
                            String cmd = msgJson.getString("cmd");
                            // Responses to commands, e.g. program schedule
                            if(cmd.equalsIgnoreCase("display")){
                                String cmdId = msgJson.getString("cmd_id");
                                if(cmdId.equalsIgnoreCase("program_schedule")){
                                    String msg = msgJson.getString("msg");
                                    if(msg != null && msg.length() > 0){
                                        notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.ProgramSchedule.getValue(), msg));
                                    }
                                }else if(cmdId.equalsIgnoreCase("discover_and_query")){
                                    String target = msgJson.getString("target");
                                    String targetId = msgJson.getString("target_id");
                                    if (!target.equalsIgnoreCase("B") || target.equalsIgnoreCase("B") && targetId.equalsIgnoreCase("191")){
                                        String msg = msgJson.getString("msg");
                                        if(msg.length() > 0){
                                            notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.DiscoverDevices.getValue(), msg));
                                        }
                                    }
                                }else if(cmdId.equalsIgnoreCase("set_advanced_setting")){
                                    String msg = msgJson.getString("msg");
                                    String settingStatus = msgJson.getString("status");
                                    if (settingStatus != null && settingStatus.length() > 0 && settingStatus.equalsIgnoreCase("success")){
                                        if(msg.length() > 0){
                                            notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.SetGroupSettingComplete.getValue(), null));
                                        }
                                    }
                                }else if (cmdId.equalsIgnoreCase("get_device_status")){
                                    String msg = msgJson.getString("msg");
                                    if (msg.length() > 0){
                                        notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.GetDeviceStatus.getValue(), msg));
                                    }
                                }else if (cmdId.equalsIgnoreCase("assign_and_group")){
                                    String target = msgJson.getString("target");
                                    String targetId = msgJson.getString("target_id");
                                    String msg = msgJson.getString("msg");
                                    if (target.equalsIgnoreCase("B") && msg.equalsIgnoreCase("bridge_rt_update_complete")){
                                        notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.ProcessAndSetRT.getValue(), null));
                                    }else{
                                        if (msg != null && msg.charAt(0) == '3' && !targetId.equalsIgnoreCase("191")){
                                            return;
                                        }
                                        if (msg.length() > 0){
                                            notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.AssignAndGroup.getValue(), msg));
                                        }
                                    }
                                }else if(cmdId.equalsIgnoreCase("ui_connected")){
                                    //String extra = msgJson.getString("extra");
                                    String msg = msgJson.getString("msg");
                                    if (msg != null && msg.length() > 0){
                                        notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.ConnectedToEsl.getValue(), msg));
                                    }
                                }else if(cmdId.equalsIgnoreCase("update_firmware")){
                                    String msg = msgJson.getString("msg");
                                    if(msg.length() > 0){
                                        if(LegacyRadion.legacyRadionActive){
                                            notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.LegacyLightFirmwareUpdate.getValue(), msg));
                                        }
                                        else if(LegacyVortech.legacyVortechActive){
                                            notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.LegacyPumpFirmwareUpdate.getValue(), msg));
                                        }
                                        else{
                                            notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.DeviceFirmwareUpdate.getValue(), msg));
                                        }
                                    }
                                }
                            }else if(cmd.equalsIgnoreCase("cevent")){
                                // Connection event
                                String event = msgJson.getString("event");
                                if(event.equalsIgnoreCase("cm_status")){
                                    boolean isConnectedToEsl = msgJson.getBoolean("status");
                                    if(isConnectedToEsl){
                                        notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.ConnectedToEsl.getValue(), null));
                                    }
                                }else if(event.equalsIgnoreCase("bridge_status")){
                                    String msg = msgJson.getString("status");
                                    boolean bridgeStatus = Boolean.valueOf(msg);
                                    System.out.println(msg + ", Bridge Status = " + bridgeStatus);
                                    if (bridgeStatus && LegacyRadion.legacyRadionActive){
                                        notifyWebSocketMessageReceivers(new ESLWebSocketMessage(ESLWebSocketMessageType.USBDeviceConnected.getValue(), null));
                                    }

                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public boolean isOpen(){
        return (wsClient != null && wsClient.getConnection() != null && wsClient.getConnection().isOpen());
    }

    public void connect() throws Exception{
        if(isOpen()){
            disconnect();
        }
        if(wsClient != null){
            wsClient.connect();
        }
    }

    public void disconnect() throws Exception{
        if(isOpen()){
            wsClient.close();
        }
    }
}

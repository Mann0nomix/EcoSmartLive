package com.ecotechmarine.ecosmartlive_android.websocket;

/**
 * Created by sobrien on 11/25/13.
 */
public interface IESLWebSocketMessageSender {
    void addWebSocketMessageReceiver(IESLWebSocketMessageReceiver o);
    void deleteWebSocketMessageReceiver(IESLWebSocketMessageReceiver o);
    void notifyWebSocketMessageReceivers(IESLWebSocketMessage notification);
}

package com.ecotechmarine.ecosmartlive_android.websocket;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by sobrien on 11/25/13.
 */
public class ESLWebSocketMessageSender implements IESLWebSocketMessageSender {
    private List<IESLWebSocketMessageReceiver> receivers;

    @Override
    public synchronized void addWebSocketMessageReceiver(IESLWebSocketMessageReceiver o)
    {
        if (receivers == null)
        {
            receivers = new ArrayList<IESLWebSocketMessageReceiver>();
        }
        else if (receivers.contains(o))
        {
            return;
        }
        receivers.add(o);
    }

    @Override
    public synchronized void deleteWebSocketMessageReceiver(IESLWebSocketMessageReceiver o)
    {
        if (receivers == null)
        {
            return;
        }
        int idx = receivers.indexOf(o);
        if (idx != -1)
        {
            receivers.remove(idx);
        }
    }

    public synchronized void notifyWebSocketMessageReceivers(IESLWebSocketMessage notification)
    {
        if (receivers == null)
        {
            return;
        }
        for (IESLWebSocketMessageReceiver o : receivers)
        {
            o.onWebSocketMessageReceived(notification);
        }
    }
}

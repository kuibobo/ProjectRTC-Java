package webrtc.server.component;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import webrtc.server.cache.GuavaCache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SocketIOComponent {

    @Autowired
    private SocketIOServer server;

    @Autowired
    private GuavaCache cache;

    @PostConstruct
    private void autoStartup() {
        server.start();
    }

    @PreDestroy
    private void autoStop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    /**
     * 客户端连接的时候触发
     *
     * @param client SocketIOClient
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.info("-- {} joined --", client.getSessionId());
        // 自定义事件`connected` -> 与客户端通信  （也可以使用内置事件，如：Socket.EVENT_CONNECT）
        client.sendEvent("connected", "ok");
        client.sendEvent("id", client.getSessionId());
    }

    /**
     * 客户端关闭连接时触发
     *
     * @param client SocketIOClient
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("-- {} left --", client.getSessionId());

        String cacheKey = "clients";
        List<Map<String, String>> clients = (List) cache.get(cacheKey);
        if (clients == null)
            return;

        clients = clients.stream().filter(item -> !item.get("id").equals(client.getSessionId().toString())).collect(Collectors.toList());
        cache.add(cacheKey, clients);
    }

    @OnEvent(value = "init")
    public void onInit(SocketIOClient client, AckRequest request, Object template) {
        log.info("init");
    }

    @OnEvent(value = "message")
    public void onMessage(SocketIOClient client, AckRequest request, Map<String, String> msg) {
        log.info("message");
        String to = msg.get("to");
        SocketIOClient otherClient = null;

        if (!StringUtil.isNullOrEmpty(to)) {
            UUID uuid = UUID.fromString(to);
            otherClient = server.getClient(uuid);
            if (otherClient == null)
                return;
        }
        msg.remove("to");
        msg.put("from", client.getSessionId().toString());
        otherClient.sendEvent("message", msg);
    }

    @OnEvent(value = "readyToStream")
    public void onReadyToStream(SocketIOClient client, AckRequest request, Map<String, String> msg) {
        log.info("readyToStream");

        String cacheKey = "clients";
        List<Map<String, String>> clients = (List) cache.get(cacheKey);

        if (clients == null) {
            clients = new ArrayList();
        }
        clients.add(new HashMap<String, String>() {{
            put("id", client.getSessionId().toString());
            put("name", msg.get("name"));
        }});

        cache.add(cacheKey, clients);
    }

    @OnEvent(value = "update")
    public void onUpdate(SocketIOClient client, AckRequest request, Object template) {
        log.info("update");
    }
}

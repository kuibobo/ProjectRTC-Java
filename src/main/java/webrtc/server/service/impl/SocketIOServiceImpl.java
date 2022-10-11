//package webrtc.server.service.impl;
//
//import com.corundumstudio.socketio.SocketIOClient;
//import com.corundumstudio.socketio.SocketIOServer;
//import com.corundumstudio.socketio.annotation.OnConnect;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//import webrtc.server.service.ISocketIOService;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Slf4j
//@Service
//@Component
//public class SocketIOServiceImpl implements ISocketIOService {
//
//    /**
//     * 存放已连接的客户端
//     */
//    private static Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>();
//
//    /**
//     * 自定义事件`push_data_event`,用于服务端与客户端通信
//     */
//    private static final String PUSH_DATA_EVENT = "push_data_event";
//
//    @Autowired
//    private SocketIOServer socketIOServer;
//
//    /**
//     * Spring IoC容器创建之后，在加载SocketIOServiceImpl Bean之后启动
//     */
//    @PostConstruct
//    private void autoStartup() {
//        start();
//    }
//
//    /**
//     * Spring IoC容器在销毁SocketIOServiceImpl Bean之前关闭,避免重启项目服务端口占用问题
//     */
//    @PreDestroy
//    private void autoStop() {
//        stop();
//    }
//
//    @Override
//    public void start() {
//        // 监听客户端连接
//        socketIOServer.addConnectListener(client -> {
//            log.debug("************ 客户端： " + getIpByClient(client) + " 已连接 ************");
//            // 自定义事件`connected` -> 与客户端通信  （也可以使用内置事件，如：Socket.EVENT_CONNECT）
//            client.sendEvent("connected", "你成功连接上了哦...");
//            String userId = getParamsByClient(client);
//            if (userId != null) {
//                clientMap.put(userId, client);
//            }
//        });
//
//        // 监听客户端断开连接
//        socketIOServer.addDisconnectListener(client -> {
//            String clientIp = getIpByClient(client);
//            log.debug(clientIp + " *********************** " + "客户端已断开连接");
//            String userId = getParamsByClient(client);
//            if (userId != null) {
//                clientMap.remove(userId);
//                client.disconnect();
//            }
//        });
//
//        // 自定义事件`client_info_event` -> 监听客户端消息
//        socketIOServer.addEventListener(PUSH_DATA_EVENT, String.class, (client, data, ackSender) -> {
//            // 客户端推送`client_info_event`事件时，onData接受数据，这里是string类型的json数据，还可以为Byte[],object其他类型
//            String clientIp = getIpByClient(client);
//            log.debug(clientIp + " ************ 客户端：" + data);
//        });
//
//        // 启动服务
//        socketIOServer.start();
//    }
//
//    @OnConnect
//    public void onConnect(SocketIOClient client) {
//        String sessionId = client.getHandshakeData().getSingleUrlParam("userId");
//        if (!StringUtils.isEmpty(sessionId)) {
//            // 存储 SocketIOClient，用于发送消息
//            log.info("用户:" + sessionId + "已连接");
//        }
//    }
//
//    @Override
//    public void stop() {
//        if (socketIOServer != null) {
//            socketIOServer.stop();
//            socketIOServer = null;
//        }
//    }
//
//    @Override
//    public void pushMessageToUser(String userId, String msgContent) {
//        SocketIOClient client = clientMap.get(userId);
//        if (client != null) {
//            client.sendEvent(PUSH_DATA_EVENT, msgContent);
//        }
//    }
//
//    /**
//     * 获取客户端url中的userId参数（这里根据个人需求和客户端对应修改即可）
//     *
//     * @param client: 客户端
//     * @return: java.lang.String
//     */
//    private String getParamsByClient(SocketIOClient client) {
//        // 获取客户端url参数（这里的userId是唯一标识）
//        Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
//        List<String> userIdList = params.get("userId");
//        if (!CollectionUtils.isEmpty(userIdList)) {
//            return userIdList.get(0);
//        }
//        return null;
//    }
//
//    /**
//     * 获取连接的客户端ip地址
//     *
//     * @param client: 客户端
//     * @return: java.lang.String
//     */
//    private String getIpByClient(SocketIOClient client) {
//        String sa = client.getRemoteAddress().toString();
//        String clientIp = sa.substring(1, sa.indexOf(":"));
//        return clientIp;
//    }
//
//}
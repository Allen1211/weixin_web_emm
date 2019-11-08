package com.allen.imsystem.netty;

import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.NewFriendNotify;
import com.allen.imsystem.service.IFriendService;
import com.allen.imsystem.service.IMessageService;
import com.allen.imsystem.service.INotifyService;
import com.allen.imsystem.service.impl.RedisService;
import com.auth0.jwt.interfaces.Claim;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import org.aspectj.bridge.IMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;

    @Autowired
    private GlobalChannelGroup channelGroup;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private INotifyService notifyService;

    @Autowired
    private WebSocketEventHandler webSocketEventHandler;

    /**
     * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。
     * 也就是客户端与服务端建立了通信通道并且可以传输数据
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("new channel active : " + ctx.channel().remoteAddress().toString());
    }

    /**
     * channel 通道 Inactive 不活跃的 当客户端主动断开服务端的链接后，这个通道就是不活跃的。
     * 也就是说客户端与服务端关闭了通信通道并且不可以传输数据
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        String uid = getUidFromContext(ctx);
        Channel channel = ctx.channel();
        boolean removeSuccess = channelGroup.removeChannel(uid, channel);
        boolean hasChannelExist = channelGroup.hasChannel(uid);
        // 如果没有了，设置为离线状态
        if (!hasChannelExist && uid != null) {
            redisService.hset(GlobalConst.Redis.KEY_USER_STATUS, uid, GlobalConst.UserStatus.OFFLINE);
        }
        if (removeSuccess) {
            System.out.println(new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(new Date()) +
                    " websocket client closed : " + uid + "  channelId: " + channel.id());
        }
    }

    /**
     * 接收客户端发送的消息 channel 通道 Read 读 简而言之就是从通道中读取数据，也就是服务端接收客户端发来的数据。
     * 但是这个数据在不进行解码时它是ByteBuf类型的
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpUpgradeRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * channel 通道 Read 读取 Complete 完成 在通道读取完成后会在这个方法里通知，
     * 对应可以做刷新操作 ctx.flush()
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 处理websocket消息帧，解析JSON数据，并分发给业务处理类。
     *
     * @param ctx
     * @param frame
     */
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 如果是关闭帧，处理用户离线的业务
        if (frame instanceof CloseWebSocketFrame) {
            // TODO 用户离线
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        }
        // 如果是PING帧，返回一个PONG
        if (frame instanceof PingWebSocketFrame) {
            System.out.println("get a ping! from " + ctx.channel().id().toString());
            ctx.channel().write(new PongWebSocketFrame(frame.content()));
            return;
        }


        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            System.out.println("ws接收到信息 系统时间：" + System.currentTimeMillis() + " -> ");
            System.out.println("getMessage: " + text);

            // JSON解析出eventCode
            JSONObject jsonObject = JSONObject.parseObject(text);
            Integer eventCode = (Integer) jsonObject.get("eventCode");
            if (eventCode.equals(104)||eventCode.equals(105)) {
                jsonObject.put("uid", getUidFromContext(ctx));
            }
            // 委托给处理类处理请求
            webSocketEventHandler.handleRequest(eventCode, jsonObject);
            return;
        }

        if (frame instanceof BinaryWebSocketFrame) {
            return;
        }

    }


    /**
     * 处理HTTP请求， 本项目用于处理websocket建立前的upgrade请求
     *
     * @param ctx
     * @param request
     */
    private void handleHttpUpgradeRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 如果HTTP解码失败，或者非websocket请求， 返回HTTP异常
        if (request.decoderResult().isFailure()
                || !"websocket".equals(request.headers().get("Upgrade"))) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        // token验证
        String uri = request.uri();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        List<String> tokenList = parameters.get("token");
        if (tokenList == null || tokenList.size() == 0 || !verifyTokenAndSetAttr(ctx, tokenList.get(0))) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
            return;
        }

        // 构造握手响应返回
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(GlobalConst.Path.TALK_WEBSOCKET_URL, null, false);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
            // 握手成功后
            addUserChannelToContainer(ctx);
            // 推送离线通知
            pushOfflineNotify(ctx);
        }
    }

    /**
     * 返回httpResponse
     *
     * @param ctx
     * @param request
     * @param response
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request,
                                  DefaultFullHttpResponse response) {
        if (response.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), StandardCharsets.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
        }

        ChannelFuture f = ctx.channel().writeAndFlush(response);
        // 如果是非 Keep-Alive连接，或者返回不成功，关闭
        if (!HttpUtil.isKeepAlive(request) || response.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 把用户channel加入到容器中
     *
     * @param ctx
     */
    private void addUserChannelToContainer(ChannelHandlerContext ctx) {
        String uid = getUidFromContext(ctx);
        Channel newChannel = ctx.channel();
        boolean addSuccess = channelGroup.addChannel(uid, newChannel);
        System.out.println("user " + uid + " join websocket");

//        Channel oldChannel = GlobalChannelGroup.removeChannel(uid,);
//        if(oldChannel == null){ // 新channel
//            // 加入到容器
//            GlobalChannelGroup.addChannel(uid,ctx.channel());
//        }else{  // 该账户已经建立了一个channel了，挤掉。
//            newChannel.writeAndFlush(new TextWebSocketFrame("{\"err\":\"该账户已经建立了连接！你已把对方挤掉\"}"));
//            System.out.println("user "+uid + " replace original channel :" + oldChannel.id());
//            oldChannel.close();
//            GlobalChannelGroup.addChannel(uid,newChannel);
//        }

        // 更新用户登录状态
        redisService.hset(GlobalConst.Redis.KEY_USER_STATUS, uid, GlobalConst.UserStatus.ONLINE);
        return;
    }

    private void pushOfflineNotify(ChannelHandlerContext ctx) {
        String uid = getUidFromContext(ctx);
        List<NewFriendNotify> newFriendNotifyList = notifyService.getAllNewFriendNotify(uid);
        List<FriendApplicationDTO> newApplyNotifyList = notifyService.getAllNewFriendApplyNotify(uid);
        if (!CollectionUtils.isEmpty(newFriendNotifyList)) {
            messageService.sendNotify(205, uid, newFriendNotifyList);
        }
        if (!CollectionUtils.isEmpty(newApplyNotifyList)) {
            messageService.sendNotify(204, uid, newApplyNotifyList);
        }
    }

    private String getUidFromContext(ChannelHandlerContext ctx) {
        return (String) ctx.channel().attr(AttributeKey.valueOf("uid")).get();
    }

    private boolean verifyTokenAndSetAttr(ChannelHandlerContext ctx, String token) {
        if (token != null) {
            Map<String, Claim> result = JWTUtil.verifyLoginToken(token, "");
            if (result == null) {
                return false;
            }
            String uid = result.get("uid").asString();
            Integer userId = result.get("userId").asInt();
            ctx.channel().attr(AttributeKey.valueOf("uid")).set(uid);
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);
            return true;
        } else {
            return false;
        }
    }
}

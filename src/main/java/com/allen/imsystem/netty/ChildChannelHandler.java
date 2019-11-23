package com.allen.imsystem.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Channel注册类
 */
@Component
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    @Autowired
    private WebSocketServerHandler webSocketServerHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 将请求和应答消息解码为HTTP消息
        ch.pipeline().addLast("http-codec",new HttpServerCodec())
                .addLast("aggregator",new HttpObjectAggregator(65536))
                .addLast("http-chunked",new ChunkedWriteHandler())
                .addLast("handler",webSocketServerHandler);
    }
}

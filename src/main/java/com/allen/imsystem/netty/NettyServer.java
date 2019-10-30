package com.allen.imsystem.netty;

import com.allen.imsystem.common.Const.GlobalConst;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class NettyServer implements Runnable {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private ChannelFuture channelFuture;

    @Autowired
    private ChildChannelHandler childChannelHandler;

//    @PostConstruct
    public void init() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            System.out.println("netty run");
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(childChannelHandler);
            // 服务器绑定端口并监听
            channelFuture = bootstrap.bind(GlobalConst.NETTY_PORT).sync();
            // 服务器关闭端口监听
            channelFuture.channel().closeFuture().sync();
            serverChannel = channelFuture.channel();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    @PreDestroy
    public void destory() {
        System.out.println("destroy server resources");
        if (null == serverChannel) {
            System.out.println("server channel is null");
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        serverChannel.closeFuture().syncUninterruptibly();
        bossGroup = null;
        workerGroup = null;
        serverChannel = null;
    }


}

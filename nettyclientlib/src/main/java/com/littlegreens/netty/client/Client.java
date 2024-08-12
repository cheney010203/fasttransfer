package com.littlegreens.netty.client;

import android.util.Log;

import com.geely.netty.nettycore.Const;
import com.geely.netty.nettycore.FileUploadFile;
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.littlegreens.netty.client.listener.NettyClientListener;

import java.io.File;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Created by littleGreens on 2018-11-10.
 * TCP 客户端
 */
public class Client {
    private static final String TAG = "Client";
    private NettyTcpClient mNettyTcpStringClient;

    private Client() {
    }
    private static Client mInstance=null;
    //静态工厂方法
    public static Client getInstance() {
        if (mInstance == null) {
            mInstance = new Client();
        }
        return mInstance;
    }



    private  void connectFileServerAndUpload(int port, String host, final FileUploadFile fileUploadFile) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                    ch.pipeline().addLast(new FileUploadClientHandler(fileUploadFile));
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
    public  void sendFile(String path,String ip,int port){
        Thread clientFileThread = new Thread("client-Netty-File") {
            @Override
            public void run() {
                super.run();
                try {
                    FileUploadFile uploadFile = new FileUploadFile();
                    File file = new File(path);
                    String fileMd5 = file.getName();// 文件名
                    uploadFile.setFile(file);
                    uploadFile.setFile_md5(fileMd5);
                    uploadFile.setStarPos(0);// 文件开始位置
                    connectFileServerAndUpload(port, ip, uploadFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        clientFileThread.start();
    }

    public void initStringConnect(String ip,int port,NettyClientListener<String> listener){
        mNettyTcpStringClient = new NettyTcpClient.Builder()
                .setHost(ip)    //设置服务端地址
                .setTcpPort(port) //设置服务端端口号
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(true) //设置是否发送心跳
                .setHeartBeatInterval(5)    //设置心跳间隔时间。单位：秒
                .setHeartBeatData(Const.HEART_BEAT_DATA) //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .setIndex(0)    //设置客户端标识.(因为可能存在多个tcp连接)
//                .setPacketSeparator("#")//用特殊字符，作为分隔符，解决粘包问题，默认是用换行符作为分隔符
//                .setMaxPacketLong(1024)//设置一次发送数据的最大长度，默认是1024
                .build();

        mNettyTcpStringClient.setListener(listener); //设置TCP监听

        mNettyTcpStringClient.connect();//连接服务器
    }

    public  void sendString(String content,MessageStateListener listener){
        if (!mNettyTcpStringClient.getConnectStatus()) {
            Log.e(TAG, "sendString error,connect error" + content);
        } else {
            mNettyTcpStringClient.sendMsgToServer(content, listener);
        }
    }

    public class NettyClientStringLinsten implements NettyClientListener<String>{

        @Override
        public void onMessageResponseClient(String msg, int index) {
            Log.e(TAG, "onMessageResponse:" + msg);
        }

        @Override
        public void onClientStatusConnectChanged(int statusCode, int index) {
            Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
        }
    }
}

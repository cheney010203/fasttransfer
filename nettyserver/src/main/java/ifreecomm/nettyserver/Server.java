package ifreecomm.nettyserver;

import android.util.Log;

import com.geely.netty.nettycore.Const;

import ifreecomm.nettyserver.netty.NettyServerListener;
import ifreecomm.nettyserver.netty.NettyTcpServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public class Server {
    private static final String TAG = "Server";
    private Server() {
    }
    private static Server mInstance=null;
    //静态工厂方法
    public static Server getInstance() {
        if (mInstance == null) {
            mInstance = new Server();
        }
        return mInstance;
    }

    public void startStringServer(int port){
        NettyTcpServer nettyTcpServer = NettyTcpServer.getInstance();
        if (!nettyTcpServer.isServerStart()) {
            nettyTcpServer.setListener(new StringServerListener());

            nettyTcpServer.start(port);
        } else {
            NettyTcpServer.getInstance().disconnect();
        }
    }

    public boolean sendMsgToClient(String data, ChannelFutureListener listener){
        return NettyTcpServer.getInstance().sendMsgToServer(data,listener);
    }

    public void startFileServer(int port){
        NettyTcpServer nettyTcpServer = NettyTcpServer.getInstance();
        nettyTcpServer.startFileServer(port);
    }

    public class StringServerListener implements NettyServerListener<String> {

        @Override
        public void onMessageResponseServer(String msg, String ChannelId) {
            Log.e(TAG,"onMessageResponseServer,msg:"+msg+",ChannelId:"+ChannelId);
            if(Const.HEART_BEAT_DATA.equals(msg)){
                return;
            }
            //TODO:handler msg
            Log.e(TAG,"onMessageResponseServer,true msg:"+msg+",ChannelId:"+ChannelId);
        }

        @Override
        public void onStartServer() {
            Log.e(TAG, "onStartServer");
        }

        @Override
        public void onStopServer() {
            Log.e(TAG, "onStopServer");
        }

        @Override
        public void onChannelConnect(Channel channel) {
            Log.e(TAG, "onChannelConnect,channel:"+channel.toString());
            NettyTcpServer.getInstance().selectorChannel(channel);
        }

        @Override
        public void onChannelDisConnect(Channel channel) {
            Log.e(TAG, "onChannelDisConnect,channel:"+channel.toString());
        }
    }
}
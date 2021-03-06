package cameron.lookingglass;


import android.app.Application;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

public class SocketClient extends Application {

    static Socket globalmsocket;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(LookingGlassServer.getURL());
            globalmsocket = mSocket;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    public static Socket getGLobalSocket() {return globalmsocket;};
}

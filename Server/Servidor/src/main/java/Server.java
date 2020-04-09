import java.util.Scanner;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;


public class Server {
    static final int PORT = 8189;
    static SocketIOServer server;

    public static void main(String[] args) {
        server();
    }

    public static void server() {
        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(PORT);
        server = new SocketIOServer(config);
        final BroadcastOperations bo = server.getBroadcastOperations();

        server.addEventListener("join", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                System.out.println(data + " has joined the chat");
                bo.sendEvent("userjoinedthechat", data + " has joined the chat");
            }
        });

        server.addEventListener("send", String.class, new DataListener<String>() {

            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                System.out.println(data);
                bo.sendEvent("newmessage", data);
            }
        });

        server.addEventListener("disconnect", String.class, new DataListener<String>() {

            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                System.out.println(data + "Disconected");
            }
        });

        server.start();
        Scanner myObj = new Scanner(System.in);
        System.out.println("Exit program");

        String userName = myObj.nextLine();
        if (userName.contains("exit")) {
            System.out.println(userName);
            server.stop();
        }

    }
}

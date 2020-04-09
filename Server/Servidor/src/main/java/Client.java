import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.Scanner;

public class Client {
    static private Socket socket;
    static final int PORT = 8189;

    public static void main(String[] args) throws URISyntaxException {
        client();
    }

    public static void client() throws URISyntaxException {


        socket = IO.socket("http://localhost:" + PORT);
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                socket.emit("join", "Java Client");

            }
        });
        socket.on("newmessage", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Client recieved : " + args[0]);
            }
        });

        Scanner myObj = new Scanner(System.in);
        System.out.println("Send msg");

        String userName = myObj.nextLine();
        socket.emit("send", "{\"sender\":\"Ferran\",\"msg\":\"" + userName + "\"}");
        socket.connect();

        socket.on("userjoinedthechat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println(args[0]);
            }
        });
    }
}


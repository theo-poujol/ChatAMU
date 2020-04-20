import chatamu.protocol.Client;
import chatamu.protocol.Server;

import java.io.IOException;

public class ServerTest {

    public static void main (String[] args) throws IOException, InterruptedException {
        Server server = new Server(12345);
        server.run();
    }
}

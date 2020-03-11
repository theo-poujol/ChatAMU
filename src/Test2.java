import chatamu.protocol.Client;
import chatamu.protocol.Server;

import java.io.IOException;

public class Test2 {

    public static void main (String[] args) throws IOException {
        Server server = new Server(12345);
        server.run();
    }
}

package chatamu;

import chatamu.protocol.Client;
import chatamu.protocol.ClientNIO;

import java.io.IOException;

public class NioClientTest {
    public static void main (String[] args) throws IOException, InterruptedException {
        ClientNIO client = new ClientNIO("127.0.0.1", 12345);
        client.process();
    }
}

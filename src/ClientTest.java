import chatamu.protocol.Client;

public class ClientTest {

    public static void main (String[] args) {
        Client client = new Client("localhost", 12345);
        client.process();
    }
}

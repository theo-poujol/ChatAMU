import chatamu.protocol.Client;

public class ClientTest {

    public static void main (String[] args) {
        Client client = new Client("127.0.0.1", Integer.parseInt(args[0]));
        client.process();
    }
}

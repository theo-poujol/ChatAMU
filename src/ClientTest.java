import chatamu.protocol.Client;

public class ClientTest {

    public static void main (String[] args) {
        Client client = new Client("localhost", Integer.parseInt(args[0]));
        client.process();
    }
}

import chatamu.protocol.Client;

public class ClientTest {

    public static void main (String[] args) {
//        System.out.println(chatamu.protocol.Protocol.PREFIX.LOGIN.ordinal());
        Client client = new Client("localhost", 1234);
//        client.init();
        client.process();
    }
}

package chatamu.protocol;

import java.io.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private ServerSocketChannel ssc;
    private String hostname;
    private int port;
    private HashMap<SocketChannel, String> clientPool;
    private HashSet<String> namePool;


    // Constructeur on récupère le port du serveur
    public Server(int port) {
        this.hostname = "localhost";
        this.port = port;
        this.clientPool = new HashMap<>();
        this.namePool = new HashSet<>();
    }

    public void run() throws IOException {


        Selector selector = Selector.open();

        this.ssc = ServerSocketChannel.open();
        this.ssc.socket().bind(new InetSocketAddress("localhost", this.port));
        this.ssc.configureBlocking(false);

        this.ssc.register(selector, this.ssc.validOps());


        while (true) {
            selector.select();
            Set<SelectionKey> Keys = selector.selectedKeys();
            Iterator<SelectionKey> Iterator = Keys.iterator();

            while (Iterator.hasNext()) {
                SelectionKey key = Iterator.next();

                // Si une clé est acceptable alors on fait un nouveau channel correspondant à un nouveau client
                if (key.isAcceptable()) {
                    SocketChannel clientSocket = this.ssc.accept();
                    clientSocket.configureBlocking(false);
                    clientSocket.register(selector, SelectionKey.OP_READ);

                    System.out.println("CONNEXION " + clientSocket.getRemoteAddress() + "\n");


                }

                // Si une clé est prête à être lue alors on fait un nouveau channel correspondant à un nouveau client
                else if (key.isReadable()) {



                    SocketChannel clientSocket = (SocketChannel) key.channel();
                    ByteBuffer clientBuffer = ByteBuffer.allocate(1024);
                    clientBuffer.clear();
                    clientSocket.read(clientBuffer);
                    String msg = new String(clientBuffer.array()).trim();
                    String command = parseCommand(msg);
                    switch (command) {
                        case "LOGIN":

                            String pseudo = parseContain(msg, command.length()+1);

                            if (this.clientPool.get(clientSocket) == null) {
                                if (this.namePool.contains(pseudo)) {
                                    String errorLoginMessage = Protocol.PREFIX.ERR_LOG.toString();
                                    ByteBuffer errorBuffer = ByteBuffer.wrap(errorLoginMessage.getBytes());
                                    clientSocket.write(errorBuffer);
                                    clientSocket.close();
                                    break;
                                }
                                else {
//                                    System.out.println(msg);
                                    System.out.println("JOIN " + pseudo);
                                    this.clientPool.put(clientSocket, pseudo);
                                    this.namePool.add(pseudo);
                                    clientSocket.write(clientBuffer);
                                    clientBuffer.clear();
                                    break;
                                }
                            }

                        case "MESSAGE":
//                            System.out.println(msg);
                            String containMsg = parseContain(msg, command.length()+1);
                            String formattedMsg = this.clientPool.get(clientSocket) + "> " + containMsg;
//                            clientBuffer = ByteBuffer.wrap(formattedMsg.getBytes());

                            if (containMsg.equals("STOP")) {
                                System.out.println("DISCONNECTED " + this.clientPool.get(clientSocket));
                                this.namePool.remove(this.clientPool.get(clientSocket));
                                this.clientPool.remove(clientSocket);
                                clientBuffer = ByteBuffer.wrap(Protocol.PREFIX.DCNTD.toString().getBytes());
                                clientSocket.write(clientBuffer);
                                clientSocket.close();
                            }

                            else {
                                System.out.println(formattedMsg);
//                                clientBuffer = ByteBuffer.allocate(formattedMsg.getBytes().length);
//                                clientBuffer.put(formattedMsg.getBytes());
//                                clientBuffer.flip();
//                                /* On envoit le message à tous les clients connectés sur le salon */
//                                for (SocketChannel client : this.clientPool.keySet()) {
//                                    client.write(clientBuffer);
//                                }
                            }
                            clientBuffer = null;
                            break;


                        default:
                            //todo Envoyer au client concerné une erreur de message du protocol

                            String errorLoginMessage = Protocol.PREFIX.ERR_MSG.toString();
                            ByteBuffer errorBuffer = ByteBuffer.wrap(errorLoginMessage.getBytes());
                            errorBuffer.flip();
                            clientSocket.write(errorBuffer);
                            break;


                    }
                }

                else if (key.isWritable()) {

                }
                Iterator.remove();
            }
        }


    }


    public String parseCommand(String str) {

        int ispace = str.indexOf(" ");
        String command = "";
        if (ispace != -1) {
            command = str.substring(0, ispace);
        }

        switch (command) {
            case "LOGIN":
                return "LOGIN";
            case "MESSAGE":
                return "MESSAGE";
            default:
                return "";
        }
    }

    public String parseContain(String str, int index) {
        return str.substring(index);
    }
}

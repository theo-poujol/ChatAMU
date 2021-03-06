package chatamu.protocol;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public class Server {

    private ServerSocketChannel ssc;
    private String hostname;
    private int port;
    private HashMap<SocketChannel, String> clientPool;
    private HashSet<String> namePool;
    private HashMap<SocketChannel, ArrayBlockingQueue<ByteBuffer>> queue;
    private int CAPACITY = 1024;


    // Constructeur on récupère le port du serveur
    // Server de type Java NIO

    public Server(int port) {
        this.hostname = "localhost";
        this.port = port;
        this.clientPool = new HashMap<>();
        this.namePool = new HashSet<>();
        this.queue = new HashMap<>();

    }

    public void run() throws IOException, InterruptedException {


        Selector selector = Selector.open();

        this.ssc = ServerSocketChannel.open();
        this.ssc.socket().bind(new InetSocketAddress("127.0.0.1", this.port));
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
                                //s'il existe déjà un client connecté possedant ce pseudo
                                if (this.namePool.contains(pseudo)) {
                                    String errorLoginMessage = Protocol.PREFIX.ERR_LOG.toString();
                                    ByteBuffer errorBuffer = ByteBuffer.wrap((errorLoginMessage+(char)10).getBytes());
                                    clientSocket.write(errorBuffer);
                                    clientSocket.close();
                                    break;
                                }
                                //attribution et stockage du pseudo
                                else {
                                    System.out.println("JOIN " + pseudo);
                                    this.clientPool.put(clientSocket, pseudo);
                                    this.namePool.add(pseudo);
                                    clientBuffer.flip();
                                    clientSocket.write(clientBuffer);

                                    clientBuffer = ByteBuffer.wrap(("JOIN " + pseudo +(char)10).getBytes());
                                    addMsg2Queue(clientBuffer);
                                    this.queue.put(clientSocket, new ArrayBlockingQueue<ByteBuffer>(CAPACITY));

                                    MessageCheck messageCheck = new MessageCheck(clientSocket, this.queue);
                                    Thread sendingThread = new Thread(messageCheck);
                                    sendingThread.start();
                                    break;
                                }
                            }

                        case "MESSAGE":
                            String containMsg = parseContain(msg, command.length()+1);
                            String formattedMsg = this.clientPool.get(clientSocket) + "> " + containMsg;


                            if (containMsg.equals("STOP")) {
                                pseudo = this.clientPool.get(clientSocket);
                                System.out.println("DISCONNECTED " + pseudo);

                                clientBuffer = ByteBuffer.wrap((Protocol.PREFIX.DCNTD.toString()+(char)10).getBytes());


                                this.namePool.remove(this.clientPool.get(clientSocket));
                                this.clientPool.remove(clientSocket);

                                clientSocket.write(clientBuffer);
                                clientSocket.close();

                                this.queue.remove(clientSocket);

                                clientBuffer = ByteBuffer.wrap(("QUIT " + pseudo +(char)10).getBytes());
                                addMsg2Queue(clientBuffer);
                                break;
                            }

                            else {
                                System.out.println(formattedMsg);
                                clientBuffer = ByteBuffer.wrap((formattedMsg+(char)10).getBytes());
                                addMsg2Queue(clientBuffer);
                                break;
                            }



                        default:
                            break;
                    }
                }
                Iterator.remove();
            }
        }


    }


    // Méthode pour identifier la commande utilisée
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

    // Méthode pour extraire le contenu du message d'un commande
    public String parseContain(String str, int index) {
        return str.substring(index);
    }

    // Ajoute le message en paramètre à toute les files d'attente des clients
    public void addMsg2Queue(ByteBuffer msgBuffer) throws InterruptedException {
        for (SocketChannel clients : this.queue.keySet()) {
            this.queue.get(clients).put(msgBuffer);
        }
    }




    //Classe qui s'occupe de distribuer les messages contenus dans la file d'attente du client concerné (Thread donc une instance par client connecté)
    private class MessageCheck implements Runnable {

        private SocketChannel client;
        private ArrayBlockingQueue<ByteBuffer> Clientqueue;

        private MessageCheck(SocketChannel client, HashMap<SocketChannel, ArrayBlockingQueue<ByteBuffer>> queue) {
            this.client = client;
            this.Clientqueue = queue.get(this.client);
        }

        @Override
        public void run() {


            while (true) {
                if (!(this.Clientqueue.isEmpty())) {
                    for (ByteBuffer buffer : this.Clientqueue) {
                        try {

                                int i = this.client.write(buffer);
                                while(i == 0)
                                {
                                   i = this.client.write(buffer);
                                }
                                this.Clientqueue.remove(buffer);




                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}




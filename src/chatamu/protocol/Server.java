package chatamu.protocol;

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
    //todo peut être mettre une key ?
    private HashMap<SelectionKey, String> clientPool;
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
        this.ssc.socket().bind(new InetSocketAddress("localhost",this.port));
        this.ssc.configureBlocking(false);

        this.ssc.register(selector, this.ssc.validOps());



        while(true) {
            selector.select();
            Set<SelectionKey> Keys = selector.selectedKeys();
            Iterator<SelectionKey> Iterator = Keys.iterator();

            while (Iterator.hasNext())
            {
                SelectionKey key = Iterator.next();

                // Si une clé est acceptable alors on fait un nouveau channel correspondant à un nouveau client
                if (key.isAcceptable())
                {
                    SocketChannel clientSocket = this.ssc.accept();
                    clientSocket.configureBlocking(false);
                    clientSocket.register(selector, SelectionKey.OP_READ);
                    System.out.println("Connexion Acceptée: " + clientSocket.getRemoteAddress() + "\n");




                    ByteBuffer nameBuffer = ByteBuffer.allocate(256);
                    clientSocket.read(nameBuffer);
                    String pseudo = new String(nameBuffer.array()).trim();

                    ByteBuffer errorBuffer;

                    // PSEUDO DEJA PRESENT DANS LA SALLE
                    if (namePool.contains(pseudo)) {
                        errorBuffer = ByteBuffer.allocate(2);
                        String errorLoginMessage = "3";
                        ByteBuffer.wrap(errorLoginMessage.getBytes());
                        errorBuffer.flip();
                        clientSocket.write(errorBuffer);
                        clientSocket.close();
                    }

                    else {
                        this.namePool.add(pseudo);
                        this.clientPool.put(key, pseudo);
                        System.out.println(pseudo + " a rejoint de salon");
                    }


                }

                // Si une clé est prête à être lue alors on fait un nouveau channel correspondant à un nouveau client
                else if (key.isReadable())
                {
                    SocketChannel clientSocket = (SocketChannel) key.channel();



//                    String pseudoRequest = "Entrer un pseudo svp";
//                    ByteBuffer buffer = ByteBuffer.wrap(pseudoRequest.getBytes());
//                    buffer.flip();
//                    clientSocket.write(buffer);
//                    buffer.clear();
//                    buffer = ByteBuffer.allocate(256);
//                    clientSocket.read(buffer);
//                    String pseudo = new String(buffer.array()).trim();
//
//                    while(this.clientPool.get(pseudo) != null) {
//                        buffer = ByteBuffer.wrap(pseudoRequest.getBytes());
//                        buffer.flip();
//                        clientSocket.write(buffer);
//                        buffer.clear();
//                        buffer = ByteBuffer.allocate(256);
//                        clientSocket.read(buffer);
//                        pseudo = new String(buffer.array()).trim();
//                    }
//
//                    this.clientPool.put(pseudo, clientSocket);
//                    buffer = ByteBuffer.allocate(256);
//                    clientSocket.read(buffer);



                    ByteBuffer buffer2 = ByteBuffer.allocate(256);
                    clientSocket.read(buffer2);

                    String msg = new String(buffer2.array()).trim();




                    if (msg.equals("Close")) {
                        System.out.println("Message reçu: " + msg);
                        buffer2.flip();
                        clientSocket.write(buffer2);
                        clientSocket.close();

                    }
                    else if (msg.equals("")) {
                        System.out.println("Connexion fermée");
                        buffer2.flip();
                        clientSocket.write(buffer2);
                        clientSocket.close();
                    }

                    else {
                        System.out.println("Message reçu: " + msg);
                        buffer2.flip();
                        clientSocket.write(buffer2);
                    }


                }
                Iterator.remove();
            }
        }



    }

}

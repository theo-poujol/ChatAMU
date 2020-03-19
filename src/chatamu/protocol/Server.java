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




                }

                // Si une clé est prête à être lue alors on fait un nouveau channel correspondant à un nouveau client
                else if (key.isReadable())
                {


                    SocketChannel clientSocket = (SocketChannel) key.channel();


                    if (this.clientPool.get(key) == null) {
                        System.out.println("PSEUDO PAS RENSEIGNE");
                        ByteBuffer nameBuffer = ByteBuffer.allocate(256);
                        clientSocket.read(nameBuffer);
                        String pseudo = new String(nameBuffer.array()).trim();

                        if (this.namePool.contains(pseudo)) {
                            System.out.println("Pseudo déjà dans la salle");
                            ByteBuffer errorBuffer = ByteBuffer.allocate(256);
                            String errorLoginMessage = Protocol.PREFIX.ERR_LOG.toString();
                            ByteBuffer.wrap(errorLoginMessage.getBytes());
                            errorBuffer.flip();
                            clientSocket.write(errorBuffer);
                            errorBuffer.clear();
                            clientSocket.close();
                        }

                        else {

                            this.clientPool.put(key, pseudo);
                            this.namePool.add(pseudo);
                            System.out.println(pseudo + " vient de rejoindre le salon.");
                        }

                    }

                    else {
                        System.out.println("PSEUDO RENSEIGNE");
                        ByteBuffer echoBuffer = ByteBuffer.allocate(256);
                        clientSocket.read(echoBuffer);

                        String msg = new String(echoBuffer.array()).trim();

                        if (msg.equals("Close")) {
                            System.out.println("Message reçu: " + msg);
                            echoBuffer.flip();
                            clientSocket.write(echoBuffer);
                            clientSocket.close();

                        }
                        else if (msg.equals("")) {
                            System.out.println("Connexion fermée");
                            echoBuffer.flip();
                            clientSocket.write(echoBuffer);
                            clientSocket.close();
                        }

                        else {
                            System.out.println("Message reçu: " + msg);
                            echoBuffer.flip();
                            clientSocket.write(echoBuffer);
                        }
                    }






//                    if (this.clientPool.get(key) == null) {
//                        ByteBuffer nameBuffer = ByteBuffer.allocate(256);
//                        clientSocket.read(nameBuffer);
//                        String pseudo = new String(nameBuffer.array()).trim();
//                        System.out.println(pseudo);
//                        if (!pseudo.equals("")) {
//
//                            // PSEUDO DEJA PRESENT DANS LA SALLE
//                            if (namePool.contains(pseudo)) {
//                                System.out.println("Pseudo déjà dans la salle");
//                                ByteBuffer errorBuffer = ByteBuffer.allocate(2);
//                                String errorLoginMessage = "3";
//                                ByteBuffer.wrap(errorLoginMessage.getBytes());
//                                errorBuffer.flip();
//                                clientSocket.write(errorBuffer);
//                                errorBuffer.clear();
//                                clientSocket.close();
//                            }
//
//                            else {
////                                System.out.println("On ajoute le pseudo");
////                                this.namePool.add(pseudo);
////                                this.clientPool.put(key, pseudo);
//                                System.out.println(pseudo + " a rejoint le salon");
//                                clientSocket.close();
//                            }
//                        }
//                    }
//
//                    else {
//
//                        ByteBuffer echoBuffer = ByteBuffer.allocate(256);
//                        clientSocket.read(echoBuffer);
//
//                        String msg = new String(echoBuffer.array()).trim();
//
//                        if (msg.equals("Close")) {
//                            System.out.println("Message reçu: " + msg);
//                            echoBuffer.flip();
//                            clientSocket.write(echoBuffer);
//                            clientSocket.close();
//
//                        }
//                        else if (msg.equals("")) {
//                            System.out.println("Connexion fermée");
//                            echoBuffer.flip();
//                            clientSocket.write(echoBuffer);
//                            clientSocket.close();
//                        }
//
//                        else {
//                            System.out.println("Message reçu: " + msg);
//                            echoBuffer.flip();
//                            clientSocket.write(echoBuffer);
//                        }
//                    }
                }
                Iterator.remove();
            }
        }



    }

}

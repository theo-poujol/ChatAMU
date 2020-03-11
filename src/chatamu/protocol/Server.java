package chatamu.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    ServerSocketChannel ssc;
    int port;

    // Constructeur on récupère le port du serveur
    public Server(int port) {this.port = port;}

    public void run() throws IOException {


        Selector selector = Selector.open();

        // On créée la channel du serversocket puis on la bind
        // On n'oublie pas de la passer en non bloquante
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
                    SocketChannel client = this.ssc.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Connexion Acceptée: " + client.getRemoteAddress() + "\n");
                }

                // Si une clé est prête à être lue alors on fait un nouveau channel correspondant à un nouveau client
                else if (key.isReadable())
                {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    client.read(buffer);

                    String msg = new String(buffer.array()).trim();




                    if (msg.equals("Close")) {
                        System.out.println("Message reçu: " + msg);
                        buffer.flip();
                        client.write(buffer);
                        client.close();

                    }
                    else if (msg.equals("")) {
                        System.out.println("Connexion fermée");
                        buffer.flip();
                        client.write(buffer);
                        client.close();
                    }

                    else {
                        System.out.println("Message reçu: " + msg);
                        buffer.flip();
                        client.write(buffer);
                    }


                }
                Iterator.remove();
            }
        }



    }

}

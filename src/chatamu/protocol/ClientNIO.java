package chatamu.protocol;

import chatamu.exception.LoginException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientNIO {

    private InetSocketAddress clientAddr;
    private SocketChannel client;
    private ByteBuffer readBuffer;

    public ClientNIO(String address, int port) throws IOException {
        this.clientAddr = new InetSocketAddress(address,port);
        this.client = SocketChannel.open(this.clientAddr);
        System.out.println("Connexion à " +  address + " au port " + port);
    }


    public void process() throws IOException, InterruptedException {
        read();
        login();
        writing();
    }


    public void read() {
        Thread thread = new Thread(new HandleReceiv(this.client));
        thread.start();
    }

    public void writing() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            if (message != null) {
                byte[] messageByte = (Protocol.PREFIX.MESSAGE.toString() + message).getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(messageByte);
//                buffer.flip();
                this.client.write(buffer);
//                buffer.compact();
            }
        }
    }

    public void login() throws IOException {
        System.out.println("Entrer un pseudo");

        Scanner scanner = new Scanner(System.in);

        byte[] pseudo = (Protocol.PREFIX.LOGIN.toString() + scanner.nextLine()).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(pseudo);
//        buffer.flip();
        this.client.write(buffer);
//        buffer.compact();
    }


    private final class HandleReceiv implements Runnable {

        SocketChannel client;

        public HandleReceiv(SocketChannel socketChannel) {

            this.client = socketChannel;

        }

        @Override
        public void run() {

            try {
                while (true) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    buffer.clear();
                    this.client.read(buffer);
                    String message = new String(buffer.array()).trim();

                    if (message.equals(Protocol.PREFIX.ERR_LOG.toString())) {
                        this.client.close();
                        throw new LoginException();
                    }

                    else if (message.equals(Protocol.PREFIX.MESSAGE.toString())) {
                        System.out.println("ERROR Chatamu");
                    }

                    else if (message.equals(Protocol.PREFIX.DCNTD.toString())) {
                        System.out.println(message);
                        this.client.close();
                        System.exit(1);
                    }

                    else System.out.println(message);
                }

            }

            catch(IOException e) {
                e.printStackTrace();
            }

            catch (LoginException e) {
                e.getMessage();
                System.exit(1);
            }
        }
    }



}

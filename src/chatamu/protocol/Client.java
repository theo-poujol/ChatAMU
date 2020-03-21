package chatamu.protocol;

import chatamu.exception.LoginException;
import chatamu.exception.MessageException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private SocketChannel socket;

    public Client(String server,int port)
    {
        try {
            this.clientSocket = new Socket();
            this.clientSocket.connect(new InetSocketAddress(server, port));
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
        }

        catch(IOException exception) {
            exception.printStackTrace();
        }
    }


    public void process() {
        try {

            Thread thread_rcv = new Thread(new HandleReceive(this, this.in));
            thread_rcv.start();

            //todo Faire un booléen propre (Gérer la terminaison du while proprement)
            boolean bool = true;
            boolean queryNickName = false;
            while(bool)
            {

                Scanner scanner = new Scanner(System.in);
                if (!(queryNickName)) {
                    System.out.println("Entrer un pseudo...");
                    String pseudo = scanner.nextLine();
                    this.out.write(Protocol.PREFIX.LOGIN.toString() + pseudo);
                    this.out.newLine();
                    this.out.flush();
                    queryNickName = true;
                }


                String message = scanner.nextLine();
                if (message != null) {
                    this.out.write(Protocol.PREFIX.MESSAGE.toString() + message);
                    this.out.newLine();
                    this.out.flush();

//                    if (message.equals("STOP") || message.equals("")) {
//                        this.in.close();
//                        this.out.close();
//                        scanner.close();
//                        this.clientSocket.close();
//                        bool = false;
//                    }
                }

            }
        }
        catch(Exception exception) {
            exception.printStackTrace();
        }
    }


    private final class HandleReceive implements Runnable {
        private BufferedReader in;
        private Client client;

        HandleReceive(Client client, BufferedReader in)
        {
            this.client = client;
            this.in = in;
        }


        @Override
        public void run() {
            try
            {
                while (true)
                {
                    String response = in.readLine();
                    in.reset();
                    if (response != null) {
                        if (response.equals(Protocol.PREFIX.ERR_LOG.toString())) {
                            this.client.clientSocket.close();
                            throw new LoginException();
                        }
                        else if (response.equals(Protocol.PREFIX.ERR_MSG.toString())) throw new MessageException();
                        else if (response.equals(Protocol.PREFIX.DCNTD.toString())) {
                            this.client.clientSocket.close();
                            System.out.println(response);
                            System.exit(1);
                        }
                    }
                }
            }
            catch (IOException exception)
            {
                System.out.println("Vous avez quitté le salon.");
            }

            catch (LoginException exception) {
                System.out.println(exception.getMessage());
                System.exit(1);
            }

            catch (MessageException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    public void setClientSocket(SocketChannel socket) { this.socket = socket; }
    public SocketChannel getSocket() { return this.socket; }
}

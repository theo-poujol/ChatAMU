package chatamu.protocol;

import chatamu.exception.LoginException;
import chatamu.exception.MessageException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private SocketChannel socket;

    // Le constructeur du client nous connecte au serveur
    // Et initialise ses opérations d'entrées/sorties.
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


            // Booléen nous indiquant si la requête de pseudo est déjà effectuée.
            boolean queryNickName = false;
            while(true)
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
                }
            }
        }
        catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    // Classe utilsée pour notre Thread s'occupant de lire les messages reçus.
    // Nous permettant d'effectuer des entrées/sorties de manière concurrente.

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

                    String response = "";
                    StringBuilder stb = new StringBuilder();
                    int i;

                    // 10 est l'ASCII du fin de ligne sous UNIX
                    // On lit donc la réponse du serveur jusqu'à la fin de ligne
                    while ((i = in.read()) != 10) {
//                        System.out.println("IL Y A : " + (char)i);
                        stb.append((char)i);
                        response = stb.toString();
                    }

//                    while  ((response = this.in.readLine()) != null) {
//
//                        if (response.equals(Protocol.PREFIX.ERR_LOG.toString())) {
//                            this.client.clientSocket.close();
//                            throw new LoginException();
//                        }
//                        else if (response.equals(Protocol.PREFIX.MESSAGE.toString())) {
//                            System.out.println("ERROR Chatamu");
//                        }
//                        else if (response.equals(Protocol.PREFIX.DCNTD.toString())) {
//                            this.client.clientSocket.close();
//                            System.out.println(response);
//                            System.exit(1);
//                        }
//                        else System.out.println("Je recois : " + response);
//                        System.out.println("JE LIS");
//                    }

                    if (response.equals(Protocol.PREFIX.ERR_LOG.toString())) {
                        this.client.clientSocket.close();
                        throw new LoginException();
                    }
                    else if (response.equals(Protocol.PREFIX.MESSAGE.toString())) {
                        System.out.println("ERROR Chatamu");
                    }
                    else if (response.equals(Protocol.PREFIX.DCNTD.toString())) {
                        this.client.clientSocket.close();
                        System.out.println(response);
                        System.exit(1);
                    }
                    else System.out.println("Je recois : " + response);
                }
            }

            catch (IOException exception)
            {
                System.out.println("Vous avez quitté le salon.");
            }

            // Exception lancée sur erreur lors de la connexion
            catch (LoginException exception) {
                System.out.println(exception.getMessage());
                System.exit(1);
            }
        }
    }
}

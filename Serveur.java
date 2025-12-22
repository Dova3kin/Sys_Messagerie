import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.io.*;

class Serveur {

    private static void analyse(String demande) {
        String sender, code;
        sender = demande.split(":")[0];
        code = demande.split(":")[1];

        if (code.equals("001")) { // 001 : création du compte client + connexion
            String msg = sender;
            try {
                Files.write(Paths.get("SERVEUR/clients.txt"), msg.getBytes(), StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
                System.out.println(sender + " connecté");
            } catch (Exception E) {
            }
        } else if (code.equals("002")) // 002 : connexion
            System.out.println(sender + " connecté");
    }

    public static void main(String args[]) {
        ServerSocket server = null;
        Socket sock = null;
        PrintWriter reponseServeur = null;
        BufferedReader demandeClient = null;
        String demande;
        Scanner sc = new Scanner(System.in);
        try {
            server = new ServerSocket(7770);
            while (true) {
                sock = server.accept();
                demandeClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                reponseServeur = new PrintWriter(sock.getOutputStream(), true);
                demande = demandeClient.readLine();
                analyse(demande);
                reponseServeur.close();
                sock.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                server.close();
            } catch (IOException e2) {
                System.out.println(e2.getMessage());
            }
        }
    }

}
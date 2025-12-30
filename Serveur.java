import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

class Serveur {
    private static ArrayList<Client> clients = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private static void loadClient() {
        File fichier = new File("SERVEUR/clients.ser");
        if (fichier.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier))) {
                clients = (ArrayList<Client>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void addClient(Client client) {
        clients.add(client);
        File dossier = new File("SERVEUR");
        if (!dossier.exists())
            dossier.mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("SERVEUR/clients.ser"))) {
            oos.writeObject(clients);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    public static synchronized void saveAll() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("SERVEUR/clients.ser"))) {
            oos.writeObject(clients);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    public static synchronized Client getClient(String tel) {
        for (Client c : clients) {
            if (c.getTel().equals(tel))
                return c;
        }
        return null;
    }

    public static synchronized ArrayList<Client> getAllClient() {
        return clients;
    }

    public static void main(String args[]) {
        loadClient();
        Socket sock = null;
        try (ServerSocket server = new ServerSocket(7770)) {
            System.out.println("Démarage du serveur...");
            Thread.sleep(1000);
            System.out.println("Serveur démaré");

            while (true) {
                sock = server.accept();
                System.out.println("Tentative de connexion...");
                Thread t = new Thread(new GestionUtilisateur(sock));
                t.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
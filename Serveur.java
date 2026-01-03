import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class Serveur {
    private static ArrayList<Client> clients = new ArrayList<>();
    private static List<GestionUtilisateur> observateurs = new CopyOnWriteArrayList<>();

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

    public static void updateClient(String tel, Client clientAJour) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getTel().equals(tel)) {
                clients.set(i, clientAJour);
                saveAll();
                break;
            }
        }
    }

    public static void ajouterObservateur(GestionUtilisateur obs) {
        observateurs.add(obs);
    }

    public static void supprimerObservateur(GestionUtilisateur obs) {
        observateurs.remove(obs);
    }

    public static synchronized void diffuserNotificationGlobale(String message, String telExpediteur) {
        for (Client c : clients) {
            if (c.getTel().equals(telExpediteur))
                continue;
            if (GestionUtilisateur.getClientsCo().containsKey(c.getTel())) {
                for (GestionUtilisateur gu : observateurs) {
                    if (gu.getTelClient().equals(c.getTel())) {
                        gu.recevoirNotification(message);
                        break;
                    }
                }
            } else {
                c.addNotif(message);
            }
        }
        saveAll();
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
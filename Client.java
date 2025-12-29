import java.io.*;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Client implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tel, nom, prenom, dossierPerso;
    private ArrayList<String> conversations = new ArrayList<>();
    private transient Socket soc = null;
    private transient BufferedReader reponseServeur = null;
    private transient PrintWriter requeteServeur = null;
    private transient ObjectInputStream ois = null;

    /**
     * @param tel
     * @throws ClientAlreadyExistException
     */
    public void setClient(String tel) throws ClientAlreadyExistException {
        this.tel = tel;
        dossierPerso = "CLIENT/" + tel;
        try {
            sendRequest("101"); // 101 : Demande d'inscription
            String code = getResponse().split(":")[0];
            if (code.equals("200")) {
                Path path = Paths.get(dossierPerso);
                Files.createDirectories(path);
                System.out.println("Compte créé avec Succes");
            } else if (code.equals("201")) {
                throw new ClientAlreadyExistException();
            }
        } catch (FileAlreadyExistsException fae) {
            throw new ClientAlreadyExistException();
        } catch (ClientAlreadyExistException ce) {
            throw new ClientAlreadyExistException();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean connect() {
        try {
            soc = new Socket("localhost", 7770);
            reponseServeur = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            requeteServeur = new PrintWriter(soc.getOutputStream(), true);
            ois = new ObjectInputStream(soc.getInputStream());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendRequest(String code) {
        requeteServeur.println(getTel() + ":" + code);
    }

    public void sendRequest(String code, Client recepteur, String message) {
        requeteServeur.println(getTel() + ":" + code + ":" + recepteur + ":" + message);
    }

    public void sendRequest(String tel, String code) {
        requeteServeur.println(tel + ":" + code);
    }

    public String getResponse() throws IOException {
        return reponseServeur.readLine();
    }

    public ObjectInputStream getObjectResponse() throws IOException {
        return ois;
    }

    public String getTel() {
        return tel;
    }

    public String getPrenom() {
        return (prenom == null) ? "" : prenom;
    }

    public String getNom() {
        return (nom == null) ? "" : nom;
    }

    public ArrayList<String> getConv() {
        return conversations;
    }

    public Socket getSocket() {
        return soc;
    }

    private static void wait(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void link(String tel) throws NotFoundClientException {
        sendRequest(tel, "001"); // Envoie la demande de connexion
        try {
            String reponse = getResponse();
            String[] data = reponse.split(":");
            String code = data[0];

            if (code.equals("200")) {
                this.tel = tel;
                if (data.length > 2) {
                    this.nom = data[1];
                    this.prenom = data[2];
                }
            } else {
                throw new NotFoundClientException(tel);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void setTel(String tel) {
        this.tel = tel;
        this.dossierPerso = "CLIENT/" + tel;
    }

    private void message() {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equals("3")) {
            System.out.println("==========MESSAGE==========");
            System.out.println("1 : Conversations");
            System.out.println("2 : Nouvelles conversations");
            System.out.println("3 : Retour");
            input = scanner.nextLine();
            Chat chat = new Chat(this);
            switch (input) {
                case "1":
                    chat.openChat();
                    break;
                case "2":
                    chat.createChat();
                    break;
                case "3":
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public String toString() {
        return tel + (prenom != null ? " " + prenom : "") + " " + (nom != null ? nom : "");
    }

    public static void main(String[] args) {
        Client c = new Client();
        new Connexion(c);
        System.out.println("Bienvenue " + (c.getPrenom() != null ? c.getPrenom() : ""));
        wait(500);
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equals("4")) {
            System.out.println("==========ACCUEIL==========");
            System.out.println("1 : Messages");
            System.out.println("2 : Notifications");
            System.out.println("3 : Options");
            System.out.println("4 : Déconnexion");
            input = scanner.nextLine();
            switch (input) {
                case "1":
                    c.message();
                    break;
                case "2":
                    break;
                case "3":
                    break;
                case "4":
                    try {
                        c.getSocket().close();
                    } catch (IOException ioe) {
                        System.out.println(ioe.getMessage());
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

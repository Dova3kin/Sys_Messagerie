import java.io.*;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Client {
    private String tel;
    private String nom;
    private String prenom;
    private String dossierPerso;
    private BufferedReader reponseServeur = null;
    private PrintWriter requeteServeur = null;

    /**
     * @param tel
     * @throws ClientAlreadyExistException
     *                                     Créer le clien sur le serveur, et créer
     *                                     un fichier client localement
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
        Socket soc = null;
        try {
            soc = new Socket("localhost", 7770);
            reponseServeur = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            requeteServeur = new PrintWriter(soc.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void sendRequest(String code) {
        requeteServeur.println(getTel() + ":" + code);
    }

    private void sendRequest(String tel, String code) {
        requeteServeur.println(tel + ":" + code);
    }

    private String getResponse() throws IOException {
        return reponseServeur.readLine();
    }

    public String getTel() {
        return tel;
    }

    public String getPrenom() {
        return prenom;
    }

    private static void wait(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void link(String tel) throws NotFoundClientException {
        sendRequest(tel, "001");
        try {
            if (!getResponse().split(":")[0].equals("200"))
                throw new NotFoundClientException(tel);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
            switch (input) {
                case "1":

                    break;
                case "2":

                    break;
                case "3":
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) {
        Client c = new Client();
        new Connexion(c);
        System.out.println("Bienvenue " + ((c.getPrenom() != null) ? c.getPrenom() : ""));
        wait(500);
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equals("3")) {
            System.out.println("==========ACCUEIL==========");
            System.out.println("1 : Messages");
            System.out.println("2 : Options");
            System.out.println("3 : Déconnexion");
            input = scanner.nextLine();
            switch (input) {
                case "1":
                    c.message();
                    break;
                case "2":
                    break;
                case "3":
                    break;
                default:
                    break;
            }
        }
    }
}

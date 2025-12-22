import java.io.*;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private final String tel;
    private String nom;
    private String prenom;
    private String dossierPerso;
    private BufferedReader reponseServeur = null;
    private PrintWriter requeteServeur = null;
    private static String telRegex = "^0[0-9]{9}$";
    private static Pattern pattern = Pattern.compile(telRegex);

    public Client(String tel) throws Exception {
        this.tel = tel;
        String dossierPerso = "CLIENT/" + tel;
        try {
            Path path = Paths.get(dossierPerso);
            Files.createDirectory(path);
            System.out.println("Compte créé avec Succes... Connexion");
        } catch (FileAlreadyExistsException fae) {
            throw new Exception("Compte déjà existant !");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean connect() {
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

    private String getResponse() throws IOException {
        return reponseServeur.readLine();
    }

    public String getTel() {
        return tel;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String idConnexion;
        boolean connecte = false;

        /**
         * Connexion au serveur ou création du compte
         */
        if (connect())
            while (!connecte) {
                System.out.println("Veuillez rentrer votre identifiant (si vous êtes nouveau, rentrez \"n\"");
                idConnexion = scan.nextLine();

                if (idConnexion.equals("n")) {
                    System.out.println(
                            "Bienvenue, veuillez rentrer votre numéro dé téléphone, celui-ci deviendra votre identifiant de connexions");

                    while (!connecte) {
                        idConnexion = scan.nextLine();
                        Matcher matcher = pattern.matcher(idConnexion);

                        if (matcher.matches()) {
                            try {
                                Client c = new Client(idConnexion);
                                c.sendRequest("001");
                                connecte = true;
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                break;
                            }
                        } else {
                            System.out.println("Veuillez donner un numéro de téléphone avec le format \"0XXXXXXXXX\"");
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                            }
                            System.out.print("Numéro de téléphone : ");
                        }
                    }
                } else {
                    connecte = true;
                }
                System.out.println("connecté");
            }
        else {
            System.out.println("Serveur hors connexion");
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
            System.out.println("Veuillez appeler le service de maintenance pour signaler le problème");
        }

    }

}

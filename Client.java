import java.util.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Client {
    private final String tel;
    private String nom;
    private String prenom;
    private String dossierPerso;
    private static String telRegex = "^0[0-9]{9}$";
    private static Pattern pattern = Pattern.compile(telRegex);

    public Client(String tel) {
        this.tel = tel;
        String dossierPerso = "CLIENT/" + tel;
        try {
            Path path = Paths.get(dossierPerso);
            Files.createDirectory(path);
            System.out.println("Compte créé avec Succes... Connexion");
        } catch (FileAlreadyExistsException fae) {
            System.out.println("Compte déjà existant !");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void connect() {
        Socket soc = null;
        try {
            soc = new Socket("localhost", 7770);
            BufferedReader buffReader = new BufferedReader(
                    new InputStreamReader(soc.getInputStream()));
            String ligne;
            while ((ligne = buffReader.readLine()) != null)
                System.out.println(ligne);
        } catch (IOException e) {
        }
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String idConnexion;
        boolean connecte = false;

        /**
         * Connexion au serveur ou création du compte
         */
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
                        Client c = new Client(idConnexion);
                        connecte = true;
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
        }

    }
}

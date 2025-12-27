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
    private String tel;
    private String nom;
    private String prenom;
    private String dossierPerso;
    private BufferedReader reponseServeur = null;
    private PrintWriter requeteServeur = null;
    private static String telRegex = "^0[0-9]{9}$";
    private static Pattern pattern = Pattern.compile(telRegex);

    public Client() {
    }

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

    private boolean connect() {
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

    private static void wait(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void link(String tel) {
        sendRequest(tel, "001");
        try {
            if (!getResponse().split(":")[0].equals("200"))
                System.out.println('a');
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;
        boolean identifie = false;
        Client c = new Client();

        if (c.connect()) {
            while (!identifie) {
                System.out.println("Veuillez rentrer votre identifiant (si vous êtes nouveau, rentrez \"n\"");
                input = scanner.nextLine();
                if (input.equals("n")) {
                    System.out.println(
                            "Bienvenue, veuillez rentrer votre numéro dé téléphone, celui-ci deviendra votre identifiant de connexions");
                    while (!identifie) {
                        input = scanner.nextLine();
                        Matcher matcher = pattern.matcher(input);
                        if (matcher.matches()) {
                            try {
                                c.setClient(input);
                                identifie = true;
                            } catch (ClientAlreadyExistException caee) {
                                System.out.println(caee.getMessage());
                                wait(500);
                                System.out.print("Identifiant : ");
                                input = scanner.nextLine();
                                matcher = pattern.matcher(input);
                                if (matcher.matches()) {
                                    try {
                                        c.link(input);
                                        identifie = true;
                                    } catch (Exception e) {
                                        System.out.println("link error : " + e.getMessage());
                                    }
                                } else {
                                    System.out.println("Identifiant inconnu");
                                }
                            }
                        } else {
                            System.out.println("Veuillez donner un numéro de téléphone avec le format \"0XXXXXXXXX\"");
                            wait(500);
                            System.out.print("Numéro de téléphone : ");
                        }
                    }
                } else {
                    Matcher matcher = pattern.matcher(input);
                    if (matcher.matches()) {
                        try {
                            c.link(input);
                            identifie = true;
                        } catch (Exception e) {
                            System.out.println("link error : " + e.getMessage());
                        }
                    } else {
                        System.out.println("Identifiant inconnu");
                    }
                }
            }
        } else {
            System.out.println("Serveur éteint");
        }

    }
}

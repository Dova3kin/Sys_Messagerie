import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connexion {
    private static String telRegex = "^0[0-9]{9}$";
    private static Pattern pattern = Pattern.compile(telRegex);

    public Connexion(Client c) {
        Scanner scanner = new Scanner(System.in);
        String input;
        boolean identifie = false;

        if (c.connect()) {
            while (!identifie) {
                System.out.println("Veuillez rentrer votre identifiant (si vous êtes nouveau, rentrez \"n\")");
                System.out.print(">");
                input = scanner.nextLine();
                if (input.equals("n")) {
                    System.out.println(
                            "Bienvenue, veuillez rentrer votre numéro dé téléphone, celui-ci deviendra votre identifiant de connexions");
                    System.out.print(">");
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
                                    } catch (NotFoundClientException nfce) {
                                        System.out.println("Identifiant inconnu");
                                        System.out.print(">");
                                    }
                                } else {
                                    System.out.println(
                                            "Veuillez donner un numéro de téléphone avec le format \"0XXXXXXXXX\"");
                                    System.out.print(">");
                                    wait(500);
                                    System.out.println("Identifiant : ");
                                }
                            }
                        } else {
                            System.out
                                    .println("Veuillez donner un numéro de téléphone avec le format \"0XXXXXXXXX\"");
                            System.out.print(">");
                            wait(500);
                            System.out.print("Numéro de téléphone : ");
                        }
                    }
                } else {
                    while (!identifie) {
                        Matcher matcher = pattern.matcher(input);
                        if (matcher.matches()) {
                            try {
                                c.link(input);
                                identifie = true;
                            } catch (NotFoundClientException e) {
                                System.out.println("Identifiant inconnu");
                                break;
                            }
                        } else {
                            System.out
                                    .println("Veuillez donner un numéro de téléphone avec le format \"0XXXXXXXXX\"");
                            System.out.print(">");
                            wait(500);
                            System.out.print("Numéro de téléphone : ");
                            input = scanner.nextLine();
                        }
                    }
                }
            }
        } else {
            System.out.println("Serveur éteint");
            System.exit(0);
        }

    }

    private static void wait(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

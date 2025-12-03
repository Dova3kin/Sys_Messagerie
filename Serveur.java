import java.net.*;
import java.util.Scanner;
import java.io.*;

class Serveur {

    public static void main(String args[]) {
        ServerSocket server = null;
        Socket sock = null;
        PrintWriter sockOut = null;
        String str;
        Scanner sc = new Scanner(System.in);
        try {
            server = new ServerSocket(7770);
            while (true) {
                sock = server.accept();
                sockOut = new PrintWriter(
                        sock.getOutputStream(), true);
                BufferedReader sockIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                do {
                    str = sc.nextLine();
                    sockOut.println(str);
                } while (!str.equals("q"));
                sockOut.println("test");
                sockOut.close();
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
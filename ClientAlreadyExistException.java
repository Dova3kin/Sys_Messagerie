public class ClientAlreadyExistException extends Exception {

    public ClientAlreadyExistException() {
        super("Vous avez déjà un compte");
    }
}

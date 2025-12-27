public class NotFoundClientException extends Exception {
    public NotFoundClientException(String tel) {
        super(tel + " Not Found");
    }
}

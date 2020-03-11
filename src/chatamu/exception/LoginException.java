package chatamu.exception;

public class LoginException extends Exception {

    @Override
    public String getMessage() {
        return "ERROR LOGIN aborting chatamu protocol";
    }



}

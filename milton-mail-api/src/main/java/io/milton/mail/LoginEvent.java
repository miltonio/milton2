package io.milton.mail;

/**
 *
 */
public class LoginEvent implements Event{
    final private String username;
    final private String password;

    private boolean loginSuccessful;

    public LoginEvent(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setLoginSuccessful(boolean loginSuccessful) {
        this.loginSuccessful = loginSuccessful;
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }


    
}

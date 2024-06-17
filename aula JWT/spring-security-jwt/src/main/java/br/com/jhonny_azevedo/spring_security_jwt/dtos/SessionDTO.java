package br.com.jhonny_azevedo.spring_security_jwt.dtos;

public class SessionDTO {

    private String login;
    private String token;

    public SessionDTO() {
    }

    public SessionDTO(String login, String token) {
        this.login = login;
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

package br.com.jhonny_azevedo.spring_security_jwt.security;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class JWTObject {

    private String subject; // nome do usuário
    private Date issuedAt; // data de criação do token
    private Date expiration; // data de espiração do token
    private List<String> roles; // roles do usuário

    public JWTObject() {
    }

    // "String ... roles" uma outra forma de passar um Array como parâmetro
    public JWTObject(String subject, Date issuedAt, Date expiration, String ... roles) {
        this.subject = subject;
        this.issuedAt = issuedAt;
        this.expiration = expiration;
        this.roles = Arrays.asList(roles);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public List<String> getRoles() {
        return roles;
    }

    // "String ... roles" uma outra forma de passar um Array como parâmetro
    public void setRoles(String ... roles) {
        this.roles = Arrays.asList(roles);
    }

    @Override
    public String toString() {
        return "JWTObject{" +
                "subject='" + subject + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiration=" + expiration +
                ", roles=" + roles +
                '}';
    }
}

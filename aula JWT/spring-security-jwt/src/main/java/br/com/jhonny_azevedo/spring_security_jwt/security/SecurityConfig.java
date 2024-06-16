package br.com.jhonny_azevedo.spring_security_jwt.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
// Os atributos da classe serão preenchidos com os valores contido
// no prefixo "security.config" do arquivo application.properties
@ConfigurationProperties(prefix = "security.config")
public  class SecurityConfig {

    public static String PREFIX;
    public static String KEY;
    public static Long EXPIRATION;

    public SecurityConfig() {
    }

    public SecurityConfig(String PREFIX, String KEY, Long EXPIRATION) {
        this.PREFIX = PREFIX;
        this.KEY = KEY;
        this.EXPIRATION = EXPIRATION;
    }

    public String getPREFIX() {
        return PREFIX;
    }

    public String getKEY() {
        return KEY;
    }

    public Long getEXPIRATION() {
        return EXPIRATION;
    }

    public void setPREFIX(String PREFIX) {
        this.PREFIX = PREFIX;
    }

    public void setKEY(String KEY) {
        this.KEY = KEY;
    }

    public void setEXPIRATION(Long EXPIRATION) {
        this.EXPIRATION = EXPIRATION;
    }
}

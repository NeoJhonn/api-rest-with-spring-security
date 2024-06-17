package br.com.jhonny_azevedo.spring_security_jwt.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @GetMapping
    public String welcome() {
        return "Welcome to Spring Security JWT";
    }

    @GetMapping("/users")
    public String welcomeUser() {
        return "Authorized User or Managers";
    }

    @GetMapping("/managers")
    public String welcomeAdmin() {
        return "Authorized Managers";
    }
}

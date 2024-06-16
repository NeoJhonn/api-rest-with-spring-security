package br.com.jhonny_azevedo.spring_security.controllers;

import br.com.jhonny_azevedo.spring_security.models.User;
import br.com.jhonny_azevedo.spring_security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class WelcomeController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public void addUser(@RequestBody User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser == null) {
            System.out.println(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User already exists");
        }
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to My Spring Boot Web API";
    }

    @GetMapping("/users")
    //@PreAuthorize("hasAnyRole('MANAGERS','USERS')")
    public String users() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String roles = authentication.getAuthorities().toString();

        System.out.println("Username: " + username);
        System.out.println("Roles: " + roles);

        return "Authorized User or Manager";
    }

    @GetMapping("/managers")
    //@PreAuthorize("hasRole('MANAGERS')")
    public String managers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String roles = authentication.getAuthorities().toString();

        System.out.println("Username: " + username);
        System.out.println("Roles: " + roles);



        return "Authorized manager";
    }
}

package br.com.jhonny_azevedo.spring_security_jwt.controllers;

import br.com.jhonny_azevedo.spring_security_jwt.dtos.LoginDTO;
import br.com.jhonny_azevedo.spring_security_jwt.dtos.SessionDTO;
import br.com.jhonny_azevedo.spring_security_jwt.models.User;
import br.com.jhonny_azevedo.spring_security_jwt.repositories.UserRepository;
import br.com.jhonny_azevedo.spring_security_jwt.security.JWTCreator;
import br.com.jhonny_azevedo.spring_security_jwt.security.JWTObject;
import br.com.jhonny_azevedo.spring_security_jwt.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class LoginController {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserRepository repository;

    @PostMapping("/login")
    public SessionDTO login(@RequestBody LoginDTO loginDTO) {
        User user = repository.findByUsername(loginDTO.getUsername());

        if (user != null) {
           boolean passwordsMatch = encoder.matches(loginDTO.getPassword(), user.getPassword());
           if  (!passwordsMatch) {
               throw new RuntimeException("Senha invalida para o login: "+ loginDTO.getUsername());
           }
           // Enviar objeto Session para retornar mais informações do usuário
            SessionDTO session = new SessionDTO();
            session.setLogin(user.getUsername());

            // criar um objeto JWTObject
            JWTObject jwtObject = new JWTObject();
            jwtObject.setIssuedAt(new Date(System.currentTimeMillis()));
            jwtObject.setExpiration(new Date(System.currentTimeMillis() + SecurityConfig.EXPIRATION));
            jwtObject.setRoles(user.getRoles());
            // Criar o token e settar em session
            session.setToken(JWTCreator.create(SecurityConfig.PREFIX, SecurityConfig.KEY, jwtObject));

            return session;
        } else {
            throw new RuntimeException("Erro ao tentar fazer o login");
        }
    }
}

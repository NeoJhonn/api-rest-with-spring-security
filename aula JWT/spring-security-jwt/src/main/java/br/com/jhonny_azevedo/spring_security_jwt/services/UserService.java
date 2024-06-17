package br.com.jhonny_azevedo.spring_security_jwt.services;

import br.com.jhonny_azevedo.spring_security_jwt.models.User;
import br.com.jhonny_azevedo.spring_security_jwt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository repository;

    // Para criptografar senhas do usuário
    @Autowired
    private PasswordEncoder encoder;

    public void createUser(User user) {
        User userExists = repository.findByUsername(user.getUsername());

        if (userExists != null) {
            throw new RuntimeException("User already exists");
        }

        String password = user.getPassword();
        // criptografar senha antes de salvar no banco
        user.setPassword(encoder.encode(password));

        repository.save(user);
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public boolean isPresentByUsername(String username) {
        return repository.existsByUsername(username);
    }
}

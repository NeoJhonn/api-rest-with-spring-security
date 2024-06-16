package br.com.jhonny_azevedo.spring_security_jwt.repositories;

import br.com.jhonny_azevedo.spring_security_jwt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    User findByUsername(@Param("username") String username);

    boolean existsByUsername(String username);
}

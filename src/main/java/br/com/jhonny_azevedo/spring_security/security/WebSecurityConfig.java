package br.com.jhonny_azevedo.spring_security.security;


import br.com.jhonny_azevedo.spring_security.services.SecurityDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  // Habilita o suporte a @PreAuthorize
public class WebSecurityConfig {

    @Autowired
    private SecurityDatabaseService securityDatabaseService;

    // Quando você anota um método com @Autowired, o Spring injeta automaticamente as dependências necessárias no
    // momento em que o método é chamado. Isso é conhecido como injeção por método.
    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(securityDatabaseService)
                .passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // desabilita a segurança padrão do Spring Security para usar a nossa
                .authorizeRequests(authorizeRequests -> authorizeRequests
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/login").permitAll()// só será permitido método POST em "/login"
                                .requestMatchers("/users").hasAnyRole("USERS", "MANAGERS")
                                .requestMatchers("/managers").hasRole("MANAGERS")
                                .anyRequest().authenticated()
                ).httpBasic(withDefaults());
                //.formLogin(withDefaults());


        return http.build();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        // Você pode criar quantos Users vc precisar, aqui criei user e admin
//        UserDetails user = User.withUsername("user")
//                .password("{noop}user123") // O "{noop}" é uma estratégia de criptografia
//                .roles("USERS")
//                .build();
//
//        UserDetails admin = User.withUsername("admin")
//                .password("{noop}master123")// O "{noop}" é uma estratégia de criptografia
//                .roles("MANAGERS")
//                .build();
//
//
//        return new InMemoryUserDetailsManager(user, admin);
//    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

}

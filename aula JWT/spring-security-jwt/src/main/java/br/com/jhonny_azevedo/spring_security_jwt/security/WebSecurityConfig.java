package br.com.jhonny_azevedo.spring_security_jwt.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.frameOptions(withDefaults()).disable()) // desabilita o frameOptions para permitir o Swagger
        .cors(cors -> cors.disable()) // desabilita o cors na sua api
        .csrf(csrf -> csrf.disable()) // desabilita a segurança padrão do Spring Security para usar a nossa
                .addFilterAfter(new JWTFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests(authorizeRequests -> {

                            try {
                                authorizeRequests
                                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                                        .requestMatchers("/").permitAll()
                                        .requestMatchers(HttpMethod.POST,"/login").permitAll()// só será permitido método POST em "/login"
                                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                                        .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("USERS", "MANAGERS")
                                        .requestMatchers("/managers").hasRole("MANAGERS")
                                        .anyRequest().authenticated()
                                        .and()
                                        .sessionManagement(sessionManagement ->
                                                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                );//.httpBasic(withDefaults());
        //.formLogin(withDefaults());


        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}

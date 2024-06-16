# Spring Security

- Grupo de filtros de servlets que ajudam você a adicionar autenticação e autorização a sua aplicação web.

- Para habilitar segurança no seu projeto, adicione a depência do Spring Security ao seu projeto:

```
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

- Spring Security já cria uma autenticação básica padrão, quando você inicializa sua aplicação e acessa localhost:8080, você será 
redirecionado para uma página de login onde o Username = user e Password = é gerada no console do Spring a cada vez que você roda a 
aplicação.

## Autenticação Simples

- Para definir um usuário e senha padrão(para que a senha não mude e cada nova seção) você pode definir no arquivo 
application.properties:

```
# Autenticação Simple padrão
spring.security.user.name=neojhon <-------------------
spring.security.user.password=user123 <---------------------
spring.security.user.roles=USERS

```

## Geração de Usuários em memória

- Cria uma classe de configuração, defina os usuários no método userDetailsService():

```
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  // Habilita o suporte a @PreAuthorize
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest().authenticated()
                )
                .formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Você pode criar quantos Users vc precisar, aqui criei user e admin
        UserDetails user = User.withUsername("user")
                .password("{noop}user123") // O "{noop}" é uma estratégia de criptografia
                .roles("USERS")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password("{noop}master123")// O "{noop}" é uma estratégia de criptografia
                .roles("MANAGERS")
                .build();


        return new InMemoryUserDetailsManager(user, admin);
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

}
```

- No seu controller você pode defini as roles que terão acesso as rotas com a notação @PreAuthorize:

```
@RestController
@RequestMapping("/")
public class WelcomeController {

    @GetMapping
    public String welcome() {
        return "Welcome to My Spring Boot Web API";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('MANAGERS','USERS')")
    public String users() {

        return "Authorized User or Manager";
    }

    @GetMapping("/managers")
    @PreAuthorize("hasRole('MANAGERS')")
    public String managers() {

        return "Authorized manager";
    }
}
```
























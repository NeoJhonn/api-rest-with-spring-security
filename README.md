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

## Configure Adapter

- Elimina a necessidade dos controllers dispor as roles de usuários(@PreAuthorize), você pode configurar as roles e as permições para
as rotas dentro da sua classe de configuração WebSecurityConfig através do @Bean SecurityFilterChain(método):

```
Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  // Habilita o suporte a @PreAuthorize
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/").permitAll() // <------------------------
                                .requestMatchers(HttpMethod.POST, "/login").permitAll()// só será permitido método POST em "/login" // <------------------------
                                .requestMatchers("/users").hasAnyRole("USERS", "MANAGERS") // <------------------------
                                .requestMatchers("/managers").hasRole("MANAGERS") // <------------------------
                                .anyRequest().authenticated()
                )
                .formLogin(withDefaults());
        return http.build();
    }
```

## Autenticação com Banco de Dados

- UserDetailService: usada para recuperar dados relacionados ao usuário, possui um método loadUserByUsername() que pode ser substituído
para personalizar o processo de localização do usuário.
  - Crie uma classe SecurityDatabaseService.java que implemente UserDetailService para retornar um usuário para contexto de segurança
conforme o seu banco de dados:

```
@Service
public class SecurityDatabaseService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        userEntity.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        });

        UserDetails user = new org.springframework.security.core.userdetails.User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                authorities
        );

        return user;
    }
}

----------------------------------------------------------------

// UserRepository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username") <---------------------------
    User findByUsername(@Param("username") String username);
}
```

- Adicine o método globalUserDetails() a sua classe WebSecurityConfig e retire o formulário de login(autenticação básica -> httpBasic(withDefaults())):

```
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
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/").permitAll()
                                .requestMatchers(HttpMethod.POST, "/login").permitAll()// só será permitido método POST em "/login"
                                .requestMatchers("/users").hasAnyRole("USERS", "MANAGERS")
                                .requestMatchers("/managers").hasRole("MANAGERS")
                                .anyRequest().authenticated()
                ).httpBasic(withDefaults());<------------------------
                //.formLogin(withDefaults()); 


        return http.build();
    }
```

# JWT - JSON Web Token

- E um padrão da internet para a criação de dados com assinatura opcional e/ou criptografia cujo conteúdo contém o JSON que afirma
algum número de declarações. Os tokens são assinados usando um segredo privado ou uma chave pública/privada.

- Crie um projeto no spring initializr com as seguintes dependências: Spring Web, Spring Security, Spring Data JPA, Postgres.

















































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

- Adicione a dependência do JWT:

```
<!-- JWT - JSON Web Token-->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

- Crie toda a estrutura do seu projeto(models, controllers, services, etc).

- O JWT use filtros para fazer a autenticação via tokens e é o que vamos fazer a seguir.

- Crie um JWTObject no pacote security:

```
public class JWTObject {

    private String subject; // nome do usuário
    private Date issuedAt; // data de criação do token
    private Date expiration; // data de espiração do token
    private List<String> roles; // roles do usuário

    public JWTObject() {
    }

    // "String ... roles" uma outra forma de passar um Array como parâmetro
    public JWTObject(String subject, Date issuedAt, Date expiration, String ... roles) {
        this.subject = subject;
        this.issuedAt = issuedAt;
        this.expiration = expiration;
        this.roles = Arrays.asList(roles);
    }
    
    // Adicione os getters e setters	
}

```

- Crie um SecurityConfig no pacote security, irá conter informações das credenciais para geração do token:

```
@Configuration
// Os atributos da classe serão preenchidos com os valores contido 
// no prefixo "security.config" do arquivo application.properties
@ConfigurationProperties(prefix = "security.config")
public class SecurityConfig {

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
    
    // Adicione somente os setters
}
```

- No application.properties adicione as propriedades para o token:

```
security.config.prefix=Bearer
security.config.key=SECRET_KEY
security.config.expiration=3600000
```

- Agora vamos criar a classe JWTCreator, recurso que faz toda a interação entre o Spring Security com o mecanismo do JWT:

```
public class JWTCreator {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String ROLE_AUTHORITIES = "authorities";

    public static String create(String prefix, String key, JWTObject jwtObject) {
        String  token = Jwts.builder()
                          .setSubject(jwtObject.getSubject())
                          .setIssuedAt(jwtObject.getIssuedAt())
                          .setExpiration(jwtObject.getExpiration())
                          .claim(ROLE_AUTHORITIES, checkRoles(jwtObject.getRoles()))
                          .signWith(SignatureAlgorithm.HS512, key)
                          .compact();

        return prefix + " " + token;
    }

    public static JWTObject create(String token, String prefix, String key)
            throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException {
        token = token.replace(prefix, "").trim();
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        JWTObject jwtObject = new JWTObject(
                    claims.getSubject(),
                    claims.getIssuedAt(),
                    claims.getExpiration(),
                    (List) claims.get(ROLE_AUTHORITIES)
        );

        return jwtObject;
    }

    private static List<String> checkRoles(List<String> roles) {
        return roles.stream().map(s -> "ROLE_".concat(s.replace("ROLE_", ""))).collect(Collectors.toList());
    }
}
```

- Agora vamos criar a classe JWTFilter, recurso de filter que responsável por validar a integridade do token que esta sendo recibido em
todas as requisições:

```
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JWTFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // obter o token da request com AUTHORIZATION
        String token = request.getHeader(JWTCreator.HEADER_AUTHORIZATION);

        // Validar a integridade do token
        try {
            if (token != null && !token.isEmpty()) {
                JWTObject tokenObject = JWTCreator.create(token, SecurityConfig.PREFIX, SecurityConfig.KEY);

                List<SimpleGrantedAuthority> authorities = tokenObject.getRoles().stream().map(role ->
                                                            new SimpleGrantedAuthority(role)).toList();

                // Criar o objeto de autenticação
                UsernamePasswordAuthenticationToken userToken =
                        new UsernamePasswordAuthenticationToken(
                                tokenObject.getSubject(),
                                null,
                                authorities);

                SecurityContextHolder.getContext().setAuthentication(userToken);
            } else {
                SecurityContextHolder.clearContext();
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException |
                 java.security.SignatureException e) {
            e.printStackTrace();
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }

}
```

- Agora vamos criar a classe WebSecurityConfig:

```
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
        http.headers(headers -> headers.frameOptions(withDefaults()).disable()); // desabilita o frameOptions para permitir o Swagger
        http.cors(cors -> cors.disable()); // desabilita o cors na sua api
        http.csrf(csrf -> csrf.disable()) // desabilita a segurança padrão do Spring Security para usar a nossa
                .addFilterAfter(new JWTFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests(authorizeRequests -> {

                            try {
                                authorizeRequests
                                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
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

```

- Agora vamos criar as classe LoginDTO e SessionDTO:

```
// Classe que receberá os dados para a realização do Login na aplicação

public class LoginDTO {

    private String username;
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Adicionar getters e setters
}



---------------------------------------------

// Classe que representa uma sessão do sistema contendo o token gerado

public class SessionDTO {

    private String login;
    private String token;

    public SessionDTO() {
    }

    public SessionDTO(String login, String token) {
        this.login = login;
        this.token = token;
    }

    // Adicionar getters e setters
}
```

- Agora vamos criar o Controller LoginController que irar fazer o login na aplicação:

```
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
```

- Por fim crie um WelcomeController para testar se a autenticação está correta pra acessa os endpoints de acordo com o tipo de usuário:

```
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
```

- Para testa o token coloque o token gerado quando fizer login no Insomnia -> Auth Type -> Bearer.

- Arquivo Insomnia para testar os endpoints disponível na pasta documentation.










































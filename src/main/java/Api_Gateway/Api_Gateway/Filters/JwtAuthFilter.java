    package Api_Gateway.Api_Gateway.Filters;

    import io.jsonwebtoken.JwtException;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.security.Keys;
    import org.springframework.cloud.gateway.filter.GlobalFilter;
    import org.springframework.cloud.gateway.filter.GatewayFilterChain;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Component;
    import org.springframework.web.server.ServerWebExchange;
    import reactor.core.publisher.Mono;

    import javax.crypto.SecretKey;

    @Component
    public class JwtAuthFilter implements GlobalFilter {

        private static final String SECRET = "clave_secreta_123456789000000000";

        private SecretKey getSigningKey() {
            return Keys.hmacShaKeyFor(SECRET.getBytes());
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

            String path = exchange.getRequest().getURI().getPath();

            // 🔓 RUTAS PUBLICAS
            if (path.contains("/auth/login") ||
                    path.contains("/auth/register")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                  Jwts.parser()
                      .verifyWith(getSigningKey())
                      .build()
                      .parseSignedClaims(token);


            } catch (JwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        }
    }

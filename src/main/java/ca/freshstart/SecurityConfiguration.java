package ca.freshstart;

import ca.freshstart.applications.auth.helpers.CspAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .antMatcher("/**")
            .addFilterBefore(corsFilter(), ChannelProcessingFilter.class)
            .addFilterBefore(authFilter(), BasicAuthenticationFilter.class)
            .authorizeRequests()
            .antMatchers("/login", "/restore", "/email", "/version",
                    "/portal-stomp/**",
                    "/export/appointment",
                    "/export/*",
                    "/reports/table/*",
                    "/reports/byKey/*",
                    "/matchingBoard/confirmation/*",
                    "/matchingBoard/confirmation/*/confirm",
                    "/matchingBoard/confirmation/*/decline",
                    "booking/week/*",
                    "/booking/event/*/lastChange",
                    "/booking/event/*/confirm",
                    "/booking/event/*/decline").permitAll()
            .anyRequest().authenticated()
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    @Bean
    public CspAuthenticationFilter authFilter() {
        return new CspAuthenticationFilter();
    }
}

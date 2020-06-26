package pl.konradboniecki.budget.passwordmanagement.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Scope(scopeName = SCOPE_SINGLETON)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/actuator/health",
                        "/actuator/prometheus",
                        "/api/reset-password/form",
                        "/api/reset-password/request",
                        "/api/reset-password/**/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
        // @formatter:on
    }
}

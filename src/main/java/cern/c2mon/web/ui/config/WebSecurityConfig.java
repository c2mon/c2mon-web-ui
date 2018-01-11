/*******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package cern.c2mon.web.ui.config;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import cern.c2mon.client.common.service.SessionService;
import cern.c2mon.web.ui.security.DefaultAccessDecisionManager;
import cern.c2mon.web.ui.security.DefaultAuthenticationProvider;
import cern.c2mon.web.ui.security.RbacAuthenticationProvider;
import cern.c2mon.web.ui.security.RbacDecisionManager;

/**
 * @author Justin Lewis Salmon
 */
@EnableConfigurationProperties(C2monWebUIProperties.class)
@EnableWebSecurity
@Configuration
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private Environment environment;

  @Autowired
  private C2monWebUIProperties properties;

  @Autowired
  private ApplicationContext appContext;

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/static/**").antMatchers("/css/**").antMatchers("/js/**").antMatchers("/img/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // If the RBAC triplet isn't given, disable authentication entirely.
    if (!properties.isSecurityEnabled() || !environment.containsProperty("c2mon.web.rbac.admin")) {
      http.authorizeRequests().anyRequest().permitAll().and().csrf().disable();
      return;
    }

    http.authenticationProvider(authenticationProvider());

    http.authorizeRequests()
          .accessDecisionManager(accessDecisionManager())
          .antMatchers("/configloader/progress").hasRole("ADMIN")
          .antMatchers("/process/**").hasRole("ADMIN")
          .antMatchers("/commandviewer/**").hasRole("ADMIN")
          .anyRequest().anonymous()
        .and()
        .formLogin()
          .loginPage("/login")
          .loginProcessingUrl("/login")
          .failureUrl("/login?error=true")
          .permitAll()
        .and()
        .csrf().disable();
  }

  @Bean
  public AccessDecisionManager accessDecisionManager() {
    if (properties.isSecurityEnabled() && sessionService() != null) {
      Map<String, String> authorisationDetails = new HashMap<>();
      authorisationDetails.put("configloader/progress", environment.getProperty("c2mon.web.rbac.admin"));
      // Allow show charts without login
      authorisationDetails.put("commandviewer", environment.getProperty("c2mon.web.rbac.user"));

      log.info("Using RbacDecisionManager");
      return new RbacDecisionManager(sessionService(), authorisationDetails);
    }

    log.info("Using DefaultAccessDecisionManager");
    return new DefaultAccessDecisionManager();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    if (properties.isSecurityEnabled() && sessionService() != null) {
      log.info("Using RbacAuthenticationProvider");
      return new RbacAuthenticationProvider(sessionService());
    }

    log.info("Using DefaultAuthenticationProvider");
    return new DefaultAuthenticationProvider();
  }

  private SessionService sessionService() {
    try {
      return appContext.getBean(SessionService.class);
    } catch (NoSuchBeanDefinitionException e) {
      log.debug("No SessionService registered");
    }

    return null;
  }
}

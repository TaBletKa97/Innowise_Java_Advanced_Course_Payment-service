package com.innowise.paymentservice.security;

import com.innowise.paymentservice.exceptions.WrongHeaderException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnotherSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String userId = request.getHeader("user_id");
        String role = request.getHeader("role");

        log.debug("Filtering request.\nuser_id = {}, role={}", userId, role);

        try {
            if (userId != null && role != null) {
                Long id = Long.parseLong(userId);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    id,
                                    null,
                                    List.of(new SimpleGrantedAuthority(role)));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authenticationToken);
                }
            }
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
            throw new WrongHeaderException();
        }

        log.debug("User authenticated successfully");

        filterChain.doFilter(request, response);
    }
}
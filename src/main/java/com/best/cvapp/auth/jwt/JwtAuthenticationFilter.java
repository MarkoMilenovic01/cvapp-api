package com.best.cvapp.auth.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentication filter responsible for processing JWT access tokens
 * on every incoming HTTP request.
 *
 * Flow:
 * 1. Reads the Authorization header from the request.
 * 2. If the header is missing or does not start with "Bearer ",
 *    the request continues without authentication.
 * 3. Extracts the JWT token from the header.
 * 4. Tries to extract the user's email from the token.
 * 5. Loads the user from the database using UserDetailsService.
 * 6. Validates that the token belongs to that user, is not expired,
 *    and that the user account is still enabled.
 * 7. If valid, creates an Authentication object and stores it in
 *    the SecurityContext for the current request.
 *
 * Invalid or expired tokens do not directly stop the request here.
 * Instead, the security context is cleared and Spring Security decides
 * later whether the endpoint requires authentication.
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    /**
     * Executes the JWT authentication logic once per request.
     *
     * If a valid Bearer token is present, the user is authenticated for
     * the duration of the request. If the token is missing or invalid,
     * the request continues without an authenticated user.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            authenticateRequest(jwt, request);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Validates the given JWT and, if valid, places the authenticated user
     * into the Spring Security context.
     *
     * the raw JWT access token without the "Bearer " prefix
     *  the current HTTP request, used to attach request details
     */
    private void authenticateRequest(String jwt, HttpServletRequest request) {
        String email = jwtService.extractEmail(jwt);

        if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(jwt, userDetails)) {
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
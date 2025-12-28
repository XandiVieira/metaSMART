package com.relyon.metasmart.config;

import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("password")
                .build();
    }

    @Nested
    @DisplayName("Filter chain tests")
    class FilterChainTests {

        @Test
        @DisplayName("Should continue filter chain when no auth header")
        void shouldContinueWhenNoAuthHeader() throws Exception {
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(jwtService, never()).extractEmail(anyString());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should continue filter chain when auth header does not start with Bearer")
        void shouldContinueWhenNotBearerToken() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(jwtService, never()).extractEmail(anyString());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should authenticate user with valid token")
        void shouldAuthenticateUserWithValidToken() throws Exception {
            String token = "valid-jwt-token";
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractEmail(token)).thenReturn("john@example.com");
            when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(user);
            when(jwtService.isTokenValid(token, user)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
        }

        @Test
        @DisplayName("Should not authenticate when token is invalid")
        void shouldNotAuthenticateWhenTokenInvalid() throws Exception {
            String token = "invalid-jwt-token";
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractEmail(token)).thenReturn("john@example.com");
            when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(user);
            when(jwtService.isTokenValid(token, user)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should not authenticate when email is null")
        void shouldNotAuthenticateWhenEmailNull() throws Exception {
            String token = "jwt-token";
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractEmail(token)).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(userDetailsService, never()).loadUserByUsername(anyString());
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should handle exception during JWT validation")
        void shouldHandleExceptionDuringValidation() throws Exception {
            String token = "jwt-token";
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractEmail(token)).thenThrow(new RuntimeException("JWT parsing error"));

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}

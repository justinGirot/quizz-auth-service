package com.quizz.authservice.service;

import com.quizz.authservice.config.properties.AppProperties;
import com.quizz.authservice.config.properties.JwtProperties;
import com.quizz.authservice.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling JWT authentication cookies.
 * Provides methods to set and clear httpOnly cookies with proper security attributes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;

    /**
     * Sets JWT authentication cookie with secure attributes.
     * Cookie is httpOnly to prevent XSS attacks.
     * Secure flag is enabled in production environments.
     * SameSite attribute provides CSRF protection.
     *
     * @param response HttpServletResponse to add the cookie
     * @param token JWT token to store in the cookie
     */
    public void setAuthenticationCookie(HttpServletResponse response, String token) {
        JwtProperties.CookieProperties cookieProps = jwtProperties.getCookie();
        boolean isProduction = isProductionEnvironment();

        String secureFlag = isProduction ? SecurityConstants.Cookie.SECURE_FLAG : SecurityConstants.Cookie.NO_SECURE_FLAG;

        String cookieValue = String.format(
            SecurityConstants.Cookie.AUTH_COOKIE_FORMAT,
            cookieProps.getName(),
            token,
            secureFlag,
            cookieProps.getPath(),
            cookieProps.getMaxAge(),
            cookieProps.getSameSite()
        );

        response.addHeader(SecurityConstants.Cookie.SET_COOKIE_HEADER, cookieValue);

        log.debug("JWT authentication cookie set (name: {}, secure: {}, maxAge: {}s, sameSite: {})",
                cookieProps.getName(), isProduction, cookieProps.getMaxAge(), cookieProps.getSameSite());
    }

    /**
     * Clears the JWT authentication cookie by setting its Max-Age to 0.
     * Used during logout to invalidate the client-side token.
     *
     * @param response HttpServletResponse to add the cookie clearing header
     */
    public void clearAuthenticationCookie(HttpServletResponse response) {
        JwtProperties.CookieProperties cookieProps = jwtProperties.getCookie();
        boolean isProduction = isProductionEnvironment();

        String secureFlag = isProduction ? SecurityConstants.Cookie.SECURE_FLAG : SecurityConstants.Cookie.NO_SECURE_FLAG;

        String cookieValue = String.format(
            SecurityConstants.Cookie.CLEAR_COOKIE_FORMAT,
            cookieProps.getName(),
            secureFlag,
            cookieProps.getPath(),
            cookieProps.getSameSite()
        );

        response.addHeader(SecurityConstants.Cookie.SET_COOKIE_HEADER, cookieValue);

        log.debug("JWT authentication cookie cleared (name: {})", cookieProps.getName());
    }

    /**
     * Determines if the application is running in a production environment.
     *
     * @return true if production, false otherwise
     */
    private boolean isProductionEnvironment() {
        String environment = appProperties.getEnvironment();
        return !"dev".equalsIgnoreCase(environment) && !"development".equalsIgnoreCase(environment);
    }
}

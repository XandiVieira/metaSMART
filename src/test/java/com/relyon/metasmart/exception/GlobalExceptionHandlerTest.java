package com.relyon.metasmart.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void shouldHandleResourceNotFoundException() {
        var exception = new ResourceNotFoundException("Resource not found");

        var response = exceptionHandler.handleResourceNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException")
    void shouldHandleDuplicateResourceException() {
        var exception = new DuplicateResourceException("Duplicate resource");

        var response = exceptionHandler.handleDuplicateResource(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Duplicate resource");
    }

    @Test
    @DisplayName("Should handle AuthenticationException")
    void shouldHandleAuthenticationException() {
        var exception = new AuthenticationException("Authentication failed");

        var response = exceptionHandler.handleAuthentication(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication failed");
    }

    @Test
    @DisplayName("Should handle BadRequestException")
    void shouldHandleBadRequestException() {
        var exception = new BadRequestException("Bad request");

        var response = exceptionHandler.handleBadRequest(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Bad request");
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void shouldHandleAccessDeniedException() {
        var exception = new AccessDeniedException("Access denied");

        var response = exceptionHandler.handleAccessDenied(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        var response = exceptionHandler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorMessages.VALIDATION_FAILED);
        assertThat(response.getBody().getErrors()).containsEntry("field", "must not be null");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with null message")
    void shouldHandleMethodArgumentNotValidExceptionWithNullMessage() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("object", "field", null, false, null, null, null);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        var response = exceptionHandler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).containsEntry("field", "Invalid value");
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        var exception = new RuntimeException("Unexpected error");

        var response = exceptionHandler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    @DisplayName("Should handle duplicate field errors in validation")
    void shouldHandleDuplicateFieldErrorsInValidation() {
        var bindingResult = mock(BindingResult.class);
        var fieldError1 = new FieldError("object", "field", "first error");
        var fieldError2 = new FieldError("object", "field", "second error");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        var exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        var response = exceptionHandler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        // Should keep the first error for duplicate fields
        assertThat(response.getBody().getErrors()).containsEntry("field", "first error");
    }

    @Test
    @DisplayName("Should handle SubscriptionRequiredException")
    void shouldHandleSubscriptionRequiredException() {
        var exception = new SubscriptionRequiredException("Premium subscription required");

        var response = exceptionHandler.handleSubscriptionRequired(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Premium subscription required");
    }

    @Test
    @DisplayName("Should handle SubscriptionRequiredException with feature and tier")
    void shouldHandleSubscriptionRequiredExceptionWithFeatureAndTier() {
        var exception = new SubscriptionRequiredException("aiInsights", "Premium");

        var response = exceptionHandler.handleSubscriptionRequired(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("aiInsights");
        assertThat(response.getBody().getMessage()).contains("Premium");
    }

    @Test
    @DisplayName("Should handle UsageLimitExceededException")
    void shouldHandleUsageLimitExceededException() {
        var exception = new UsageLimitExceededException("active goals", 3, 3);

        var response = exceptionHandler.handleUsageLimitExceeded(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("active goals");
        assertThat(response.getBody().getMessage()).contains("3/3");
    }

    @Test
    @DisplayName("Should handle UsageLimitExceededException for guardians")
    void shouldHandleUsageLimitExceededExceptionForGuardians() {
        var exception = new UsageLimitExceededException("guardians per goal", 1, 1);

        var response = exceptionHandler.handleUsageLimitExceeded(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("guardians per goal");
        assertThat(response.getBody().getMessage()).contains("Upgrade to Premium");
    }
}

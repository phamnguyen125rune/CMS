package IVS.CMS.services.error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import IVS.CMS.audit.events.AuditLogEvent;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.dto.response.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalException {

    private final ApplicationEventPublisher eventPublisher;


    private void publishErrorAuditLog(HttpServletRequest request, String errorType, Object details, int statusCode) {
        try {
            Long userId = SecurityService.getCurrentUserId().orElse(null);
            String uri = request != null ? request.getRequestURI() : "UNKNOWN";
            String method = request != null ? request.getMethod() : "UNKNOWN";

            String action = errorType + " - " + method + " " + uri;
            if (action.length() > 250) {
                action = action.substring(0, 250);
            }

            Map<String, Object> reqInfo = new HashMap<>();
            reqInfo.put("method", method);
            reqInfo.put("uri", uri);
            if (request != null && request.getQueryString() != null) {
                reqInfo.put("query", request.getQueryString());
            }

            this.eventPublisher.publishEvent(new AuditLogEvent(
                    userId,
                    "API_ERROR",
                    0L,
                    action,
                    reqInfo,
                    details,
                    statusCode
            ));
        } catch (Exception e) {
            log.error("Failed to publish error audit log: {}", e.getMessage());
        }
    }

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<RestResponse<Object>> handleBadRequestException(BadRequestException exception, HttpServletRequest request) {
        publishErrorAuditLog(request, "BAD_REQUEST", exception.getMessage(), HttpStatus.BAD_REQUEST.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Yêu cầu không hợp lệ");
        res.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        List<String> errors = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        Object message = errors.isEmpty()
                ? "Dữ liệu không hợp lệ"
                : (errors.size() > 1 ? errors : errors.get(0));

        publishErrorAuditLog(request, "VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Dữ liệu không hợp lệ");
        res.setMessage(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        publishErrorAuditLog(request, "MALFORMED_JSON", "Dữ liệu JSON gửi lên sai cú pháp hoặc kiểu dữ liệu", HttpStatus.BAD_REQUEST.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Dữ liệu không hợp lệ");
        res.setMessage("Dữ liệu gửi lên không đúng định dạng JSON");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String detail = String.format("Tham số '%s' có giá trị '%s' không đúng kiểu dữ liệu yêu cầu", ex.getName(), ex.getValue());
        publishErrorAuditLog(request, "TYPE_MISMATCH", detail, HttpStatus.BAD_REQUEST.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Tham số không hợp lệ");
        res.setMessage(detail);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler({ ResourceNotFoundException.class, NoResourceFoundException.class })
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(Exception exception, HttpServletRequest request) {
        publishErrorAuditLog(request, "NOT_FOUND", exception.getMessage(), HttpStatus.NOT_FOUND.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError("Không tìm thấy tài nguyên");
        res.setMessage(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RestResponse<Object>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String detail = String.format("Phương thức %s không được hỗ trợ cho endpoint này", ex.getMethod());
        publishErrorAuditLog(request, "METHOD_NOT_ALLOWED", detail, HttpStatus.METHOD_NOT_ALLOWED.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        res.setError("Phương thức không được hỗ trợ");
        res.setMessage(detail);

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(res);
    }

    @ExceptionHandler({ ForbiddenException.class, AccessDeniedException.class })
    public ResponseEntity<RestResponse<Object>> handleForbiddenException(Exception ex, HttpServletRequest request) {
        publishErrorAuditLog(request, "FORBIDDEN", ex.getMessage(), HttpStatus.FORBIDDEN.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError("Bị từ chối truy cập");
        res.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            InternalAuthenticationServiceException.class,
            LockedException.class,
            DisabledException.class
    })
    public ResponseEntity<RestResponse<Object>> handleAuthException(Exception exception, HttpServletRequest request) {
        publishErrorAuditLog(request, "AUTH_FAILED", "Email hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("Xác thực thất bại");
        res.setMessage("Email hoặc mật khẩu không chính xác");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<RestResponse<Object>> handleConflictException(ConflictException ex, HttpServletRequest request) {
        publishErrorAuditLog(request, "CONFLICT", ex.getMessage(), HttpStatus.CONFLICT.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.CONFLICT.value());
        res.setError("Xung đột dữ liệu");
        res.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RestResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        publishErrorAuditLog(request, "DATA_INTEGRITY_VIOLATION", "Vi phạm ràng buộc dữ liệu hoặc trùng lặp khóa", HttpStatus.CONFLICT.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.CONFLICT.value());
        res.setError("Xung đột dữ liệu");
        res.setMessage("Dữ liệu đã tồn tại hoặc vi phạm ràng buộc hệ thống");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex, HttpServletRequest request) {
        log.error("Internal Server Error: ", ex);
        publishErrorAuditLog(request, "INTERNAL_SERVER_ERROR", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError("Lỗi hệ thống");
        res.setMessage("Đã xảy ra lỗi, vui lòng thử lại");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}


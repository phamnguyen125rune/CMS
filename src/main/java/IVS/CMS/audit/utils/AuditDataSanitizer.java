package IVS.CMS.audit.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Tiện ích làm sạch dữ liệu nhạy cảm và chuyển đổi đối tượng sang chuỗi JSON
 */
@Component
public class AuditDataSanitizer {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldpassword", "newpassword", "passwordhash",
            "token", "refreshtoken", "accesstoken", "secret");

    private final ObjectMapper objectMapper;

    /*
     * Khởi tạo cấu hình Json. Contructor của class AuditDataSanitizer.
     * Khởi tạo ObjectMapper riêng biệt cho module Audit:
     * 
     * .registerModule(new JavaTimeModule()) - hỗ trợ chuyển đổi các kiểu dữ liệu
     * thời gian Java 8+ (LocalDateTime, Instant, v.v.) sang JSON và ngược lại.
     * -- java.time.LocalDateTime đang được sử dụng trong các dto.
     * -- Nếu không set config này thì khi save vào DB, các trường trước đó được
     * định nghĩa là LocalDateTime sẽ không lưu được vào DB. Dữ liệu AuditLog sẽ bị
     * ghi NULL.
     */
    public AuditDataSanitizer() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Làm sạch và chuyển đổi đối tượng (DTO/Map) sang chuỗi JSON.
     * Tự động quét đệ quy để băm đối chiếu SHA-256 các trường nhạy cảm.
     * 
     * @param data Dữ liệu đầu vào (Request DTO hoặc Response DTO)
     * @return Chuỗi JSON đã được làm sạch, hoặc null nếu data rỗng
     */
    public String sanitizeAndSerialize(Object data) {
        if (data == null) {
            return null;
        }
        // Với responseData, tồn tại trường hợp responseData là String (ví dụ: khi
        // response là string đơn giản), cần check loại bỏ chuỗi đó trước khi convert
        // sang Map.
        if (data instanceof String str) {
            return str;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawMap = objectMapper.convertValue(data, Map.class);
            Map<String, Object> sanitizedMap = sanitizeMap(rawMap);
            return objectMapper.writeValueAsString(sanitizedMap);
        } catch (Exception e) {
            return "{\"raw\":\"" + data.toString() + "\"}";
        }
    }

    /**
     * Xử lý làm sạch một Map dữ liệu (Object).
     */
    private Map<String, Object> sanitizeMap(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value != null) {
                result.put(key, sanitizeValue(key, value));
            }
        }
        return result;
    }

    /**
     * Xử lý làm sạch một Danh sách dữ liệu (List/Array).
     */
    private List<Object> sanitizeList(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(sanitizeValue(null, item));
            }
        }
        return result;
    }

    /**
     * Phân luồng xử lý cho từng giá trị đơn lẻ:
     * 1. Nếu là trường nhạy cảm -> Mã hóa đối chiếu SHA-256.
     * 2. Nếu là Map lồng nhau -> Đệ quy gọi sanitizeMap.
     * 3. Nếu là List lồng nhau -> Đệ quy gọi sanitizeList.
     * 4. Các giá trị thông thường khác -> Giữ nguyên.
     */
    @SuppressWarnings("unchecked")
    private Object sanitizeValue(String key, Object value) {
        if (key != null && isSensitiveKey(key)) {
            return createHashFingerprint(value.toString());
        }
        if (value instanceof Map) {
            return sanitizeMap((Map<String, Object>) value);
        }
        if (value instanceof List) {
            return sanitizeList((List<?>) value);
        }
        return value;
    }

    /**
     * Kiểm tra xem tên trường có thuộc danh sách nhạy cảm hay không.
     */
    private boolean isSensitiveKey(String key) {
        return SENSITIVE_KEYS.contains(key.toLowerCase());
    }

    /**
     * Tạo mã đối chiếu Fingerprint 8 ký tự đầu bằng SHA-256.
     * TODO: Trực tiếp return "[PROTECTED]" nếu trong tương lai không cần.
     */
    private String createHashFingerprint(String rawText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder("[HASH: ");
            for (int i = 0; i < 4; i++) {
                hexString.append(String.format("%02x", hash[i]));
            }
            hexString.append("]");
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "[PROTECTED]";
        }
    }
}

package IVS.CMS.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Cấu hình kích hoạt xử lý bất đồng bộ (@Async) cho toàn bộ ứng dụng.
 * Hiện đang sử dụng chỉ dành cho Audit log
 * (//TODO: Hiện tại chưa có nhu cầu sử dụng async cho tác vụ nào khác ngoài audit log)
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}

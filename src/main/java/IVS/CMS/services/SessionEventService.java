package IVS.CMS.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import IVS.CMS.domain.User;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.services.error.BadRequestException;

@Service
public class SessionEventService {
    private static final long NO_TIMEOUT = 0L;
    private static final String LOCKED_MESSAGE = "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.";

    private final ConcurrentMap<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public SessionEventService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SseEmitter subscribeCurrentUser() {
        String email = SecurityService.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Bạn chưa đăng nhập"));
        User user = this.userRepository.findByEmail(email);
        if (user == null || user.getId() <= 0) {
            throw new BadRequestException("Phiên đăng nhập không hợp lệ");
        }

        SseEmitter emitter = new SseEmitter(NO_TIMEOUT);
        emittersByUser.computeIfAbsent(user.getId(), ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(user.getId(), emitter));
        emitter.onTimeout(() -> removeEmitter(user.getId(), emitter));
        emitter.onError(ignored -> removeEmitter(user.getId(), emitter));

        sendEvent(user.getId(), emitter, "connected", Map.of(
                "status", "ok",
                "userId", user.getId()));
        return emitter;
    }

    public void notifyAccountLocked(long userId) {
        List<SseEmitter> emitters = emittersByUser.remove(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "message", LOCKED_MESSAGE,
                "userId", userId);
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("account_locked").data(payload));
            } catch (IOException ignored) {
                // Client may already be gone.
            } finally {
                emitter.complete();
            }
        });
    }

    private void sendEvent(long userId, SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException ignored) {
            removeEmitter(userId, emitter);
            emitter.complete();
        }
    }

    private void removeEmitter(long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId);
        }
    }
}

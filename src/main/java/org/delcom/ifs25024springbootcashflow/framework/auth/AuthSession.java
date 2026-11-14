package org.delcom.ifs25024springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Sesi login. User disimpan di VaadinSession dan HTTP session, serta di cookie
 * {@code cashflow_uid} yang ditandatangani (HMAC-SHA256) agar sesi dapat dipulihkan
 * setelah refresh atau restart server tanpa bisa dipalsukan.
 * Flag {@code LOGGED_OUT_KEY} mencegah sesi dipulihkan kembali setelah logout.
 */
public final class AuthSession {

    private static final String USER_ID_KEY = "cashflow.userId";
    private static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;

    private AuthSession() {
    }

    /** Menandai user sebagai sudah login. Dipanggil setelah {@code AuthUseCase.login} sukses. */
    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, null);
        session.setAttribute(USER_ID_KEY, user.getId());

        HttpSession http = httpSession();
        if (http != null) {
            http.removeAttribute(LOGGED_OUT_KEY);
            http.setAttribute(USER_ID_KEY, user.getId());
        }
        writeCookie(user.getId() + "." + sign(String.valueOf(user.getId())), COOKIE_MAX_AGE_SECONDS);
    }

    /** Menghapus sesi login dan cookie. */
    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        session.setAttribute(USER_ID_KEY, null);

        HttpSession http = httpSession();
        if (http != null) {
            http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
            http.removeAttribute(USER_ID_KEY);
        }
        writeCookie("", 0);
    }

    /** Mengambil user yang sedang login; memulihkan sesi dari HTTP session atau cookie bila perlu. */
    public static Optional<User> currentUser(IUserRepository repository) {
        VaadinSession session = VaadinSession.getCurrent();
        HttpSession http = httpSession();

        boolean loggedOut = Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))
                || (http != null && Boolean.TRUE.equals(http.getAttribute(LOGGED_OUT_KEY)));
        if (loggedOut) {
            return Optional.empty();
        }

        Integer userId = (Integer) session.getAttribute(USER_ID_KEY);
        if (userId == null && http != null) {
            userId = (Integer) http.getAttribute(USER_ID_KEY);
        }
        if (userId == null) {
            userId = idFromCookie().orElse(null);
        }
        if (userId == null) {
            return Optional.empty();
        }

        session.setAttribute(USER_ID_KEY, userId);
        return repository.findById(userId);
    }

    // ---------------------------------------------------------------- helper

    private static HttpSession httpSession() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request instanceof VaadinServletRequest servletRequest) {
            return servletRequest.getHttpServletRequest().getSession(true);
        }
        return null;
    }

    private static void writeCookie(String value, int maxAgeSeconds) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response instanceof VaadinServletResponse servletResponse) {
            Cookie cookie = new Cookie(COOKIE_NAME, value);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(maxAgeSeconds);
            servletResponse.getHttpServletResponse().addCookie(cookie);
        }
    }

    private static Optional<Integer> idFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        Cookie[] cookies = request == null ? null : request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    /** Memeriksa format "id.tandatangan" dan mengembalikan id bila tanda tangan valid. */
    private static Optional<Integer> verify(String value) {
        int dot = value.indexOf('.');
        if (dot <= 0) {
            return Optional.empty();
        }
        String idPart = value.substring(0, dot);
        String signature = value.substring(dot + 1);
        boolean valid = MessageDigest.isEqual(
                sign(idPart).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(idPart));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC tidak tersedia", e);
        }
    }

    /** Kunci tanda tangan cookie dari SESSION_SECRET (environment atau .env); ganti di server produksi. */
    private static String secret() {
        String secret = System.getenv("SESSION_SECRET");
        if (secret == null) {
            secret = System.getProperty("SESSION_SECRET");
        }
        return secret == null ? "cashflow-ganti-kunci-ini" : secret;
    }
}

package xiaozhi.modules.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import xiaozhi.common.exception.RenException;

@Service
@RequiredArgsConstructor
public class SsoTicketService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;

    @Value("${chuangke.sso.secret:}")
    private String ssoSecret;

    public SsoUser verify(String ticket) {
        if (StringUtils.isBlank(getSsoSecret()) || StringUtils.isBlank(ticket)) {
            throw new RenException("SSO未启用");
        }

        String[] parts = ticket.split("\\.");
        if (parts.length != 2) {
            throw new RenException("SSO登录凭证无效");
        }

        try {
            String expectedSignature = sign(parts[0]);
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[1].getBytes(StandardCharsets.UTF_8))) {
                throw new RenException("SSO登录凭证签名无效");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[0]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});
            long expiresAt = ((Number) payload.getOrDefault("exp", 0)).longValue();
            if (expiresAt < System.currentTimeMillis() / 1000) {
                throw new RenException("SSO登录凭证已过期");
            }

            String userId = String.valueOf(payload.getOrDefault("uid", ""));
            if (StringUtils.isBlank(userId)) {
                throw new RenException("SSO登录凭证缺少用户");
            }

            String username = "ck_" + userId.replaceAll("[^A-Za-z0-9_-]", "");
            if (username.length() > 50) {
                username = username.substring(0, 50);
            }

            boolean superAdmin = "admin".equals(String.valueOf(payload.getOrDefault("role", "")));
            return new SsoUser(username, superAdmin);
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            throw new RenException("SSO登录凭证解析失败");
        }
    }

    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(getSsoSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private String getSsoSecret() {
        String envSecret = System.getenv("CHUANGKE_SSO_SECRET");
        return StringUtils.defaultIfBlank(ssoSecret, envSecret);
    }

    public record SsoUser(String username, boolean superAdmin) {}
}

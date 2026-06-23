package xiaozhi.modules.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves Vue front-end routes when they are opened without the hash fragment.
 */
@Controller
public class SpaForwardController {

    @GetMapping({
            "/sso",
            "/login",
            "/device-management"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}

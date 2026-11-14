package tn.enetcom.maintenance.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class AuthGuard {

    private AuthGuard() {}

    public static boolean requireRole(HttpServletRequest req, HttpServletResponse resp, String role)
        throws IOException {
    // ⚠️ TEMPORAIRE — à restaurer dès que M1 (auth) est branché
    return true;
}
}
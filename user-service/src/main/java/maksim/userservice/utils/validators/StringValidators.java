package maksim.userservice.utils.validators;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;

@Component
public class StringValidators {
    private static final String[] DANGEROUS_PATTERNS = {"'", "\"", ";", "--", "/*", "*/", "xp_", "exec"};

    public String textScreening(String str) {
        return Encode.forHtml(str);
    }

    public boolean isSafeFromSqlInjection(String input) {
        for (String pattern : DANGEROUS_PATTERNS) {
            if (input.contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    public String getDangerousPatterns() {
        return String.join(" ", DANGEROUS_PATTERNS);
    }

}

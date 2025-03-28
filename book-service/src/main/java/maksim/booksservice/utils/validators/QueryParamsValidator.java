package maksim.booksservice.utils.validators;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QueryParamsValidator {
    private static final String DANGEROUS_CHARS = "[\n\r]";

    public void queryAsMapValidating(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            entry.setValue(
                entry.getValue().replaceAll(DANGEROUS_CHARS, "_")
            );
        }
    }

}

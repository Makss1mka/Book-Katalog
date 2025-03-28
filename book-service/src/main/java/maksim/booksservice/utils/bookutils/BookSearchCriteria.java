package maksim.booksservice.utils.bookutils;

import maksim.booksservice.exceptions.BadRequestException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import maksim.booksservice.utils.enums.DateOperator;
import maksim.booksservice.utils.enums.JoinMode;
import maksim.booksservice.utils.enums.NumberOperator;

@Getter
@Setter
public class BookSearchCriteria {
    private String name;
    private Integer authorId;
    private String authorName;
    private List<String> genres = null;

    private Date issuedDate = null;
    private DateOperator issuedDateOperator = null;

    private Integer rating = null;
    private NumberOperator ratingOperator = null;

    private Date statusMinDate = null;
    private Date statusMaxDate = null;

    private JoinMode joinModeForAuthor = JoinMode.WITHOUT;
    private JoinMode joinModeForStatuses = JoinMode.WITHOUT;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private void includeDate(Map<String, String> params) {
        if (params.containsKey("issuedDate")) {
            try {
                this.issuedDate = formatter.parse(params.get("issuedDate"));
            } catch (ParseException e) {
                throw new BadRequestException("Date format is invalid. Date should be in format 'yyyy-MM-dd'");
            }

            this.issuedDateOperator = (params.containsKey("issueDateOperator"))
                    ? DateOperator.fromValue(params.get("issueDateOperator")) : DateOperator.NEWER;
        }
    }

    public BookSearchCriteria(Map<String, String> params) {
        this.name = params.get("name");
        this.authorId = params.containsKey("authorId") ? Integer.parseInt(params.get("authorId")) : null;
        this.authorName = params.get("authorName");

        if (params.containsKey("genres")) {
            this.genres = Arrays.stream(params.get("genres").split(",")).toList();
        }

        this.includeDate(params);

        if (params.containsKey("rating")) {
            this.rating = Integer.parseInt(params.get("rating"));
            this.ratingOperator = (params.containsKey("ratingOperator"))
                    ? NumberOperator.fromValue(params.get("ratingOperator")) : NumberOperator.GREATER;
        }

        if (params.containsKey("joinModeForAuthor")) {
            this.joinModeForAuthor = JoinMode.fromValue(params.get("joinModeForAuthor"));
        }

        if (params.containsKey("joinModeForStatuses")) {
            this.joinModeForStatuses = JoinMode.fromValue(params.get("joinModeForStatuses"));

            if (this.joinModeForStatuses == JoinMode.WITH) {
                try {
                    this.statusMinDate = formatter.parse(
                        params.getOrDefault("statusMinDate", "1990-01-01")
                    );

                    this.statusMaxDate = formatter.parse(
                        params.getOrDefault("statusMaxDate", "2222-01-01")
                    );
                } catch (ParseException e) {
                    throw new BadRequestException("Date format is invalid. Date should be in format 'yyyy-MM-dd'");
                }
            }
        }
    }
}
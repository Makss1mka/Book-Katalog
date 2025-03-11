package maksim.booksservice.utils.bookutils;

import jakarta.ws.rs.BadRequestException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import maksim.booksservice.utils.enums.BookStatus;
import maksim.booksservice.utils.enums.BookStatusScope;
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
    private Integer statusCount = null;
    private NumberOperator statusOperator = null;
    private BookStatus status = null;
    private BookStatusScope statusScope = null;

    private void includeDate(Map<String, String> params) {
        if (params.containsKey("issuedDate")) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            try {
                this.issuedDate = formatter.parse(params.get("issuedDate"));
            } catch (ParseException e) {
                throw new BadRequestException("Date format is invalid. Date shoud be in format 'yyyy-MM-dd'");
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

        if (params.containsKey("statusCount")) {
            this.statusCount = Integer.parseInt(params.get("statusCount"));

            this.statusOperator = (params.containsKey("statusOperator"))
                    ? NumberOperator.fromValue(params.get("statusOperator")) : NumberOperator.GREATER;

            this.status = (params.containsKey("status"))
                    ? BookStatus.fromValue(params.get("status")) : BookStatus.READ;

            this.statusScope = (params.containsKey("statusScope"))
                    ? BookStatusScope.fromValue(params.get("statusScope")) : BookStatusScope.OVERALL;
        }
    }
}
package maksim.booksservice.utils;

import jakarta.ws.rs.BadRequestException;
import java.util.Map;
import maksim.booksservice.utils.enums.SortDirection;
import maksim.booksservice.utils.enums.SortField;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class Pagination {

    private Pagination() {}

    public static Sort getSort(String sortField, String sortDir) {
        Sort sort = Sort.by(sortField);

        if (sortDir.equals("asc")) {
            sort = sort.ascending();
        } else if (sortDir.equals("desc")) {
            sort = sort.descending();
        } else {
            throw new BadRequestException("Invalid sort direction value");
        }

        return sort;
    }

    public static Pageable getPageable(int pageNum, int itemsAmount, Sort sort) {
        return PageRequest.of(pageNum, itemsAmount, sort);
    }

    public static Pageable getPageable(int pageNum, int itemsAmount, SortField sortField, SortDirection sortDirection) {
        return PageRequest.of(pageNum, itemsAmount, getSort(sortField.getValue(), sortDirection.getValue()));
    }

    public static Pageable getPageable(Map<String, String> params) {
        SortField sortField = (params.containsKey("sortField"))
                ? SortField.fromValue(params.get("sortField")) : SortField.fromValue("rating");

        SortDirection sortDir = (params.containsKey("sortDirection"))
                ? SortDirection.fromValue(params.get("sortDirection")) : SortDirection.fromValue("desc");

        int pageNum = (params.containsKey("pageNum")) ? Integer.parseInt(params.get("pageNum")) : 0;
        int pageSize = (params.containsKey("pageSize")) ? Integer.parseInt(params.get("pageSize")) : 20;

        return PageRequest.of(pageNum, pageSize, getSort(sortField.getValue(), sortDir.getValue()));
    }

}

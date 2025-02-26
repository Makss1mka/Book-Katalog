package maksim.reviewsservice.utils;

import jakarta.ws.rs.BadRequestException;
import maksim.reviewsservice.utils.enums.SortDirection;
import maksim.reviewsservice.utils.enums.SortField;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class Pagination {

    public Sort getSort(String sortField, String sortDir) {
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

    public Pageable getPageable(int pageNum, int itemsAmount, Sort sort) {
        return PageRequest.of(pageNum, itemsAmount, sort);
    }

    public Pageable getPageable(int pageNum, int itemsAmount, SortField sortField, SortDirection sortDirection) {
        return PageRequest.of(pageNum, itemsAmount, getSort(sortField.getValue(), sortDirection.getValue()));
    }

}

package com.ija.student_management_portal.dto;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PaginatedStudentResponse {
    private List<StudentDTO> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private int numberOfElements;
}

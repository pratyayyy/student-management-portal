package com.ija.student_management_portal.dto;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BulkImportResult {
    private int totalRecords;
    private int successfulImports;
    private int failedImports;
    private List<String> errors;
    private String message;
    private boolean success;
    private long importTimestampMillis;
}

package com.ija.student_management_portal.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RegistrationRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String role;
    private String studentId;

    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", studentId='" + studentId + '\'' +
                '}';
    }
}

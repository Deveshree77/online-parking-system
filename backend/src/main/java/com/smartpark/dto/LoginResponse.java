package com.smartpark.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private String token;
    private String type;
    private Long userId;
    private String fullName;
    private String email;
    private String role;

    public static LoginResponse of(String token, Long userId, String fullName, String email, String role) {
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userId)
                .fullName(fullName)
                .email(email)
                .role(role)
                .build();
    }
}

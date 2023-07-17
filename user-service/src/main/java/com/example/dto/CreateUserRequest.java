package com.example.dto;

import com.example.model.User;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phone;
    private String email;

    @Min(18)
    private int age;

    User to() {

        return User.builder()
                .name(this.name)
                .age(this.age)
                .email(this.email)
                .phone(this.phone)
                .build();
    }

}

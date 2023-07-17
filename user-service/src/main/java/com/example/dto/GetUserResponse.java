package com.example.dto;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserResponse {

    private String name;

    private String phone;

    private String email;

    private int age;

    private Date createdOn;

    private Date updatedOn;
}

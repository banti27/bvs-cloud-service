package com.bvs.aadhar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AadharData {
    private String uid;
    private String name;
    private String dob;
    private String gender;
    private String address;
}

package com.bvs.aadhar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AadharResponse {
    private String status;
    private String message;
    private AadharData data;
}

package com.sparta.todayeats.address.dto.reqeust;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddressUpdateRequest {
    private String alias;
    private String address;
    private String detail;
    private String zipCode;
}

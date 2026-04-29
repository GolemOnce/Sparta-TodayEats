package com.sparta.todayeats.address.dto.response;


import com.sparta.todayeats.address.entity.Address;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class AddressPageResponse {
    List<AddressResponse> addresses;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    public static AddressPageResponse from(Page<Address> addressPage) {
        return AddressPageResponse.builder()
                .addresses(addressPage.getContent().stream()
                        .map(AddressResponse::from)
                        .toList())
                .page(addressPage.getNumber())
                .size(addressPage.getSize())
                .totalElements(addressPage.getTotalElements())
                .totalPages(addressPage.getTotalPages())
                .sort(addressPage.getSort().isSorted() ? addressPage.getSort().toString() : "createdAt,DESC")
                .build();
    }
}

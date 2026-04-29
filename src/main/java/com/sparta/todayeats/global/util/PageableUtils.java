package com.sparta.todayeats.global.util;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.CommonErrorCode;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public class PageableUtils {
    private static final Set<Integer> ALLOWED_SIZE = Set.of(10, 30, 50);

    public static void checkSize(Pageable pageable) {
        if (!ALLOWED_SIZE.contains(pageable.getPageSize())) {
            throw new BaseException(CommonErrorCode.INVALID_PAGE_SIZE);
        }
    }
}
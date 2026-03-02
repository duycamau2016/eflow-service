package com.eflow.dto;

import lombok.*;

/**
 * Generic API response wrapper dùng chung cho mọi endpoint.
 *
 * @param <T> kiểu dữ liệu trả về
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer total;

    /** Trả về response thành công kèm dữ liệu */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Trả về response thành công kèm dữ liệu và tổng số bản ghi */
    public static <T> ApiResponse<T> ok(String message, T data, int total) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .total(total)
                .build();
    }

    /** Trả về response lỗi */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}

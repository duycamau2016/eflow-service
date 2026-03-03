package com.eflow.entity;

import lombok.*;

/**
 * Danh mục phòng ban toàn công ty.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    private Long id;
    private String name;
    private int sortOrder;
}

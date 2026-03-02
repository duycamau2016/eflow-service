package com.eflow.dto;

import lombok.*;

import java.util.List;

/**
 * DTO cho sơ đồ tổ chức dạng cây - ánh xạ tới interface OrgNode trong Angular.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgNodeDTO {

    private EmployeeDTO employee;

    private List<OrgNodeDTO> children;
}

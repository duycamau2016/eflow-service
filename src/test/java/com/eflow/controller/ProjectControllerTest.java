package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Project.ProjectStatus;
import com.eflow.exception.GlobalExceptionHandler;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.EmployeeMapper;
import com.eflow.mapper.InvoiceMilestoneMapper;
import com.eflow.mapper.ProjectInfoMapper;
import com.eflow.mapper.ProjectMapper;
import com.eflow.mapper.ProjectPhaseMapper;
import com.eflow.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests (MockMvc / @WebMvcTest) cho ProjectController.
 */
@WebMvcTest(value = ProjectController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                FlywayAutoConfiguration.class,
                MybatisAutoConfiguration.class
        })
@Import(GlobalExceptionHandler.class)
@DisplayName("ProjectController MockMvc Tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    // Mock tất cả mappers để @WebMvcTest context không cần sqlSessionFactory
    @MockBean private EmployeeMapper employeeMapper;
    @MockBean private ProjectMapper projectMapper;
    @MockBean private ProjectInfoMapper projectInfoMapper;
    @MockBean private InvoiceMilestoneMapper invoiceMilestoneMapper;
    @MockBean private ProjectPhaseMapper projectPhaseMapper;

    private ObjectMapper objectMapper;

    // ─── Dữ liệu dùng chung ────────────────────────────────────────────────

    private ProjectDTO sampleDTO;
    private ProjectDTO sampleResult;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Ghi LocalDate dạng [yyyy,MM,dd] array; dùng ISO string thay thế
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        sampleDTO = ProjectDTO.builder()
                .employeeId("EMP001")
                .name("eFlow Platform")
                .role("Tech Lead")
                .startDate(LocalDate.of(2024, 1, 15))
                .status(ProjectStatus.active)
                .build();

        sampleResult = ProjectDTO.builder()
                .id("PROJ-001")
                .employeeId("EMP001")
                .name("eFlow Platform")
                .role("Tech Lead")
                .startDate(LocalDate.of(2024, 1, 15))
                .status(ProjectStatus.active)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  POST /api/projects — Tạo dự án / Thêm thành viên
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/projects - Tạo dự án mới / Thêm thành viên")
    class CreateEndpointTests {

        @Test
        @DisplayName("201 Created và trả về DTO khi request hợp lệ")
        void create_returns201_whenRequestIsValid() throws Exception {
            given(projectService.create(any(ProjectDTO.class))).willReturn(sampleResult);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Tạo dự án thành công"))
                    .andExpect(jsonPath("$.data.id").value("PROJ-001"))
                    .andExpect(jsonPath("$.data.employeeId").value("EMP001"))
                    .andExpect(jsonPath("$.data.name").value("eFlow Platform"))
                    .andExpect(jsonPath("$.data.role").value("Tech Lead"))
                    .andExpect(jsonPath("$.data.status").value("active"));
        }

        @Test
        @DisplayName("201 Created — Thêm thành viên thứ hai vào cùng dự án")
        void create_returns201_whenAddingSecondMemberToSameProject() throws Exception {
            ProjectDTO dto2 = ProjectDTO.builder()
                    .employeeId("EMP002")
                    .name("eFlow Platform")   // cùng tên dự án
                    .role("UI Designer")
                    .status(ProjectStatus.active)
                    .build();

            ProjectDTO result2 = ProjectDTO.builder()
                    .id("PROJ-002")
                    .employeeId("EMP002")
                    .name("eFlow Platform")
                    .role("UI Designer")
                    .status(ProjectStatus.active)
                    .build();

            given(projectService.create(any(ProjectDTO.class))).willReturn(result2);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto2)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.employeeId").value("EMP002"))
                    .andExpect(jsonPath("$.data.name").value("eFlow Platform"))
                    .andExpect(jsonPath("$.data.role").value("UI Designer"));
        }

        @Test
        @DisplayName("400 Bad Request khi thiếu trường 'name'")
        void create_returns400_whenNameIsMissing() throws Exception {
            ProjectDTO invalidDTO = ProjectDTO.builder()
                    .employeeId("EMP001")
                    // name bị bỏ qua
                    .role("Developer")
                    .status(ProjectStatus.active)
                    .build();

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data.name").exists());

            then(projectService).should(never()).create(any());
        }

        @Test
        @DisplayName("400 Bad Request khi thiếu trường 'role'")
        void create_returns400_whenRoleIsMissing() throws Exception {
            ProjectDTO invalidDTO = ProjectDTO.builder()
                    .employeeId("EMP001")
                    .name("eFlow Platform")
                    // role bị bỏ qua
                    .status(ProjectStatus.active)
                    .build();

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.role").exists());

            then(projectService).should(never()).create(any());
        }

        @Test
        @DisplayName("400 Bad Request khi thiếu trường 'employeeId'")
        void create_returns400_whenEmployeeIdIsMissing() throws Exception {
            ProjectDTO invalidDTO = ProjectDTO.builder()
                    // employeeId bị bỏ qua
                    .name("eFlow Platform")
                    .role("Developer")
                    .status(ProjectStatus.active)
                    .build();

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.employeeId").exists());

            then(projectService).should(never()).create(any());
        }

        @Test
        @DisplayName("400 Bad Request khi thiếu trường 'status'")
        void create_returns400_whenStatusIsMissing() throws Exception {
            // Gửi JSON thiếu trường status
            String json = """
                    {
                        "employeeId": "EMP001",
                        "name": "eFlow Platform",
                        "role": "Developer"
                    }
                    """;

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.status").exists());

            then(projectService).should(never()).create(any());
        }

        @Test
        @DisplayName("404 Not Found khi nhân viên không tồn tại")
        void create_returns404_whenEmployeeNotFound() throws Exception {
            given(projectService.create(any(ProjectDTO.class)))
                    .willThrow(new ResourceNotFoundException("Nhân viên", "id", "EMP001"));

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("EMP001")));
        }

        @Test
        @DisplayName("Truyền đúng DTO xuống service")
        void create_passesCorrectDtoToService() throws Exception {
            given(projectService.create(any(ProjectDTO.class))).willReturn(sampleResult);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isCreated());

            then(projectService).should().create(argThat(dto ->
                    "EMP001".equals(dto.getEmployeeId()) &&
                    "eFlow Platform".equals(dto.getName()) &&
                    "Tech Lead".equals(dto.getRole()) &&
                    ProjectStatus.active == dto.getStatus()
            ));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GET /api/projects/by-name/{name}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/projects/by-name/{name} - Lấy thành viên dự án")
    class GetByNameEndpointTests {

        @Test
        @DisplayName("200 OK với danh sách assignments khi tồn tại")
        void getByName_returns200_withList() throws Exception {
            ProjectDTO dto2 = ProjectDTO.builder()
                    .id("PROJ-002").employeeId("EMP002")
                    .name("eFlow Platform").role("Developer")
                    .status(ProjectStatus.active).build();

            given(projectService.findByProjectName("eFlow Platform"))
                    .willReturn(List.of(sampleResult, dto2));

            mockMvc.perform(get("/api/projects/by-name/eFlow Platform"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.total").value(2))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name").value("eFlow Platform"))
                    .andExpect(jsonPath("$.data[1].name").value("eFlow Platform"));
        }

        @Test
        @DisplayName("200 OK với danh sách rỗng khi không có thành viên")
        void getByName_returns200_withEmptyList() throws Exception {
            given(projectService.findByProjectName("Project X")).willReturn(List.of());

            mockMvc.perform(get("/api/projects/by-name/Project X"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)))
                    .andExpect(jsonPath("$.total").value(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DELETE /api/projects/by-name/{name}
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/projects/by-name/{name} - Xoá dự án")
    class DeleteByNameEndpointTests {

        @Test
        @DisplayName("200 OK khi xoá dự án tồn tại")
        void deleteByName_returns200_whenProjectExists() throws Exception {
            willDoNothing().given(projectService).deleteByProjectName("eFlow Platform");

            mockMvc.perform(delete("/api/projects/by-name/eFlow Platform"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(containsString("eFlow Platform")));

            then(projectService).should().deleteByProjectName("eFlow Platform");
        }

        @Test
        @DisplayName("404 Not Found khi dự án không tồn tại")
        void deleteByName_returns404_whenProjectNotFound() throws Exception {
            willThrow(new ResourceNotFoundException("Dự án", "name", "Project X"))
                    .given(projectService).deleteByProjectName("Project X");

            mockMvc.perform(delete("/api/projects/by-name/Project X"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Project X")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PUT /api/projects/by-name/{name}/rename
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/projects/by-name/{name}/rename - Đổi tên dự án")
    class RenameEndpointTests {

        @Test
        @DisplayName("200 OK khi đổi tên thành công")
        void rename_returns200_whenSuccess() throws Exception {
            willDoNothing().given(projectService).renameProject("eFlow Platform", "eFlow v2.0");

            mockMvc.perform(put("/api/projects/by-name/eFlow Platform/rename")
                            .param("newName", "eFlow v2.0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Đổi tên dự án thành công"));

            then(projectService).should().renameProject("eFlow Platform", "eFlow v2.0");
        }

        @Test
        @DisplayName("400 Bad Request khi thiếu tham số newName")
        void rename_returns400_whenNewNameParamMissing() throws Exception {
            mockMvc.perform(put("/api/projects/by-name/eFlow Platform/rename"))
                    // không truyền param newName
                    .andExpect(status().isBadRequest());

            then(projectService).should(never()).renameProject(any(), any());
        }

        @Test
        @DisplayName("404 Not Found khi tên cũ không tồn tại")
        void rename_returns404_whenOldNameNotFound() throws Exception {
            willThrow(new ResourceNotFoundException("Dự án", "name", "Project X"))
                    .given(projectService).renameProject("Project X", "Project Y");

            mockMvc.perform(put("/api/projects/by-name/Project X/rename")
                            .param("newName", "Project Y"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Project X")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PUT /api/projects/{id} — Cập nhật assignment thành viên
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/projects/{id} - Cập nhật assignment thành viên")
    class UpdateMemberEndpointTests {

        @Test
        @DisplayName("200 OK khi cập nhật thành công")
        void update_returns200_whenSuccess() throws Exception {
            ProjectDTO updateDTO = ProjectDTO.builder()
                    .id("PROJ-001")
                    .employeeId("EMP001")
                    .name("eFlow Platform")
                    .role("Senior Tech Lead")
                    .status(ProjectStatus.completed)
                    .endDate(LocalDate.of(2025, 12, 31))
                    .build();

            ProjectDTO updatedResult = ProjectDTO.builder()
                    .id("PROJ-001")
                    .employeeId("EMP001")
                    .name("eFlow Platform")
                    .role("Senior Tech Lead")
                    .status(ProjectStatus.completed)
                    .endDate(LocalDate.of(2025, 12, 31))
                    .build();

            given(projectService.update(eq("PROJ-001"), any(ProjectDTO.class)))
                    .willReturn(updatedResult);

            mockMvc.perform(put("/api/projects/PROJ-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Cập nhật dự án thành công"))
                    .andExpect(jsonPath("$.data.role").value("Senior Tech Lead"))
                    .andExpect(jsonPath("$.data.status").value("completed"));
        }

        @Test
        @DisplayName("404 Not Found khi assignment không tồn tại")
        void update_returns404_whenAssignmentNotFound() throws Exception {
            given(projectService.update(eq("UNKNOWN"), any(ProjectDTO.class)))
                    .willThrow(new ResourceNotFoundException("Dự án", "id", "UNKNOWN"));

            mockMvc.perform(put("/api/projects/UNKNOWN")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("UNKNOWN")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DELETE /api/projects/{id} — Xoá assignment thành viên
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/projects/{id} - Xoá assignment thành viên")
    class DeleteMemberEndpointTests {

        @Test
        @DisplayName("200 OK khi xoá assignment tồn tại")
        void delete_returns200_whenExists() throws Exception {
            willDoNothing().given(projectService).delete("PROJ-001");

            mockMvc.perform(delete("/api/projects/PROJ-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Xoá dự án thành công"));

            then(projectService).should().delete("PROJ-001");
        }

        @Test
        @DisplayName("404 Not Found khi assignment không tồn tại")
        void delete_returns404_whenNotFound() throws Exception {
            willThrow(new ResourceNotFoundException("Dự án", "id", "GHOST"))
                    .given(projectService).delete("GHOST");

            mockMvc.perform(delete("/api/projects/GHOST"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("GHOST")));
        }
    }
}

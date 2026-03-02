package com.eflow.service;

import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Employee;
import com.eflow.entity.Project;
import com.eflow.entity.Project.ProjectStatus;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.repository.EmployeeRepository;
import com.eflow.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests cho ProjectService.
 *
 * Bao gồm các ca kiểm thử:
 *  - Tạo dự án mới (create)          → thêm thành viên đầu tiên vào dự án
 *  - Tìm kiếm theo tên dự án         → findByProjectName
 *  - Xoá dự án theo tên              → deleteByProjectName
 *  - Đổi tên dự án                   → renameProject
 *  - Cập nhật assignment thành viên  → update
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ProjectService projectService;

    // ─── Dữ liệu dùng chung ────────────────────────────────────────────────

    private Employee sampleEmployee;
    private Project  sampleProject;
    private ProjectDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
                .id("EMP001")
                .name("Nguyễn Văn A")
                .position("Developer")
                .department("Engineering")
                .email("nva@company.com")
                .phone("0901234567")
                .build();

        sampleProject = Project.builder()
                .id("PROJ-001")
                .employee(sampleEmployee)
                .name("eFlow Platform")
                .role("Tech Lead")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(null)
                .status(ProjectStatus.active)
                .build();

        sampleDTO = ProjectDTO.builder()
                .id("PROJ-001")
                .employeeId("EMP001")
                .name("eFlow Platform")
                .role("Tech Lead")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(null)
                .status(ProjectStatus.active)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TẠO DỰ ÁN (thêm thành viên vào dự án)
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("create() - Tạo dự án / Thêm thành viên")
    class CreateTests {

        @Test
        @DisplayName("Trả về DTO khi tạo thành công với ID được cung cấp")
        void create_returnsDTO_whenValidDTOWithProvidedId() {
            given(employeeRepository.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectRepository.save(any(Project.class))).willReturn(sampleProject);

            ProjectDTO result = projectService.create(sampleDTO);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("PROJ-001");
            assertThat(result.getEmployeeId()).isEqualTo("EMP001");
            assertThat(result.getName()).isEqualTo("eFlow Platform");
            assertThat(result.getRole()).isEqualTo("Tech Lead");
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.active);

            then(employeeRepository).should().findById("EMP001");
            then(projectRepository).should().save(any(Project.class));
        }

        @Test
        @DisplayName("Tự sinh UUID khi ID trong DTO để trống")
        void create_generatesUUID_whenIdIsBlank() {
            ProjectDTO dtoNoId = ProjectDTO.builder()
                    .id("")                   // blank → UUID sẽ được sinh
                    .employeeId("EMP001")
                    .name("eFlow Platform")
                    .role("Developer")
                    .status(ProjectStatus.active)
                    .build();

            Project savedProject = Project.builder()
                    .id("auto-uuid")
                    .employee(sampleEmployee)
                    .name("eFlow Platform")
                    .role("Developer")
                    .status(ProjectStatus.active)
                    .build();

            given(employeeRepository.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectRepository.save(any(Project.class))).willReturn(savedProject);

            ProjectDTO result = projectService.create(dtoNoId);

            // ID phải được gán (không null / blank)
            ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
            then(projectRepository).should().save(captor.capture());
            assertThat(captor.getValue().getId()).isNotBlank();
        }

        @Test
        @DisplayName("Tự sinh UUID khi ID trong DTO là null")
        void create_generatesUUID_whenIdIsNull() {
            ProjectDTO dtoNullId = ProjectDTO.builder()
                    .id(null)
                    .employeeId("EMP001")
                    .name("eFlow Platform")
                    .role("QA Engineer")
                    .status(ProjectStatus.pending)
                    .build();

            given(employeeRepository.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectRepository.save(any(Project.class))).willAnswer(inv -> inv.getArgument(0));

            projectService.create(dtoNullId);

            ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
            then(projectRepository).should().save(captor.capture());
            assertThat(captor.getValue().getId()).isNotBlank();
        }

        @Test
        @DisplayName("Ném ResourceNotFoundException khi nhân viên không tồn tại")
        void create_throwsResourceNotFoundException_whenEmployeeNotFound() {
            given(employeeRepository.findById("UNKNOWN")).willReturn(Optional.empty());

            ProjectDTO dtoUnknownEmp = ProjectDTO.builder()
                    .employeeId("UNKNOWN")
                    .name("Project X")
                    .role("Developer")
                    .status(ProjectStatus.active)
                    .build();

            assertThatThrownBy(() -> projectService.create(dtoUnknownEmp))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");

            then(projectRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Lưu đúng tên dự án và vai trò vào entity")
        void create_savesCorrectProjectNameAndRole() {
            ProjectDTO dto = ProjectDTO.builder()
                    .employeeId("EMP001")
                    .name("Dự án Alpha")
                    .role("Backend Developer")
                    .startDate(LocalDate.of(2025, 6, 1))
                    .status(ProjectStatus.pending)
                    .build();

            given(employeeRepository.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectRepository.save(any(Project.class))).willAnswer(inv -> inv.getArgument(0));

            projectService.create(dto);

            ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
            then(projectRepository).should().save(captor.capture());

            Project saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("Dự án Alpha");
            assertThat(saved.getRole()).isEqualTo("Backend Developer");
            assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2025, 6, 1));
            assertThat(saved.getStatus()).isEqualTo(ProjectStatus.pending);
            assertThat(saved.getEmployee().getId()).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("Thêm nhiều thành viên vào cùng một dự án")
        void create_allowsMultipleMembersWithSameProjectName() {
            Employee emp2 = Employee.builder()
                    .id("EMP002").name("Trần Thị B")
                    .position("Designer").department("Design")
                    .email("ttb@company.com").build();

            ProjectDTO dto2 = ProjectDTO.builder()
                    .employeeId("EMP002")
                    .name("eFlow Platform")   // cùng tên dự án
                    .role("UI Designer")
                    .status(ProjectStatus.active)
                    .build();

            Project savedForEmp2 = Project.builder()
                    .id("PROJ-002")
                    .employee(emp2)
                    .name("eFlow Platform")
                    .role("UI Designer")
                    .status(ProjectStatus.active)
                    .build();

            given(employeeRepository.findById("EMP002")).willReturn(Optional.of(emp2));
            given(projectRepository.save(any(Project.class))).willReturn(savedForEmp2);

            ProjectDTO result = projectService.create(dto2);

            assertThat(result.getName()).isEqualTo("eFlow Platform");
            assertThat(result.getEmployeeId()).isEqualTo("EMP002");
            assertThat(result.getRole()).isEqualTo("UI Designer");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TÌM THEO TÊN DỰ ÁN
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByProjectName() - Lấy thành viên theo tên dự án")
    class FindByProjectNameTests {

        @Test
        @DisplayName("Trả về danh sách assignments khi tồn tại")
        void findByProjectName_returnsList_whenExists() {
            Employee emp2 = Employee.builder()
                    .id("EMP002").name("Trần Thị B")
                    .position("Designer").department("Design")
                    .email("ttb@company.com").build();

            Project p2 = Project.builder()
                    .id("PROJ-002").employee(emp2)
                    .name("eFlow Platform").role("UI Designer")
                    .status(ProjectStatus.active).build();

            given(projectRepository.findByNameIgnoreCase("eFlow Platform"))
                    .willReturn(List.of(sampleProject, p2));

            List<ProjectDTO> result = projectService.findByProjectName("eFlow Platform");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ProjectDTO::getName)
                    .containsOnly("eFlow Platform");
            assertThat(result).extracting(ProjectDTO::getEmployeeId)
                    .containsExactlyInAnyOrder("EMP001", "EMP002");
        }

        @Test
        @DisplayName("Trả về danh sách rỗng khi không có assignment nào")
        void findByProjectName_returnsEmptyList_whenNotExists() {
            given(projectRepository.findByNameIgnoreCase("Không tồn tại"))
                    .willReturn(List.of());

            List<ProjectDTO> result = projectService.findByProjectName("Không tồn tại");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Tìm kiếm không phân biệt hoa thường")
        void findByProjectName_isCaseInsensitive() {
            given(projectRepository.findByNameIgnoreCase("EFLOW PLATFORM"))
                    .willReturn(List.of(sampleProject));

            List<ProjectDTO> result = projectService.findByProjectName("EFLOW PLATFORM");

            assertThat(result).hasSize(1);
            then(projectRepository).should().findByNameIgnoreCase("EFLOW PLATFORM");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  XOÁ DỰ ÁN THEO TÊN
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteByProjectName() - Xoá dự án theo tên")
    class DeleteByProjectNameTests {

        @Test
        @DisplayName("Xoá tất cả assignments khi dự án tồn tại")
        void deleteByProjectName_deletesAll_whenProjectExists() {
            given(projectRepository.findByNameIgnoreCase("eFlow Platform"))
                    .willReturn(List.of(sampleProject));
            willDoNothing().given(projectRepository).deleteAll(anyList());

            projectService.deleteByProjectName("eFlow Platform");

            then(projectRepository).should().deleteAll(List.of(sampleProject));
        }

        @Test
        @DisplayName("Ném ResourceNotFoundException khi dự án không tồn tại")
        void deleteByProjectName_throwsResourceNotFoundException_whenNotFound() {
            given(projectRepository.findByNameIgnoreCase("Project X"))
                    .willReturn(List.of());

            assertThatThrownBy(() -> projectService.deleteByProjectName("Project X"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project X");

            then(projectRepository).should(never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("Xoá tất cả assignments của dự án có nhiều thành viên")
        void deleteByProjectName_deletesAllMembers_whenMultipleAssignments() {
            Project p2 = Project.builder()
                    .id("PROJ-002").employee(sampleEmployee)
                    .name("eFlow Platform").role("Developer")
                    .status(ProjectStatus.active).build();

            List<Project> all = List.of(sampleProject, p2);
            given(projectRepository.findByNameIgnoreCase("eFlow Platform")).willReturn(all);
            willDoNothing().given(projectRepository).deleteAll(all);

            projectService.deleteByProjectName("eFlow Platform");

            then(projectRepository).should().deleteAll(all);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ĐỔI TÊN DỰ ÁN
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("renameProject() - Đổi tên dự án")
    class RenameProjectTests {

        @Test
        @DisplayName("Cập nhật tên cho tất cả assignments khi đổi tên thành công")
        void renameProject_updatesAllAssignments_whenExists() {
            Project p2 = Project.builder()
                    .id("PROJ-002").employee(sampleEmployee)
                    .name("eFlow Platform").role("Developer")
                    .status(ProjectStatus.active).build();

            given(projectRepository.findByNameIgnoreCase("eFlow Platform"))
                    .willReturn(List.of(sampleProject, p2));
            given(projectRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

            projectService.renameProject("eFlow Platform", "eFlow v2.0");

            assertThat(sampleProject.getName()).isEqualTo("eFlow v2.0");
            assertThat(p2.getName()).isEqualTo("eFlow v2.0");
            then(projectRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("Tên mới được trim khoảng trắng thừa")
        void renameProject_trimsWhitespace_fromNewName() {
            given(projectRepository.findByNameIgnoreCase("eFlow Platform"))
                    .willReturn(List.of(sampleProject));
            given(projectRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

            projectService.renameProject("eFlow Platform", "  eFlow v2.0  ");

            assertThat(sampleProject.getName()).isEqualTo("eFlow v2.0");
        }

        @Test
        @DisplayName("Ném ResourceNotFoundException khi tên cũ không tồn tại")
        void renameProject_throwsResourceNotFoundException_whenOldNameNotFound() {
            given(projectRepository.findByNameIgnoreCase("Project X"))
                    .willReturn(List.of());

            assertThatThrownBy(() -> projectService.renameProject("Project X", "Project Y"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project X");

            then(projectRepository).should(never()).saveAll(anyList());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CẬP NHẬT THÀNH VIÊN (update assignment)
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("update() - Cập nhật thông tin thành viên trong dự án")
    class UpdateTests {

        @Test
        @DisplayName("Cập nhật thành công các trường role, status, dates")
        void update_updatesFields_whenProjectExists() {
            given(projectRepository.findById("PROJ-001")).willReturn(Optional.of(sampleProject));
            given(projectRepository.save(any(Project.class))).willAnswer(inv -> inv.getArgument(0));

            ProjectDTO updateDTO = ProjectDTO.builder()
                    .id("PROJ-001")
                    .employeeId("EMP001")        // cùng nhân viên
                    .name("eFlow Platform")
                    .role("Senior Tech Lead")    // đổi role
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2025, 12, 31))  // thêm endDate
                    .status(ProjectStatus.completed)      // đổi status
                    .build();

            ProjectDTO result = projectService.update("PROJ-001", updateDTO);

            assertThat(result.getRole()).isEqualTo("Senior Tech Lead");
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.completed);
            assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        }

        @Test
        @DisplayName("Thay đổi nhân viên khi employeeId khác")
        void update_changesEmployee_whenEmployeeIdDiffers() {
            Employee emp2 = Employee.builder()
                    .id("EMP002").name("Trần Thị B")
                    .position("PM").department("Management")
                    .email("ttb@company.com").build();

            given(projectRepository.findById("PROJ-001")).willReturn(Optional.of(sampleProject));
            given(employeeRepository.findById("EMP002")).willReturn(Optional.of(emp2));
            given(projectRepository.save(any(Project.class))).willAnswer(inv -> inv.getArgument(0));

            ProjectDTO updateDTO = ProjectDTO.builder()
                    .id("PROJ-001")
                    .employeeId("EMP002")      // đổi sang nhân viên mới
                    .name("eFlow Platform")
                    .role("Project Manager")
                    .status(ProjectStatus.active)
                    .build();

            ProjectDTO result = projectService.update("PROJ-001", updateDTO);

            assertThat(result.getEmployeeId()).isEqualTo("EMP002");
            then(employeeRepository).should().findById("EMP002");
        }

        @Test
        @DisplayName("Ném ResourceNotFoundException khi assignment không tồn tại")
        void update_throwsResourceNotFoundException_whenProjectNotFound() {
            given(projectRepository.findById("UNKNOWN")).willReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.update("UNKNOWN", sampleDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }

        @Test
        @DisplayName("Ném ResourceNotFoundException khi nhân viên mới không tồn tại")
        void update_throwsResourceNotFoundException_whenNewEmployeeNotFound() {
            given(projectRepository.findById("PROJ-001")).willReturn(Optional.of(sampleProject));
            given(employeeRepository.findById("EMP999")).willReturn(Optional.empty());

            ProjectDTO updateDTO = ProjectDTO.builder()
                    .id("PROJ-001").employeeId("EMP999")
                    .name("eFlow Platform").role("Dev")
                    .status(ProjectStatus.active).build();

            assertThatThrownBy(() -> projectService.update("PROJ-001", updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("EMP999");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TÌM THEO NHÂN VIÊN
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByEmployee() - Lấy dự án theo nhân viên")
    class FindByEmployeeTests {

        @Test
        @DisplayName("Trả về danh sách dự án của nhân viên")
        void findByEmployee_returnsList_whenEmployeeExists() {
            given(employeeRepository.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectRepository.findByEmployeeId("EMP001")).willReturn(List.of(sampleProject));

            List<ProjectDTO> result = projectService.findByEmployee("EMP001");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmployeeId()).isEqualTo("EMP001");
            assertThat(result.get(0).getName()).isEqualTo("eFlow Platform");
        }

        @Test
        @DisplayName("Ném ResourceNotFoundException khi nhân viên không tồn tại")
        void findByEmployee_throwsResourceNotFoundException_whenNotFound() {
            given(employeeRepository.findById("GHOST")).willReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.findByEmployee("GHOST"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("GHOST");

            then(projectRepository).should(never()).findByEmployeeId(any());
        }
    }
}

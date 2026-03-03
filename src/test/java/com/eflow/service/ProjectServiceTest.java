package com.eflow.service;

import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Employee;
import com.eflow.entity.Project;
import com.eflow.entity.Project.ProjectStatus;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.EmployeeMapper;
import com.eflow.mapper.ProjectMapper;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private ProjectService projectService;

    private Employee sampleEmployee;
    private Project  sampleProject;
    private ProjectDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
                .id("EMP001")
                .name("Nguyen Van A")
                .position("Developer")
                .department("Engineering")
                .email("nva@company.com")
                .phone("0901234567")
                .build();

        sampleProject = Project.builder()
                .id("PROJ-001")
                .employeeId("EMP001")
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

    @Nested
    @DisplayName("create() - Tao du an / Them thanh vien")
    class CreateTests {

        @Test
        @DisplayName("Tra ve DTO khi tao thanh cong voi ID duoc cung cap")
        void create_returnsDTO_whenValidDTOWithProvidedId() {
            given(employeeMapper.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectMapper.insert(any(Project.class))).willReturn(1);

            ProjectDTO result = projectService.create(sampleDTO);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("PROJ-001");
            assertThat(result.getEmployeeId()).isEqualTo("EMP001");
            assertThat(result.getName()).isEqualTo("eFlow Platform");
            assertThat(result.getRole()).isEqualTo("Tech Lead");
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.active);

            then(employeeMapper).should().findById("EMP001");
            then(projectMapper).should().insert(any(Project.class));
        }

        @Test
        @DisplayName("Tu sinh UUID khi ID trong DTO de trong")
        void create_generatesUUID_whenIdIsBlank() {
            ProjectDTO dtoNoId = ProjectDTO.builder()
                    .id("")
                    .employeeId("EMP001")
                    .name("eFlow Platform")
                    .role("Developer")
                    .status(ProjectStatus.active)
                    .build();

            given(employeeMapper.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectMapper.insert(any(Project.class))).willReturn(1);

            projectService.create(dtoNoId);

            ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
            then(projectMapper).should().insert(captor.capture());
            assertThat(captor.getValue().getId()).isNotBlank();
        }

        @Test
        @DisplayName("Nem ResourceNotFoundException khi nhan vien khong ton tai")
        void create_throwsResourceNotFoundException_whenEmployeeNotFound() {
            given(employeeMapper.findById("UNKNOWN")).willReturn(Optional.empty());

            ProjectDTO dtoUnknownEmp = ProjectDTO.builder()
                    .employeeId("UNKNOWN")
                    .name("Project X")
                    .role("Developer")
                    .status(ProjectStatus.active)
                    .build();

            assertThatThrownBy(() -> projectService.create(dtoUnknownEmp))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");

            then(projectMapper).should(never()).insert(any());
        }

        @Test
        @DisplayName("Luu dung ten du an va vai tro vao entity")
        void create_savesCorrectProjectNameAndRole() {
            ProjectDTO dto = ProjectDTO.builder()
                    .employeeId("EMP001")
                    .name("Du an Alpha")
                    .role("Backend Developer")
                    .startDate(LocalDate.of(2025, 6, 1))
                    .status(ProjectStatus.pending)
                    .build();

            given(employeeMapper.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectMapper.insert(any(Project.class))).willReturn(1);

            projectService.create(dto);

            ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
            then(projectMapper).should().insert(captor.capture());

            Project saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("Du an Alpha");
            assertThat(saved.getRole()).isEqualTo("Backend Developer");
            assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2025, 6, 1));
            assertThat(saved.getStatus()).isEqualTo(ProjectStatus.pending);
            assertThat(saved.getEmployeeId()).isEqualTo("EMP001");
        }
    }

    @Nested
    @DisplayName("findByProjectName() - Lay thanh vien theo ten du an")
    class FindByProjectNameTests {

        @Test
        @DisplayName("Tra ve danh sach assignments khi ton tai")
        void findByProjectName_returnsList_whenExists() {
            Project p2 = Project.builder()
                    .id("PROJ-002").employeeId("EMP002")
                    .name("eFlow Platform").role("UI Designer")
                    .status(ProjectStatus.active).build();

            given(projectMapper.findByNameIgnoreCase("eFlow Platform"))
                    .willReturn(List.of(sampleProject, p2));

            List<ProjectDTO> result = projectService.findByProjectName("eFlow Platform");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ProjectDTO::getName).containsOnly("eFlow Platform");
            assertThat(result).extracting(ProjectDTO::getEmployeeId)
                    .containsExactlyInAnyOrder("EMP001", "EMP002");
        }

        @Test
        @DisplayName("Tra ve danh sach rong khi khong co assignment nao")
        void findByProjectName_returnsEmptyList_whenNotExists() {
            given(projectMapper.findByNameIgnoreCase("Khong ton tai")).willReturn(List.of());

            List<ProjectDTO> result = projectService.findByProjectName("Khong ton tai");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByProjectName() - Xoa du an theo ten")
    class DeleteByProjectNameTests {

        @Test
        @DisplayName("Xoa tat ca assignments khi du an ton tai")
        void deleteByProjectName_deletesAll_whenProjectExists() {
            given(projectMapper.findByNameIgnoreCase("eFlow Platform"))
                    .willReturn(List.of(sampleProject));
            given(projectMapper.deleteByNameIgnoreCase("eFlow Platform")).willReturn(1);

            projectService.deleteByProjectName("eFlow Platform");

            then(projectMapper).should().deleteByNameIgnoreCase("eFlow Platform");
        }

        @Test
        @DisplayName("Nem ResourceNotFoundException khi du an khong ton tai")
        void deleteByProjectName_throwsResourceNotFoundException_whenNotFound() {
            given(projectMapper.findByNameIgnoreCase("Project X")).willReturn(List.of());

            assertThatThrownBy(() -> projectService.deleteByProjectName("Project X"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project X");

            then(projectMapper).should(never()).deleteByNameIgnoreCase(any());
        }
    }

    @Nested
    @DisplayName("renameProject() - Doi ten du an")
    class RenameProjectTests {

        @Test
        @DisplayName("Cap nhat ten cho tat ca assignments khi doi ten thanh cong")
        void renameProject_updatesAllAssignments_whenExists() {
            given(projectMapper.findByNameIgnoreCase("eFlow Platform"))
                    .willReturn(List.of(sampleProject));
            given(projectMapper.updateNameByNameIgnoreCase(anyString(), anyString())).willReturn(1);

            projectService.renameProject("eFlow Platform", "eFlow v2.0");

            then(projectMapper).should().updateNameByNameIgnoreCase("eFlow Platform", "eFlow v2.0");
        }

        @Test
        @DisplayName("Nem ResourceNotFoundException khi ten cu khong ton tai")
        void renameProject_throwsResourceNotFoundException_whenOldNameNotFound() {
            given(projectMapper.findByNameIgnoreCase("Project X")).willReturn(List.of());

            assertThatThrownBy(() -> projectService.renameProject("Project X", "Project Y"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project X");

            then(projectMapper).should(never()).updateNameByNameIgnoreCase(any(), any());
        }
    }

    @Nested
    @DisplayName("update() - Cap nhat thong tin thanh vien trong du an")
    class UpdateTests {

        @Test
        @DisplayName("Cap nhat thanh cong cac truong role, status, dates")
        void update_updatesFields_whenProjectExists() {
            given(projectMapper.findById("PROJ-001")).willReturn(Optional.of(sampleProject));
            given(projectMapper.update(any(Project.class))).willReturn(1);

            ProjectDTO updateDTO = ProjectDTO.builder()
                    .id("PROJ-001")
                    .employeeId("EMP001")
                    .name("eFlow Platform")
                    .role("Senior Tech Lead")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2025, 12, 31))
                    .status(ProjectStatus.completed)
                    .build();

            ProjectDTO result = projectService.update("PROJ-001", updateDTO);

            assertThat(result.getRole()).isEqualTo("Senior Tech Lead");
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.completed);
            assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        }

        @Test
        @DisplayName("Nem ResourceNotFoundException khi assignment khong ton tai")
        void update_throwsResourceNotFoundException_whenProjectNotFound() {
            given(projectMapper.findById("UNKNOWN")).willReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.update("UNKNOWN", sampleDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }

        @Test
        @DisplayName("Doi nhan vien khi employeeId khac")
        void update_changesEmployee_whenEmployeeIdDiffers() {
            given(projectMapper.findById("PROJ-001")).willReturn(Optional.of(sampleProject));
            given(employeeMapper.findById("EMP002")).willReturn(Optional.of(
                    Employee.builder().id("EMP002").name("Tran Thi B")
                            .position("PM").department("Management")
                            .email("ttb@company.com").build()));
            given(projectMapper.update(any(Project.class))).willReturn(1);

            ProjectDTO updateDTO = ProjectDTO.builder()
                    .id("PROJ-001").employeeId("EMP002")
                    .name("eFlow Platform").role("Project Manager")
                    .status(ProjectStatus.active).build();

            ProjectDTO result = projectService.update("PROJ-001", updateDTO);

            assertThat(result.getEmployeeId()).isEqualTo("EMP002");
        }
    }

    @Nested
    @DisplayName("findByEmployee() - Lay du an theo nhan vien")
    class FindByEmployeeTests {

        @Test
        @DisplayName("Tra ve danh sach du an cua nhan vien")
        void findByEmployee_returnsList_whenEmployeeExists() {
            given(employeeMapper.findById("EMP001")).willReturn(Optional.of(sampleEmployee));
            given(projectMapper.findByEmployeeId("EMP001")).willReturn(List.of(sampleProject));

            List<ProjectDTO> result = projectService.findByEmployee("EMP001");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmployeeId()).isEqualTo("EMP001");
            assertThat(result.get(0).getName()).isEqualTo("eFlow Platform");
        }

        @Test
        @DisplayName("Nem ResourceNotFoundException khi nhan vien khong ton tai")
        void findByEmployee_throwsResourceNotFoundException_whenNotFound() {
            given(employeeMapper.findById("GHOST")).willReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.findByEmployee("GHOST"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("GHOST");

            then(projectMapper).should(never()).findByEmployeeId(any());
        }
    }
}
# eFlow Service — Backend (Spring Boot)

REST API phục vụ ứng dụng **eFlow SW2** — quản lý nhân sự, dự án và phòng ban.  
Sử dụng **Spring Boot 3.2 / Java 21**, MyBatis (XML mapper), database **H2 in-memory** (dev) hoặc **PostgreSQL** (prod), migration bằng **Flyway**.

---

## 🚀 Cài đặt & Chạy

### Yêu cầu
| Công cụ | Phiên bản |
|---------|-----------|
| Java    | 21 trở lên |
| Maven   | 3.8 trở lên |

### Chạy development

```bash
cd eFlowService
mvn spring-boot:run
```

Service khởi động tại: **http://localhost:8099**  
H2 Console: **http://localhost:8099/h2-console**  
Swagger UI: **http://localhost:8099/swagger-ui.html**

### Build JAR

```bash
mvn clean package -DskipTests
java -jar target/eflow-service-1.0.0.jar
```

---

## 📡 REST API

Base URL: `http://localhost:8099/api`

Tất cả response đều theo chuẩn wrapper:

```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "total": 10
}
```

---

### 👥 Employees — `/api/employees`

| Method | Endpoint | Mô tả |
|--------|----------|---------|
| `GET` | `/api/employees` | Lấy tất cả nhân viên (kèm dự án) |
| `GET` | `/api/employees/{id}` | Lấy nhân viên theo ID |
| `GET` | `/api/employees/org-tree` | Sơ đồ tổ chức dạng cây |
| `GET` | `/api/employees/search?keyword=` | Tìm kiếm theo tên/phòng ban/chức vụ |
| `GET` | `/api/employees/department/{dept}` | Lọc theo phòng ban |
| `GET` | `/api/employees/{id}/subordinates` | Nhân viên cấp dưới |
| `POST` | `/api/employees` | Tạo nhân viên mới → `201 Created` |
| `POST` | `/api/employees/bulk` | Import hàng loạt (xóa cũ + insert mới) |
| `PUT` | `/api/employees/{id}` | Cập nhật nhân viên |
| `DELETE` | `/api/employees/{id}` | Xoá nhân viên |

---

### 📁 Projects — `/api/projects`

| Method | Endpoint | Mô tả |
|--------|----------|---------|
| `GET` | `/api/projects` | Lấy tất cả assignment |
| `GET` | `/api/projects/employee/{employeeId}` | Dự án của một nhân viên |
| `GET` | `/api/projects/status/{status}` | Lọc theo trạng thái |
| `GET` | `/api/projects/by-name/{name}` | Tất cả thành viên của một dự án |
| `POST` | `/api/projects` | Thêm assignment → `201 Created` |
| `PUT` | `/api/projects/{id}` | Cập nhật assignment |
| `PUT` | `/api/projects/by-name/{name}/rename?newName=` | Đổi tên dự án |
| `PATCH` | `/api/projects/by-name/{name}/status?status=` | Cập nhật trạng thái dự án |
| `DELETE` | `/api/projects/{id}` | Xoá một assignment |
| `DELETE` | `/api/projects/by-name/{name}` | Xoá toàn bộ dự án |

> **Trạng thái hợp lệ:** `active` \| `completed` \| `pending`

---

### 🏦 Project Finance Info — `/api/project-info`

| Method | Endpoint | Mô tả |
|--------|----------|---------|
| `GET` | `/api/project-info` | Tất cả thông tin tài chính dự án |
| `GET` | `/api/project-info/{projectName}` | Theo tên dự án |
| `POST` | `/api/project-info` | Tạo mới → `201 Created` |
| `PUT` | `/api/project-info/{projectName}` | Cập nhật |
| `DELETE` | `/api/project-info/{projectName}` | Xoá |

---

### 🧾 Invoice Milestones — `/api/invoice-milestones`

| Method | Endpoint | Mô tả |
|--------|----------|---------|
| `GET` | `/api/invoice-milestones/{projectName}` | Danh sách mốc xuất hóa đơn |
| `POST` | `/api/invoice-milestones` | Tạo mốc mới → `201 Created` |
| `PUT` | `/api/invoice-milestones/{id}` | Cập nhật mốc |
| `DELETE` | `/api/invoice-milestones/{id}` | Xoá mốc |

> **Trạng thái:** `pending` \| `invoiced` \| `paid`

---

### 📅 Project Phases — `/api/project-phases`

| Method | Endpoint | Mô tả |
|--------|----------|---------|
| `GET` | `/api/project-phases/{projectName}` | Danh sách giai đoạn |
| `POST` | `/api/project-phases` | Tạo giai đoạn → `201 Created` |
| `PUT` | `/api/project-phases/{id}` | Cập nhật |
| `DELETE` | `/api/project-phases/{id}` | Xoá |

> **Trạng thái:** `on_track` \| `at_risk` \| `delayed` \| `completed`

---

### 🏢 Departments — `/api/departments`

| Method | Endpoint | Mô tả |
|--------|----------|---------|
| `GET` | `/api/departments` | Danh sách phòng ban (sorted) |
| `POST` | `/api/departments` | Thêm phòng ban → `201 Created` |
| `PUT` | `/api/departments/{id}` | Đổi tên phòng ban |
| `DELETE` | `/api/departments/{id}` | Xoá phòng ban |
| `POST` | `/api/departments/seed` | Seed phòng ban từ danh sách tên (chỉ thêm thiếu) |

---

### 🔐 Auth — `/api/auth`

| Method | Endpoint | Mô tả |
|--------|----------|---------|
| `POST` | `/api/auth/login` | Đăng nhập, trả về role (admin/guest) |

---

## 🏗️ Kiến trúc

```
src/main/java/com/eflow/
├── EFlowServiceApplication.java       # Entry point + @OpenAPIDefinition
├── config/
│   └── CorsConfig.java                # CORS cho Angular dev/prod
├── controller/
│   ├── AuthController.java
│   ├── DepartmentController.java
│   ├── EmployeeController.java
│   ├── InvoiceMilestoneController.java
│   ├── ProjectController.java
│   ├── ProjectInfoController.java
│   └── ProjectPhaseController.java
├── service/
│   ├── DepartmentService.java
│   ├── EmployeeService.java
│   ├── InvoiceMilestoneService.java
│   ├── ProjectInfoService.java
│   ├── ProjectPhaseService.java
│   └── ProjectService.java
├── mapper/                            # MyBatis @Mapper interfaces
│   ├── DepartmentMapper.java
│   ├── EmployeeMapper.java
│   ├── InvoiceMilestoneMapper.java
│   ├── ProjectInfoMapper.java
│   ├── ProjectMapper.java
│   └── ProjectPhaseMapper.java
├── entity/
│   ├── Department.java
│   ├── Employee.java
│   ├── InvoiceMilestone.java
│   ├── Project.java
│   ├── ProjectInfo.java
│   └── ProjectPhase.java
├── dto/
│   ├── ApiResponse.java               # Generic response wrapper
│   ├── DepartmentDTO.java
│   ├── EmployeeDTO.java
│   ├── InvoiceMilestoneDTO.java
│   ├── OrgNodeDTO.java
│   ├── ProjectDTO.java
│   ├── ProjectInfoDTO.java
│   └── ProjectPhaseDTO.java
└── exception/
    ├── ResourceNotFoundException.java  # → HTTP 404
    ├── DuplicateResourceException.java # → HTTP 409
    └── GlobalExceptionHandler.java     # @RestControllerAdvice

src/main/resources/
├── application.properties             # Dev (H2)
├── application-prod.properties        # Prod (PostgreSQL)
├── db/migration/
│   ├── V1__init_schema.sql            # employees, projects
│   ├── V2__add_project_finance_tables.sql  # project_info, invoice_milestone, project_phase
│   └── V3__add_departments_table.sql  # departments
└── mapper/                            # MyBatis XML
    ├── DepartmentMapper.xml
    ├── EmployeeMapper.xml
    ├── InvoiceMilestoneMapper.xml
    ├── ProjectInfoMapper.xml
    ├── ProjectMapper.xml
    └── ProjectPhaseMapper.xml
```

---

## 🗄️ Database

### Development — H2 In-Memory (mặc định)

```properties
spring.datasource.url=jdbc:h2:mem:eflowdb;MODE=PostgreSQL
```

> Dữ liệu **reset mỗi lần restart**. Flyway chạy lại tất cả migration từ đầu.

**H2 Console:** http://localhost:8099/h2-console  
- JDBC URL: `jdbc:h2:mem:eflowdb`
- Username: `sa` | Password: _(để trống)_

### Production — PostgreSQL

Cấu hình qua `application-prod.properties` hoặc biến môi trường:

```properties
spring.datasource.url=jdbc:postgresql://host:5432/eflowdb
spring.datasource.username=...
spring.datasource.password=...
```

### Schema (Flyway migrations)

| Version | File | Nội dung |
|---------|------|---------|
| V1 | `V1__init_schema.sql` | `employees`, `projects` |
| V2 | `V2__add_project_finance_tables.sql` | `project_info`, `invoice_milestone`, `project_phase` |
| V3 | `V3__add_departments_table.sql` | `departments` (seed 12 PB mặc định) |

---

## 📖 Swagger UI

Truy cập: **http://localhost:8099/swagger-ui.html**

Documentation sinh tự động từ `springdoc-openapi-starter-webmvc-ui 2.3.0`.

---

## 🛠️ Tech Stack

| Thành phần | Công nghệ |
|-----------|-----------|
| Framework | Spring Boot 3.2.3 |
| Ngôn ngữ | Java 21 |
| ORM | MyBatis 3 (XML mapper) |
| Migration | Flyway 9 |
| Database (dev) | H2 2.2 (PostgreSQL mode) |
| Database (prod) | PostgreSQL 15+ |
| API Docs | springdoc-openapi 2.3.0 (Swagger UI) |
| Validation | Jakarta Bean Validation |
| Build tool | Maven |
| Code gen | Lombok |
| Test | JUnit 5 + Mockito + MockMvc |


---

## 🚀 Cài đặt & Chạy

### Yêu cầu
| Công cụ | Phiên bản |
|---------|----------|
| Java    | 17 trở lên |
| Maven   | 3.8 trở lên |

### Chạy development

```bash
# Di chuyển vào thư mục backend
cd eFlowService

# Chạy với Maven wrapper
mvn spring-boot:run
```

Service khởi động tại: **http://localhost:8099**  
H2 Console: **http://localhost:8099/h2-console**

### Build JAR

```bash
mvn clean package -DskipTests
java -jar target/eflow-service-1.0.0.jar
```

### Chạy tests

```bash
# Tất cả tests
mvn test

# Chỉ chạy một class test cụ thể
mvn test -Dtest="ProjectServiceTest"
mvn test -Dtest="ProjectControllerTest"
```

---

## 📡 REST API

Base URL: `http://localhost:8099/api`

Tất cả response đều theo chuẩn wrapper:

```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "total": 10
}
```

---

### 👥 Employees — `/api/employees`

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/employees` | Lấy tất cả nhân viên (kèm dự án) |
| `GET` | `/api/employees/{id}` | Lấy nhân viên theo ID |
| `GET` | `/api/employees/org-tree` | Sơ đồ tổ chức dạng cây |
| `GET` | `/api/employees/search?keyword=` | Tìm kiếm theo tên/phòng ban/chức vụ |
| `GET` | `/api/employees/department/{dept}` | Lọc theo phòng ban |
| `GET` | `/api/employees/{id}/subordinates` | Lấy danh sách nhân viên cấp dưới |
| `POST` | `/api/employees` | Tạo nhân viên mới → `201 Created` |
| `POST` | `/api/employees/bulk-import` | Import hàng loạt từ Excel |
| `PUT` | `/api/employees/{id}` | Cập nhật nhân viên |
| `DELETE` | `/api/employees/{id}` | Xoá nhân viên |

#### Request body — Tạo/Cập nhật nhân viên
```json
{
  "id": "EMP001",
  "name": "Nguyễn Văn A",
  "position": "Tech Lead",
  "department": "SW2",
  "email": "nva@fis.com",
  "phone": "0901234567",
  "managerId": "EMP000",
  "joinDate": "2022-06-01",
  "level": 2
}
```

---

### 📁 Projects — `/api/projects`

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/projects` | Lấy tất cả assignment |
| `GET` | `/api/projects/{id}` | Lấy assignment theo ID |
| `GET` | `/api/projects/employee/{employeeId}` | Dự án của một nhân viên |
| `GET` | `/api/projects/status/{status}` | Lọc theo trạng thái |
| `GET` | `/api/projects/employee/{employeeId}/status/{status}` | Lọc kết hợp |
| `GET` | `/api/projects/by-name/{name}` | Tất cả thành viên của một dự án |
| `POST` | `/api/projects` | Thêm assignment (nhân viên vào dự án) → `201 Created` |
| `PUT` | `/api/projects/{id}` | Cập nhật assignment |
| `PUT` | `/api/projects/by-name/{name}/rename?newName=` | Đổi tên dự án |
| `DELETE` | `/api/projects/{id}` | Xoá một assignment |
| `DELETE` | `/api/projects/by-name/{name}` | Xoá toàn bộ dự án |
| `DELETE` | `/api/projects/employee/{employeeId}` | Xoá tất cả dự án của nhân viên |

#### Request body — Tạo/Cập nhật assignment
```json
{
  "employeeId": "EMP001",
  "name": "eFlow Platform",
  "role": "Tech Lead",
  "startDate": "2024-01-15",
  "endDate": null,
  "status": "active"
}
```

> **Trạng thái hợp lệ:** `active` | `completed` | `pending`

---

## 🏗️ Kiến trúc

```
src/main/java/com/eflow/
├── EFlowServiceApplication.java     # Entry point
├── config/
│   └── CorsConfig.java              # CORS cho Angular (localhost:4200)
├── controller/
│   ├── EmployeeController.java      # REST endpoints /api/employees
│   └── ProjectController.java       # REST endpoints /api/projects
├── service/
│   ├── EmployeeService.java         # Business logic nhân viên + org-tree
│   └── ProjectService.java          # Business logic dự án + CRUD by-name
├── repository/
│   ├── EmployeeRepository.java      # JPA queries nhân viên
│   └── ProjectRepository.java       # JPA queries dự án
├── entity/
│   ├── Employee.java                # Entity bảng employees
│   └── Project.java                 # Entity bảng projects (+ enum ProjectStatus)
├── dto/
│   ├── EmployeeDTO.java             # DTO nhân viên
│   ├── ProjectDTO.java              # DTO dự án (validation annotations)
│   ├── OrgNodeDTO.java              # DTO node sơ đồ cây
│   └── ApiResponse.java             # Generic response wrapper
└── exception/
    ├── ResourceNotFoundException.java    # → HTTP 404
    ├── DuplicateResourceException.java   # → HTTP 409
    └── GlobalExceptionHandler.java       # @RestControllerAdvice xử lý lỗi
```

---

## 🗄️ Database

### Development (mặc định) — H2 In-Memory

```properties
spring.datasource.url=jdbc:h2:mem:eflowdb
spring.jpa.hibernate.ddl-auto=create-drop
```

> Dữ liệu **reset mỗi lần restart**. Phù hợp để phát triển và test.

**H2 Console:** http://localhost:8099/h2-console  
- JDBC URL: `jdbc:h2:mem:eflowdb`
- Username: `sa` | Password: _(để trống)_

### Production — MySQL

Bỏ comment phần MySQL trong `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/eflowdb?useSSL=false&serverTimezone=UTC
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

### Sơ đồ quan hệ

```
employees (1) ──< projects (N)
   id (PK)           id (PK)
   name              employee_id (FK → employees.id)
   position          name
   department        role
   email             start_date
   phone             end_date
   manager_id        status  (active|completed|pending)
   level
   join_date
   avatar
```

---

## 🧪 Unit Tests

```
src/test/java/com/eflow/
├── service/
│   └── ProjectServiceTest.java       # Mockito unit tests cho service layer
└── controller/
    └── ProjectControllerTest.java    # MockMvc integration tests cho controller
```

### Nội dung test

| Class | Test cases |
|-------|-----------|
| `ProjectServiceTest` | Tạo dự án, tìm theo tên, xoá theo tên, đổi tên, cập nhật assignment, tìm theo nhân viên |
| `ProjectControllerTest` | HTTP 201/200/400/404, validation errors, request body mapping, response JSON shape |

---

## ⚙️ Cấu hình

File: `src/main/resources/application.properties`

| Property | Giá trị mặc định | Mô tả |
|----------|-----------------|-------|
| `server.port` | `8099` | Port HTTP |
| `spring.h2.console.enabled` | `true` | Bật H2 web console |
| `spring.jpa.show-sql` | `true` | Log SQL ra console |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Tạo lại schema mỗi lần start |

---

## 🛠️ Tech Stack

| Thành phần | Công nghệ |
|-----------|-----------|
| Framework | Spring Boot 3.2.3 |
| Ngôn ngữ | Java 17 |
| ORM | Spring Data JPA + Hibernate 6 |
| Database (dev) | H2 In-Memory |
| Database (prod) | MySQL 8 |
| Validation | Jakarta Bean Validation |
| Build tool | Maven |
| Code gen | Lombok |
| Excel import | Apache POI 5.2.5 |
| Test | JUnit 5 + Mockito + AssertJ + MockMvc |

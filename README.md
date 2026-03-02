# eFlow Service — Backend (Spring Boot)

REST API phục vụ ứng dụng **eFlow SW2** — quản lý nhân sự và dự án.  
Sử dụng **Spring Boot 3.2 / Java 17**, database H2 in-memory (dev) hoặc MySQL (prod).

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

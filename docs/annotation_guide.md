# 🏷️ Hướng dẫn về các Annotation trong dự án (Java Annotations Guide)

Tài liệu này giải thích ý nghĩa và cách hoạt động của các Annotation phổ biến được sử dụng trong dự án này, giúp bạn hiểu rõ cách Spring Boot và các thư viện liên quan "phép thuật" hóa mã nguồn của chúng ta.

---

## 1. Lombok Annotations (Giảm bớt mã thừa - Boilerplate)
Dùng để tự động tạo getter, setter, constructor... giúp code sạch hơn.

*   `@Data`: "Tất cả trong một". Tự động tạo `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode` và `@RequiredArgsConstructor`.
*   `@Getter` / `@Setter`: Tự động tạo các phương thức lấy (get) và gán (set) giá trị cho các trường dữ liệu.
*   `@NoArgsConstructor`: Tạo một constructor không có tham số (bắt buộc đối với JPA/Hibernate).
*   `@AllArgsConstructor`: Tạo một constructor chứa đầy đủ tất cả các tham số cho các trường dữ liệu.
*   `@RequiredArgsConstructor`: Tạo constructor cho các trường dữ liệu được đánh dấu là `final` (thường dùng để Dependency Injection).
*   `@Builder`: Triển khai Builder Pattern, giúp khởi tạo đối tượng một cách linh hoạt: `User.builder().name("ABC").build()`.
*   `@Slf4j`: Tự động tạo một đối tượng logger (`log`) để ghi lại nhật ký hoạt động của ứng dụng.
*   `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)`: Tự động đặt mức độ truy cập cho tất cả các trường dữ liệu là `private` và `final` nếu không ghi gì thêm.

---

## 2. Spring Core & Web (Framework chính)
Điều phối các thành phần và xử lý yêu cầu HTTP.

*   `@SpringBootApplication`: Đánh dấu lớp khởi chạy ứng dụng Spring Boot.
*   `@RestController`: Đánh dấu một Class là nơi tiếp nhận các yêu cầu API (HTTP Requests) và trả về dữ liệu (thường là JSON).
*   `@RequestMapping("/path")`: Khai báo đường dẫn cơ sở (URL) cho Controller hoặc Method.
*   `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`: Các kiểu yêu cầu HTTP tương ứng: Lấy dữ liệu, Tạo mới, Cập nhật và Xóa.
*   `@Service`: Đánh dấu một Class chứa các logic nghiệp vụ (Business Logic).
*   `@Repository`: Đánh dấu một Class/Interface xử lý việc lưu trữ dữ liệu vào Database.
*   `@Component`: Đánh dấu một Class chung là một "Bean" được Spring quản lý.
*   `@Configuration`: Đánh dấu Class dùng để cấu hình các thiết lập cho ứng dụng.
*   `@Bean`: Dùng trong Class `@Configuration` để khởi tạo một đối tượng và đưa vào Spring Context.
*   `@Value("${name}")`: Đọc giá trị từ file `application.properties`.

---

## 3. Spring Data (JPA/MySQL & MongoDB)
Annotation xử lý tương tác với cơ sở dữ liệu.

*   `@Entity`: Đánh dấu Class là một bảng (Table) trong MySQL.
*   `@Document`: Đánh dấu Class là một bản ghi (Document) trong MongoDB.
*   `@Id`: Đánh dấu trường dữ liệu là Khóa chính (Primary Key).
*   `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Tự động tăng ID trong MySQL.
*   `@Column`: Cấu hình tên cột hoặc các ràng buộc trong Database (vd: `unique = true`).
*   `@Query("...")`: Viết câu lệnh truy vấn tùy chỉnh (JPQL hoặc Native SQL).
*   `@Transactional`: Đảm bảo một nhóm các thao tác Database được thực hiện hoàn toàn hoặc không gì cả (Atomic).
*   `@Modifying`: Đánh dấu câu lệnh Query là lệnh cập nhật hoặc xóa dữ liệu.
*   `@Version`: Triển khai Optimistic Locking (Khóa lạc quan) để chống tranh chấp dữ liệu khi nhiều người sửa cùng lúc.

---

## 4. Validation (Kiểm tra dữ liệu đầu vào)
Sử dụng trong các DTO để đảm bảo dữ liệu từ người dùng gửi lên là hợp lệ.

*   `@Valid`: Kích hoạt việc kiểm tra các ràng buộc validation.
*   `@NotBlank`: Chuỗi ký tự không được để trống hoặc chỉ có khoảng trắng.
*   `@NotNull`: Trường dữ liệu không được phép là `null`.
*   `@Email`: Kiểm tra định dạng email hợp lệ.
*   `@Size(min=x, max=y)`: Kiểm tra độ dài của chuỗi hoặc mảng.

---

## 5. Security & Others
*   `@CrossOrigin`: Cho phép Frontend từ các domain khác truy cập API.
*   `@PreAuthorize("hasRole('...')")`: Kiểm tra quyền hạn của người dùng trước khi cho phép thực thi Method.
*   `@JsonInclude(JsonInclude.Include.NON_NULL)`: Chỉ chuyển đổi sang JSON các trường dữ liệu có giá trị (bỏ qua null).
*   `@JsonProperty("name")`: Đổi tên trường dữ liệu khi chuyển sang JSON.

---

### 💡 Mẹo nhỏ:
Nếu bạn thấy một Class có rất nhiều Annotation, hãy nhớ rằng chúng giúp **tách biệt code xử lý nghiệp vụ** và **code hạ tầng**. Thay vì phải viết hàng trăm dòng code để kết nối DB hay tạo Getter/Setter, bạn chỉ cần dùng 1 từ khóa (Annotation).

# ⚽  - Hệ Thống Đặt Sân Thể Thao Tích Hợp Trợ Lý ẢO AI

## 📖 Giới thiệu dự án
**Sportify** là nền tảng quản lý và đặt lịch thuê sân thể thao trực tuyến (Bóng đá, Tennis, Cầu lông...) được xây dựng trên nền tảng Java Spring Boot. 

Điểm nổi bật nhất của Sportify là việc tích hợp thành công **Trợ lý ảo AI (SportAI)** thông qua mô hình ngôn ngữ lớn (LLM). Trợ lý ảo không chỉ có khả năng tư vấn tự nhiên mà còn phân tích ngữ nghĩa để tự động render các "Thẻ sân bóng" kèm nút đặt lịch ngay trong luồng hội thoại, giúp khách hàng tìm sân một cách nhanh chóng và thông minh nhất.

Bên cạnh đó, dự án cung cấp quy trình thanh toán khép kín với **Cổng thanh toán điện tử VNPAY**, hỗ trợ đặt cọc 30% để giữ chỗ hoàn toàn tự động.

---

## 🛠 Yêu cầu hệ thống (Prerequisites)
Để chạy được dự án này trên máy tính của bạn, cần cài đặt sẵn:
- **Java Development Kit (JDK):** Phiên bản 11 hoặc 17.
- **IDE:** Eclipse, IntelliJ IDEA, hoặc Visual Studio Code (Cài đặt Spring Boot Extension).
- **Cơ sở dữ liệu:** MySQL (có thể dùng XAMPP / phpMyAdmin).
- **Maven:** Trình quản lý thư viện Java.

---

## 🚀 Hướng dẫn cài đặt và chạy dự án

### Bước 1: Clone dự án và Cài đặt Cơ sở dữ liệu
1. Mở Terminal/Git Bash và chạy lệnh:
   ```bash
   git clone https://github.com/trungduc17/TTCS.git
   ```
2. Mở phpMyAdmin (hoặc MySQL Workbench), tạo một Database mới có tên là `sportify`.
3. Tìm file `Database/sportify.sql` trong thư mục dự án và Import nó vào Database vừa tạo.

### Bước 2: Cấu hình Cơ sở dữ liệu & API Key
Mở file `src/main/resources/application.properties` và điều chỉnh các thông số sau cho khớp với máy của bạn:

```properties
# 1. Cấu hình kết nối MySQL (Đổi username và password nếu máy bạn cài khác)
spring.datasource.url=jdbc:mysql://localhost:3306/sportify?allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=

# 2. Cấu hình gửi Email (Mail Token)
# Bạn cần nhập Email của bạn và Mật khẩu ứng dụng (App Password) của Gmail
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# 3. Cấu hình OpenRouter AI (Dành cho Chatbot)
# Bạn truy cập OpenRouter.ai để tạo API Key mới và dán vào đây
gemini.api.key=sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxx
```
> **Lưu ý về Mail Token:** Mật khẩu ở `spring.mail.password` KHÔNG PHẢI là mật khẩu đăng nhập Gmail bình thường. Bạn phải vào Tài khoản Google -> Bảo mật -> Xác minh 2 bước -> **Mật khẩu ứng dụng (App Passwords)** để tạo một chuỗi 16 ký tự dán vào đây.

### Bước 3: Cấu hình cổng thanh toán VNPAY
Để chức năng thanh toán trực tuyến hoạt động, hệ thống sử dụng môi trường thử nghiệm (Sandbox) của VNPAY. Mở file `src/main/java/duan/sportify/VNPayConfig.java` và cấu hình:

```java
// Mã định danh (Terminal ID) do VNPAY cấp
public static String vnp_TmnCode = "HQAUQLZ1"; 

// Chuỗi khóa bảo mật (Hash Secret) do VNPAY cấp
public static String vnp_HashSecret = "TU1XQKHYTOSJHEGGTCW3F0G5XVG3HR3M"; 

// Đường dẫn trả về sau khi khách thanh toán xong
public static String vnp_Returnurl = "http://localhost:8080/sportify/checkoutResult";
```
*(Hiện tại code đã cấu hình sẵn mã Test Sandbox. Nếu bạn đưa lên thực tế, hãy thay bằng mã do VNPAY cung cấp cho doanh nghiệp của bạn).*

### Bước 4: Chạy ứng dụng
- Mở dự án bằng IDE của bạn.
- Tìm file chạy chính: `src/main/java/duan/sportify/SportifyApplication.java`.
- Bấm chuột phải chọn **Run As -> Java Application** (hoặc Run trên Spring Boot Dashboard).
- Chờ Terminal báo `Started SportifyApplication in ... seconds`.
- Mở trình duyệt web và truy cập địa chỉ: `http://localhost:8080`

---

## 👥 Phân quyền tài khoản Demo (Mặc định)
Hệ thống đã có sẵn một số tài khoản dùng để kiểm thử (Test):
- **Admin:** (Email: `admin@gmail.com` / Pass: `123`)
- **Nhân viên (Staff):** (Email: `staff@gmail.com` / Pass: `123`)
- **Khách hàng:** Đăng ký tài khoản mới trên giao diện web.

Chúc bạn có một kỳ bảo vệ đồ án thành công rực rỡ! 🎉

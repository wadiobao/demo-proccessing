# Hướng dẫn Frontend sử dụng API với Cookie-based Authentication

**Nguyên tắc chung:**

*   Frontend **không cần** lưu trữ token (access token, refresh token) trong `localStorage`, `sessionStorage` hay bất kỳ nơi nào khác.
*   Frontend **không cần** đính kèm token vào header `Authorization` cho mỗi request.
*   Trình duyệt sẽ **tự động** gửi các cookie chứa token (nếu có) cho các request đến cùng domain (hoặc domain con) mà cookie được thiết lập.

---

#### 1. Đăng nhập (Login)

*   **Endpoint:** `POST /auth/login`
*   **Mô tả:** Gửi thông tin đăng nhập (username, password) đến backend.
*   **Cách dùng:**
    *   Frontend gửi request POST với `username` và `password` trong body.
    *   Nếu đăng nhập thành công, backend sẽ thiết lập `access_token` và `refresh_token` dưới dạng `HttpOnly` cookie trong response.
    *   Trình duyệt sẽ tự động lưu trữ các cookie này. Frontend **không cần** làm gì thêm để lấy token.
    *   Frontend chỉ cần kiểm tra trạng thái HTTP (ví dụ: 200 OK) hoặc thông báo thành công từ body response để biết đăng nhập có thành công hay không.

**Ví dụ (sử dụng `fetch`):**

```javascript
async function login(username, password) {
    try {
        const response = await fetch('http://localhost:8080/auth/login', { // Thay đổi URL nếu cần
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
            credentials: 'include' // RẤT QUAN TRỌNG: Đảm bảo trình duyệt gửi và nhận cookie
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Đăng nhập thành công!', data);
            // Chuyển hướng người dùng hoặc cập nhật UI
            window.location.href = '/dashboard';
        } else {
            const errorData = await response.json();
            console.error('Đăng nhập thất bại:', errorData.message);
            // Hiển thị thông báo lỗi cho người dùng
        }
    } catch (error) {
        console.error('Lỗi kết nối:', error);
    }
}

// Gọi hàm đăng nhập
// login('your_username', 'your_password');
```

---

#### 2. Gọi các API cần xác thực (Authenticated Requests)

*   **Mô tả:** Đối với bất kỳ API nào yêu cầu người dùng đã đăng nhập (ví dụ: lấy thông tin profile, tạo bài viết, v.v.).
*   **Cách dùng:**
    *   Frontend chỉ cần gửi request như bình thường.
    *   Trình duyệt sẽ **tự động** đính kèm cookie `access_token` (và `refresh_token`) vào request nếu chúng hợp lệ và thuộc cùng domain.
    *   Frontend **không cần** thêm header `Authorization`.

**Ví dụ (sử dụng `fetch`):**

```javascript
async function getProtectedData() {
    try {
        const response = await fetch('http://localhost:8080/api/protected-resource', { // Thay đổi URL nếu cần
            method: 'GET',
            credentials: 'include' // RẤT QUAN TRỌNG: Đảm bảo trình duyệt gửi cookie
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Dữ liệu bảo vệ:', data);
            return data;
        } else if (response.status === 401) {
            console.warn('Access Token hết hạn hoặc không hợp lệ. Đang thử refresh...');
            // Xử lý refresh token (xem phần 3)
            await handleTokenRefresh(); // Gọi hàm refresh token
            // Thử lại request ban đầu sau khi refresh thành công
            return getProtectedData();
        } else {
            const errorData = await response.json();
            console.error('Lỗi khi lấy dữ liệu bảo vệ:', errorData.message);
        }
    } catch (error) {
        console.error('Lỗi kết nối:', error);
    }
}

// getProtectedData();
```

---

#### 3. Refresh Token (Làm mới Token)

*   **Endpoint:** `POST /auth/refresh`
*   **Mô tả:** Khi `access_token` hết hạn (thường nhận được lỗi 401 Unauthorized từ backend), frontend cần yêu cầu backend cấp một `access_token` mới bằng cách sử dụng `refresh_token`.
*   **Cách dùng:**
    *   Frontend gửi request POST đến `/auth/refresh`.
    *   Trình duyệt sẽ **tự động** đính kèm cookie `refresh_token`.
    *   Backend sẽ kiểm tra `refresh_token`, nếu hợp lệ, nó sẽ tạo và thiết lập một `access_token` mới dưới dạng cookie trong response.
    *   Frontend chỉ cần kiểm tra trạng thái HTTP 200 OK.

**Ví dụ (sử dụng `fetch` và một hàm xử lý lỗi toàn cục):**

```javascript
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

async function handleTokenRefresh() {
    if (isRefreshing) {
        return new Promise(function(resolve, reject) {
            failedQueue.push({ resolve, reject });
        });
    }

    isRefreshing = true;

    try {
        const response = await fetch('http://localhost:8080/auth/refresh', { // Thay đổi URL nếu cần
            method: 'POST',
            credentials: 'include' // RẤT QUAN TRỌNG
        });

        if (response.ok) {
            console.log('Refresh Token thành công. Access Token mới đã được thiết lập.');
            isRefreshing = false;
            processQueue(null); // Xử lý các request bị lỗi trong hàng đợi
            return true;
        } else {
            console.error('Refresh Token thất bại. Vui lòng đăng nhập lại.');
            isRefreshing = false;
            processQueue('Refresh Token thất bại');
            // Chuyển hướng về trang đăng nhập
            window.location.href = '/login';
            return false;
        }
    } catch (error) {
        console.error('Lỗi kết nối khi refresh token:', error);
        isRefreshing = false;
        processQueue(error);
        window.location.href = '/login';
        return false;
    }
}

// Ví dụ tích hợp với Axios Interceptor (nếu bạn dùng Axios)
// axios.interceptors.response.use(
//     response => response,
//     async error => {
//         const originalRequest = error.config;
//         if (error.response.status === 401 && !originalRequest._retry) {
//             originalRequest._retry = true;
//             try {
//                 await handleTokenRefresh();
//                 return axios(originalRequest); // Thử lại request ban đầu
//             } catch (refreshError) {
//                 return Promise.reject(refreshError);
//             }
//         }
//         return Promise.reject(error);
//     }
// );
```

---

#### 4. Đăng xuất (Logout)

*   **Endpoint:** `POST /auth/logout`
*   **Mô tả:** Yêu cầu backend vô hiệu hóa token và xóa cookie trên trình duyệt.
*   **Cách dùng:**
    *   Frontend gửi request POST đến `/auth/logout`.
    *   Trình duyệt sẽ tự động đính kèm cookie `access_token`.
    *   Backend sẽ vô hiệu hóa token và gửi lại response với các cookie `access_token` và `refresh_token` có `Max-Age=0`, khiến trình duyệt xóa chúng.
    *   Frontend chỉ cần kiểm tra trạng thái HTTP 200 OK và sau đó chuyển hướng người dùng về trang đăng nhập hoặc cập nhật UI.

**Ví dụ (sử dụng `fetch`):**

```javascript
async function logout() {
    try {
        const response = await fetch('http://localhost:8080/auth/logout', { // Thay đổi URL nếu cần
            method: 'POST',
            credentials: 'include' // RẤT QUAN TRỌNG
        });

        if (response.ok) {
            console.log('Đăng xuất thành công!');
            // Chuyển hướng về trang đăng nhập
            window.location.href = '/login';
        } else {
            const errorData = await response.json();
            console.error('Đăng xuất thất bại:', errorData.message);
        }
    } catch (error) {
        console.error('Lỗi kết nối:', error);
    }
}

// logout();
```

---

#### 5. Cấu hình CORS (nếu Frontend và Backend khác domain/port)

Nếu frontend và backend chạy trên các domain hoặc port khác nhau (ví dụ: frontend trên `localhost:3000`, backend trên `localhost:8080`), bạn **bắt buộc** phải cấu hình CORS đúng cách.

*   **Phía Frontend:**
    *   Luôn thêm `credentials: 'include'` vào tất cả các request `fetch` hoặc cấu hình `axios` để gửi cookie.
        ```javascript
        // Với fetch
        fetch(url, {
            // ...
            credentials: 'include'
        });

        // Với Axios (cấu hình global)
        // axios.defaults.withCredentials = true;
        ```
*   **Phía Backend:**
    *   Đảm bảo `SecurityConfig.java` của bạn cho phép `Access-Control-Allow-Credentials: true` và `Access-Control-Allow-Origin` được cấu hình chính xác (không phải `*` khi `credentials: 'include'` được sử dụng). Bạn đã có cấu hình CORS trong `SecurityConfig`, hãy đảm bảo `configuration.setAllowCredentials(true);` được thêm vào nếu chưa có.

---

**Tóm tắt cho Frontend:**

Chỉ cần gửi request như bình thường, đảm bảo `credentials: 'include'` được thiết lập. Trình duyệt sẽ lo phần còn lại của việc gửi và nhận cookie. Xử lý lỗi 401 để kích hoạt cơ chế refresh token tự động.

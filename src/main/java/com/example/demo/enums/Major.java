package com.example.demo.enums;

import java.util.Optional;

/**
 * Enum đại diện cho các ngành học cùng với mã ngành tương ứng.
 */
public enum Major {

    BAO_VE_THUC_VAT("7620102", "Bảo vệ thực vật"),
    BENH_HOC_THUY_SAN("7620302", "Bệnh học thủy sản"),
    CHAN_NUOI("7620105", "Chăn nuôi"),
    CHAN_NUOI_THU_Y("7620106", "Chăn nuôi thú y"),
    CONG_NGHE_KY_THUAT_CO_DIEN_TU("7510203", "Công nghệ kỹ thuật cơ điện tử"),
    CONG_NGHE_KY_THUAT_O_TO("7510205", "Công nghệ kỹ thuật ô tô"),
    CONG_NGHE_RAU_HOA_QUA_VA_CANH_QUAN("7620113", "Công nghệ rau hoa quả và cảnh quan"),
    CONG_NGHE_SINH_DUOC("7420215", "Công nghệ sinh dược"),
    CONG_NGHE_SINH_HOC("7420201", "Công nghệ Sinh học"),
    CONG_NGHE_THONG_TIN("7480201", "Công nghệ thông tin"),
    CONG_NGHE_THUC_PHAM("7540101", "Công nghệ thực phẩm"),
    KE_TOAN("7340301", "Kế toán"),
    CONG_NGHE_VA_KINH_DOANH_THUC_PHAM("7540108", "Công nghệ và kinh doanh thực phẩm"),
    KINH_TE("7310101", "Kinh tế"),
    KINH_TE_DAU_TU("7310104", "Kinh tế đầu tư"),
    KINH_TE_NONG_NGHIEP("7620115", "Kinh tế nông nghiệp"),
    KINH_TE_TAI_CHINH("7310112", "Kinh tế tài chính"),
    KINH_TE_SO("7310109", "Kinh tế số"),
    KY_THUAT_CO_KHI("7520103", "Kỹ thuật cơ khí"),
    KY_THUAT_DIEN("7520201", "Kỹ thuật điện"),
    KY_THUAT_DIEU_KHIEN_VA_TU_DONG_HOA("7520216", "Kỹ thuật điều khiển và Tự động hóa"),
    KHOA_HOC_CAY_TRONG("7620110", "Khoa học cây trồng"),
    KHOA_HOC_DAT("7620103", "Khoa học Đất"),
    KHOA_HOC_MOI_TRUONG("7440301", "Khoa học Môi trường"),
    KHOA_HOC_DU_LIEU_VA_TRI_TUE_NHAN_TAO("7480112", "Khoa học dữ liệu và Trí tuệ nhân tạo"),
    LOGISTICS_VA_QUAN_LY_CHUOI_CUNG_UNG("7510605", "Logistics và quản lý chuỗi cung ứng"),
    LUAT("7380101", "Luật"),
    MANG_MAY_TINH_VA_TRUYEN_THONG_DU_LIEU("7480102", "Mạng máy tính và truyền thông dữ liệu"),
    NONG_NGHIEP_CONG_NGHE_CAO("7620118", "Nông nghiệp công nghệ cao"),
    NUOI_TRONG_THUY_SAN("7620301", "Nuôi trồng thủy sản"),
    NGON_NGU_ANH("7220201", "Ngôn ngữ Anh"),
    QUAN_LY_BAT_DONG_SAN("7850118", "Quản lý bất động sản"),
    QUAN_LY_DAT_DAI("7850103", "Quản lý đất đai"),
    QUAN_LY_KINH_TE("7310110", "Quản lý kinh tế"),
    QUAN_LY_TAI_NGUYEN_VA_MOI_TRUONG("7850101", "Quản lý tài nguyên và môi trường"),
    QUAN_LY_VA_PHAT_TRIEN_DU_LICH("7340418", "Quản lý và phát triển du lịch"),
    QUAN_LY_VA_PHAT_TRIEN_NGUON_NHAN_LUC("7340411", "Quản lý và phát triển nguồn nhân lực"),
    QUAN_TRI_KINH_DOANH("7340101", "Quản trị Kinh doanh"),
    SU_PHAM_CONG_NGHE("7140246", "Sư phạm công nghệ"),
    TAI_CHINH_NGAN_HANG("7340201", "Tài chính - Ngân hàng"),
    THU_Y("7640101", "Thú y"),
    THUONG_MAI_DIEN_TU("7340122", "Thương mại điện tử"),
    XA_HOI_HOC("7310301", "Xã hội học");

    private final String code;
    private final String displayName;

    Major(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return this.displayName;
    }

    /**
     * Tìm một ngành học dựa trên mã ngành.
     * @param code Mã ngành cần tìm.
     * @return Một Optional chứa ngành học nếu tìm thấy, ngược lại là Optional rỗng.
     */
    public static Optional<Major> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        for (Major major : values()) {
            if (major.getCode().equals(code)) {
                return Optional.of(major);
            }
        }
        return Optional.empty();
    }
}

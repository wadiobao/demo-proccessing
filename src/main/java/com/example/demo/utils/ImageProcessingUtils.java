package com.example.demo.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.util.Arrays;

import org.springframework.stereotype.Component;

/**
 * Digital image processing toolkit for preprocessing and augmentation.
 * 
 * <p>
 * Cung cấp các bộ lọc làm mịn, cân bằng histogram, nhị phân hóa (Otsu)
 * và các kỹ thuật tăng cường dữ liệu (Xoay, Chỉnh sáng) phục vụ cho OCR.
 *
 * @since 1.0
 */
@Component
public class ImageProcessingUtils {

    /**
     * Resizing (thay đổi kích thước)
     * Đưa tệp về kích thước chuẩn.
     */
    /**
     * Standardizes image size to target dimensions using bilinear interpolation.
     * 
     * @param originalImage source buffer / ảnh nguồn
     * @param targetWidth   desired px width / chiều rộng đích
     * @param targetHeight  desired px height / chiều cao đích
     * @return resized image / ảnh đã thay đổi kích thước
     */
    public BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    /**
     * Grayscale Conversion (chuyển sang ảnh xám)
     */
    public BufferedImage toGrayscale(BufferedImage colorImage) {
        BufferedImage grayscaleImage = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = grayscaleImage.createGraphics();
        graphics.drawImage(colorImage, 0, 0, null);
        graphics.dispose();
        return grayscaleImage;
    }

    // low-pass filter to dampen high-frequency noise that typically interferes with
    // OCR character recognition
    // / bộ lọc thông thấp để làm dịu nhiễu tần số cao thường gây cản trở nhận dạng
    // ký tự OCR
    public BufferedImage reduceNoise(BufferedImage image) {
        float[] matrix = {
                1 / 16f, 1 / 8f, 1 / 16f,
                1 / 8f, 1 / 4f, 1 / 8f,
                1 / 16f, 1 / 8f, 1 / 16f
        };
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }

    // non-linear filter effective at removing 'salt and pepper' noise while
    // preserving sharp character edges
    // / bộ lọc phi tuyến tính hiệu quả trong việc loại bỏ nhiễu 'muối tiêu' trong
    // khi vẫn giữ được các cạnh ký tự sắc nét
    public BufferedImage medianFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                int[] pixels = new int[9];
                int k = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        Color c = new Color(image.getRGB(x + dx, y + dy));
                        pixels[k++] = c.getRed(); // Assuming grayscale
                    }
                }
                Arrays.sort(pixels);
                int median = pixels[4];
                result.setRGB(x, y, new Color(median, median, median).getRGB());
            }
        }
        return result;
    }

    // stretch contrast to full dynamic range [0, 255] to maximize separation
    // between foreground (text) and background
    // / kéo giãn độ tương phản sang toàn dải động [0, 255] để tối đa hóa sự phân
    // tách giữa tiền cảnh (văn bản) và hậu cảnh
    public BufferedImage normalize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int min = 255, max = 0;

        // identify global brightness bounds for linear scaling
        // / xác định các biên độ sáng toàn cục để thực hiện tỉ lệ tuyến tính
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = new Color(image.getRGB(x, y)).getRed();
                if (gray < min)
                    min = gray;
                if (gray > max)
                    max = gray;
            }
        }

        if (max == min)
            return image;

        BufferedImage normalized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = new Color(image.getRGB(x, y)).getRed();
                int norm = (int) (((double) (gray - min) / (max - min)) * 255);
                normalized.setRGB(x, y, new Color(norm, norm, norm).getRGB());
            }
        }
        return normalized;
    }

    // global contrast enhancement through cumulative distribution function mapping
    // / tăng cường độ tương phản toàn cục thông qua ánh xạ hàm phân phối tích lũy
    public BufferedImage equalizeHistogram(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                histogram[new Color(image.getRGB(x, y)).getRed()]++;
            }
        }

        int[] cdf = new int[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        float minCdf = 0;
        for (int i = 0; i < 256; i++) {
            if (cdf[i] > 0) {
                minCdf = cdf[i];
                break;
            }
        }

        BufferedImage equalized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = new Color(image.getRGB(x, y)).getRed();
                int newValue = Math.round(((cdf[gray] - minCdf) / (width * height - minCdf)) * 255);
                equalized.setRGB(x, y, new Color(newValue, newValue, newValue).getRGB());
            }
        }
        return equalized;
    }

    /**
     * Thresholding (nhị phân hóa) - Otsu's Method
     */
    /**
     * Converts grayscale image to binary (black/white) using Otsu's optimal
     * threshold.
     * 
     * @param image grayscale source / ảnh xám nguồn
     * @return binarized image / ảnh nhị phân
     */
    public BufferedImage binaryThreshold(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                histogram[new Color(image.getRGB(x, y)).getRed()]++;
            }
        }

        double sum = 0;
        for (int i = 0; i < 256; i++)
            sum += i * histogram[i];

        double sumB = 0;
        int wB = 0;
        int wF = 0;
        double varMax = 0;
        int threshold = 0;

        int total = width * height;

        // iterate all possible thresholds to minimize intra-class variance (maximize
        // inter-class variance)
        // / lặp qua tất cả các ngưỡng khả thi để giảm thiểu phương sai trong lớp (tối
        // đa hóa phương sai giữa các lớp)
        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0)
                continue;
            wF = total - wB;
            if (wF == 0)
                break;

            sumB += (double) (i * histogram[i]);
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;

            double varBetween = (double) wB * (double) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        BufferedImage thresholded = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = new Color(image.getRGB(x, y)).getRed();
                int value = (gray > threshold) ? 0xFFFFFF : 0x000000;
                thresholded.setRGB(x, y, value);
            }
        }
        return thresholded;
    }

    /**
     * Edge Detection (phát hiện biên) - Sobel Operator
     */
    public BufferedImage edgeDetection(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // convolution kernels to calculate intensity gradients in X and Y directions
        // / hạt nhân tích chập để tính toán gradient cường độ theo hướng X và Y
        int[][] sobelX = {
                { -1, 0, 1 },
                { -2, 0, 2 },
                { -1, 0, 1 }
        };
        int[][] sobelY = {
                { -1, -2, -1 },
                { 0, 0, 0 },
                { 1, 2, 1 }
        };

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int pX = 0, pY = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int gray = new Color(image.getRGB(x + i, y + j)).getRed();
                        pX += gray * sobelX[i + 1][j + 1];
                        pY += gray * sobelY[i + 1][j + 1];
                    }
                }
                int magnitude = (int) Math.sqrt(pX * pX + pY * pY);
                magnitude = Math.min(255, magnitude);
                result.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        return result;
    }

    /**
     * Data Augmentation: Rotation (xoay ảnh)
     */
    /**
     * Rotates image by a specified angle while expanding canvas to prevent
     * clipping.
     * 
     * @param image source buffer / ảnh nguồn
     * @param angle rotation in degrees / góc xoay tính theo độ
     * @return rotated image with expanded bounds / ảnh đã xoay
     */
    public BufferedImage rotate(BufferedImage image, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = image.getWidth();
        int h = image.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);
        at.rotate(rads, w / 2, h / 2);
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return rotated;
    }

    /**
     * Data Augmentation: Adjust Brightness/Contrast
     */
    public BufferedImage adjustBrightnessContrast(BufferedImage image, float brightness, float contrast) {
        RescaleOp op = new RescaleOp(contrast, brightness, null);
        return op.filter(image, null);
    }
}

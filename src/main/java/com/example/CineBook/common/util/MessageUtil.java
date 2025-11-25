package com.example.CineBook.common.util;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageUtil {

    private final MessageSource messageSource;

    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Lấy thông điệp từ file properties dựa vào mã lỗi và locale của request hiện tại.
     * Nếu không tìm thấy, sẽ thử với tiếng Anh. Nếu vẫn không tìm thấy, trả về tên của key.
     *
     * @param key Mã lỗi (ví dụ: ErrorCode.LOGIN_FAIL)
     * @return Chuỗi thông điệp đã được quốc tế hóa.
     */
    public String getMessage(Enum<?> key) {
        return getMessage(key, (Object[]) null);
    }

    /**
     * Lấy thông điệp từ file properties và điền các tham số.
     * Nếu không tìm thấy, sẽ thử với tiếng Anh. Nếu vẫn không tìm thấy, trả về tên của key.
     *
     * @param key  Mã lỗi
     * @param args Các tham số để điền vào thông điệp
     * @return Chuỗi thông điệp đã được quốc tế hóa và định dạng.
     */
    public String getMessage(Enum<?> key, Object... args) {
        String code = key.name();
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            // Fallback to English. If that also fails, use the code as the default message.
            return messageSource.getMessage(code, args, code, Locale.ENGLISH);
        }
    }
}

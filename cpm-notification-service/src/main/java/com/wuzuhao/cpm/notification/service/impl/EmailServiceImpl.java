package com.wuzuhao.cpm.notification.service.impl;

import com.wuzuhao.cpm.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@cpm.com}")
    private String from;

    @Override
    public boolean sendEmail(String to, String subject, String content) {
        if (mailSender == null) {
            log.warn("邮件服务未配置（JavaMailSender为null），无法发送邮件");
            log.warn("请检查application.yml中的邮件配置");
            return false;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功: {} -> {}", from, to);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendVerificationCode(String to, String code, String type, String baseUrl) {
        String subject = "验证码 - 社区人口管理系统";
        String htmlContent = buildVerificationCodeHtml(code, type, baseUrl);
        return sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 构建验证码邮件的HTML内容（卡片样式）
     */
    private String buildVerificationCodeHtml(String code, String type, String baseUrl) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>验证码邮件</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f5f7fa;\">\n" +
                "    <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse; background-color: #f5f7fa; padding: 40px 20px;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table role=\"presentation\" style=\"max-width: 600px; width: 100%; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); overflow: hidden;\">\n" +
                "                    <!-- 头部装饰条 -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); height: 6px;\"></td>\n" +
                "                    </tr>\n" +
                "                    <!-- 内容区域 -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 30px;\">\n" +
                "                            <!-- Logo/标题区域 -->\n" +
                "                            <div style=\"text-align: center; margin-bottom: 30px;\">\n" +
                "                                <div style=\"display: inline-block; width: 80px; height: 60px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px; line-height: 60px; margin-bottom: 15px;\">\n" +
                "                                    <span style=\"color: #ffffff; font-size: 24px; font-weight: bold; letter-spacing: 1px;\">CPM</span>\n" +
                "                                </div>\n" +
                "                                <h1 style=\"margin: 0; color: #333333; font-size: 24px; font-weight: 600;\">验证码邮件</h1>\n" +
                "                            </div>\n" +
                "                            \n" +
                "                            <!-- 提示文字 -->\n" +
                "                            <p style=\"margin: 0 0 25px 0; color: #666666; font-size: 15px; line-height: 1.6; text-align: center;\">\n" +
                "                                您好！您正在进行身份验证，请使用以下验证码：\n" +
                "                            </p>\n" +
                "                            \n" +
                "                            <!-- 验证码卡片 -->\n" +
                "                            <div style=\"background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%); border-radius: 8px; padding: 25px; margin: 30px 0; text-align: center; border: 2px dashed #667eea;\">\n" +
                "                                <p style=\"margin: 0 0 10px 0; color: #999999; font-size: 13px; letter-spacing: 1px;\">您的验证码</p>\n" +
                "                                <div style=\"font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace; margin: 10px 0;\">\n" +
                "                                    " + code + "\n" +
                "                                </div>\n" +
                "                            </div>\n" +
                "                            \n" +
                "                            <!-- 有效期提示 -->\n" +
                "                            <div style=\"background-color: #fff7e6; border-left: 4px solid #ffa940; padding: 15px; border-radius: 4px; margin: 25px 0;\">\n" +
                "                                <p style=\"margin: 0; color: #d46b08; font-size: 14px; line-height: 1.5;\">\n" +
                "                                    <strong>⚠️ 重要提示：</strong><br>\n" +
                "                                    • 验证码有效期为 <strong>5分钟</strong>，请及时使用<br>\n" +
                "                                    • 请勿将验证码泄露给他人<br>\n" +
                "                                    • 如非本人操作，请忽略此邮件\n" +
                "                                </p>\n" +
                "                            </div>\n" +
                "                            \n" +
                "                            <!-- 分隔线 -->\n" +
                "                            <div style=\"border-top: 1px solid #e8e8e8; margin: 30px 0;\"></div>\n" +
                "                            \n" +
                "                            <!-- 底部信息 -->\n" +
                "                            <p style=\"margin: 0; color: #999999; font-size: 13px; text-align: center; line-height: 1.6;\">\n" +
                "                                此邮件由系统自动发送，请勿回复。<br>\n" +
                "                                <span style=\"color: #667eea;\">社区人口管理系统</span>\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 发送HTML格式邮件
     */
    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mailSender == null) {
            log.warn("邮件服务未配置（JavaMailSender为null），无法发送邮件");
            log.warn("请检查application.yml中的邮件配置");
            return false;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示HTML格式
            
            mailSender.send(message);
            log.info("HTML邮件发送成功: {} -> {}", from, to);
            return true;
        } catch (Exception e) {
            log.error("HTML邮件发送失败: {}", e.getMessage(), e);
            return false;
        }
    }
}

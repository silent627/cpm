package com.wuzuhao.cpm.notification.service.impl;

import com.wuzuhao.cpm.notification.service.CaptchaService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int CAPTCHA_LENGTH = 4;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 40;

    public CaptchaServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Map<String, Object> generateCaptcha() {
        // 生成随机验证码
        String code = generateRandomCode();
        String key = System.currentTimeMillis() + "_" + new Random().nextInt(10000);
        
        // 将验证码存入Redis，5分钟过期
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code.toLowerCase(), 
                CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 生成验证码图片
        BufferedImage image = generateImage(code);
        
        // 转换为Base64
        String imageBase64 = imageToBase64(image);
        
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("image", "data:image/png;base64," + imageBase64);
        return result;
    }

    @Override
    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        
        String storedCode = (String) redisTemplate.opsForValue().get(CAPTCHA_PREFIX + key);
        if (storedCode == null) {
            return false;
        }
        
        // 验证码在5分钟内有效，使用后不会失效，可以重复使用
        return storedCode.equalsIgnoreCase(code.trim());
    }

    /**
     * 生成随机验证码字符串
     */
    private String generateRandomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除容易混淆的字符
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 生成验证码图片
     */
    private BufferedImage generateImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        // 绘制干扰线
        Random random = new Random();
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(IMAGE_WIDTH);
            int y1 = random.nextInt(IMAGE_HEIGHT);
            int x2 = random.nextInt(IMAGE_WIDTH);
            int y2 = random.nextInt(IMAGE_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            int x = 20 + i * 25;
            int y = 28 + random.nextInt(10);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
        
        g.dispose();
        return image;
    }

    /**
     * 将图片转换为Base64
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
}

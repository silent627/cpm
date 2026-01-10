-- ============================================
-- CPM社区人口管理系统数据库初始化脚本
-- 数据库：cpm_db
-- 包含5张表：sys_user, sys_admin, resident, household, household_member
-- 总数据量：约1000条（sys_admin仅5条）
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `cpm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `cpm_db`;

-- ============================================
-- 1. 用户表 (sys_user)
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：USER-普通用户, ADMIN-管理员',
  `status` INT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 管理员表 (sys_admin)
-- ============================================
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user表）',
  `admin_no` VARCHAR(50) DEFAULT NULL COMMENT '管理员编号',
  `department` VARCHAR(100) DEFAULT NULL COMMENT '部门',
  `position` VARCHAR(50) DEFAULT NULL COMMENT '职位',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_admin_no` (`admin_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理员表';

-- ============================================
-- 3. 居民表 (resident)
-- ============================================
DROP TABLE IF EXISTS `resident`;
CREATE TABLE `resident` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '居民ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（关联sys_user表）',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
  `gender` INT DEFAULT NULL COMMENT '性别：0-女, 1-男',
  `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
  `nationality` VARCHAR(50) DEFAULT '汉族' COMMENT '民族',
  `registered_address` VARCHAR(255) DEFAULT NULL COMMENT '户籍地址',
  `current_address` VARCHAR(255) DEFAULT NULL COMMENT '现居住地址',
  `occupation` VARCHAR(100) DEFAULT NULL COMMENT '职业',
  `education` VARCHAR(50) DEFAULT NULL COMMENT '文化程度',
  `marital_status` INT DEFAULT NULL COMMENT '婚姻状况：0-未婚, 1-已婚, 2-离异, 3-丧偶',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人电话',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
  `id_card_photo` VARCHAR(255) DEFAULT NULL COMMENT '身份证照片路径',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_id_card` (`id_card`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='居民表';

-- ============================================
-- 4. 户籍表 (household)
-- ============================================
DROP TABLE IF EXISTS `household`;
CREATE TABLE `household` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '户籍ID',
  `head_id` BIGINT DEFAULT NULL COMMENT '户主ID（关联resident表）',
  `head_name` VARCHAR(50) DEFAULT NULL COMMENT '户主姓名',
  `head_id_card` VARCHAR(18) DEFAULT NULL COMMENT '户主身份证号',
  `household_no` VARCHAR(50) DEFAULT NULL COMMENT '户籍编号',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '户籍地址',
  `household_type` INT DEFAULT 1 COMMENT '户别：1-家庭户, 2-集体户',
  `member_count` INT DEFAULT 1 COMMENT '户人数',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `move_in_date` DATETIME DEFAULT NULL COMMENT '迁入日期',
  `move_out_date` DATETIME DEFAULT NULL COMMENT '迁出日期',
  `move_in_reason` VARCHAR(255) DEFAULT NULL COMMENT '迁入原因',
  `move_out_reason` VARCHAR(255) DEFAULT NULL COMMENT '迁出原因',
  `status` INT DEFAULT 1 COMMENT '状态：0-迁出, 1-正常',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_household_no` (`household_no`),
  KEY `idx_head_id` (`head_id`),
  KEY `idx_head_id_card` (`head_id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='户籍表';

-- ============================================
-- 5. 户籍成员关系表 (household_member)
-- ============================================
DROP TABLE IF EXISTS `household_member`;
CREATE TABLE `household_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
  `household_id` BIGINT NOT NULL COMMENT '户籍ID（关联household表）',
  `resident_id` BIGINT NOT NULL COMMENT '居民ID（关联resident表）',
  `relationship` VARCHAR(50) DEFAULT NULL COMMENT '与户主关系：户主、配偶、子女、父母、其他',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_household_id` (`household_id`),
  KEY `idx_resident_id` (`resident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='户籍成员关系表';

-- ============================================
-- 插入测试数据
-- ============================================

-- 插入用户数据（约250条）
-- 密码统一为：123456 (MD5加密后的值: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`, `create_time`, `update_time`, `deleted`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000001', 'admin@cpm.com', 'ADMIN', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
('admin001', 'e10adc3949ba59abbe56e057f20f883e', '张管理员', '13800000002', 'admin001@cpm.com', 'ADMIN', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
('admin002', 'e10adc3949ba59abbe56e057f20f883e', '李管理员', '13800000003', 'admin002@cpm.com', 'ADMIN', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
('admin003', 'e10adc3949ba59abbe56e057f20f883e', '王管理员', '13800000004', 'admin003@cpm.com', 'ADMIN', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
('admin004', 'e10adc3949ba59abbe56e057f20f883e', '刘管理员', '13800000005', 'admin004@cpm.com', 'ADMIN', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
('user001', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13800138001', 'user001@cpm.com', 'USER', 1, '2024-01-02 10:00:00', '2024-01-02 10:00:00', 0),
('user002', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13800138002', 'user002@cpm.com', 'USER', 1, '2024-01-02 10:00:00', '2024-01-02 10:00:00', 0),
('user003', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13800138003', 'user003@cpm.com', 'USER', 1, '2024-01-02 10:00:00', '2024-01-02 10:00:00', 0),
('user004', 'e10adc3949ba59abbe56e057f20f883e', '赵六', '13800138004', 'user004@cpm.com', 'USER', 1, '2024-01-02 10:00:00', '2024-01-02 10:00:00', 0),
('user005', 'e10adc3949ba59abbe56e057f20f883e', '钱七', '13800138005', 'user005@cpm.com', 'USER', 1, '2024-01-02 10:00:00', '2024-01-02 10:00:00', 0);

-- 使用存储过程批量插入用户数据（240条）
DROP PROCEDURE IF EXISTS insert_users;
DELIMITER $$
CREATE PROCEDURE insert_users()
BEGIN
  DECLARE i INT DEFAULT 6;
  WHILE i <= 245 DO
    INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`, `create_time`, `update_time`, `deleted`)
    VALUES (
      CONCAT('user', LPAD(i, 3, '0')),
      'e10adc3949ba59abbe56e057f20f883e',
      CONCAT('用户', i),
      CONCAT('138', LPAD(13800 + i, 8, '0')),
      CONCAT('user', LPAD(i, 3, '0'), '@cpm.com'),
      'USER',
      1,
      DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY),
      DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY),
      0
    );
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;

CALL insert_users();
DROP PROCEDURE insert_users;

-- 插入管理员数据（5条）
INSERT INTO `sys_admin` (`user_id`, `admin_no`, `department`, `position`, `remark`, `create_time`, `update_time`, `deleted`) VALUES
(1, 'ADMIN001', '系统管理部', '系统管理员', '系统超级管理员', '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
(2, 'ADMIN002', '人口管理部', '部门主管', '负责人口信息管理', '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
(3, 'ADMIN003', '户籍管理部', '部门主管', '负责户籍信息管理', '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
(4, 'ADMIN004', '数据统计部', '数据分析师', '负责数据统计分析', '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0),
(5, 'ADMIN005', '系统维护部', '运维工程师', '负责系统维护', '2024-01-01 10:00:00', '2024-01-01 10:00:00', 0);

-- 插入居民数据（约450条）
-- 使用存储过程批量生成居民数据
DROP PROCEDURE IF EXISTS insert_residents;
DELIMITER $$
CREATE PROCEDURE insert_residents()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE gender_val INT;
  DECLARE birth_year INT;
  DECLARE birth_month INT;
  DECLARE birth_day INT;
  DECLARE id_card_prefix VARCHAR(6);
  DECLARE id_card_suffix VARCHAR(12);
  DECLARE id_card_full VARCHAR(18);
  DECLARE birth_date_val DATE;
  DECLARE name_prefix VARCHAR(10);
  DECLARE name_suffix VARCHAR(10);
  DECLARE full_name VARCHAR(50);
  
  -- 姓氏列表
  DECLARE surnames TEXT DEFAULT '张王李赵刘陈杨黄周吴徐孙胡朱高林何郭马罗梁宋郑谢韩唐冯于董萧程曹袁邓许傅沈曾彭吕苏卢蒋蔡贾丁魏薛叶阎余潘杜戴夏锺汪田任姜范方石姚谭廖邹熊金陆郝孔白崔康毛邱秦江史顾侯邵孟龙万段雷钱汤尹黎易常武乔贺赖龚文';
  DECLARE given_names TEXT DEFAULT '伟芳娜敏静丽强磊军洋勇艳杰娟涛明超秀英华文辉力建平刚桂英';
  
  WHILE i <= 450 DO
    -- 生成性别（随机）
    SET gender_val = FLOOR(RAND() * 2);
    
    -- 生成出生日期（1950-2005年）
    SET birth_year = 1950 + FLOOR(RAND() * 56);
    SET birth_month = 1 + FLOOR(RAND() * 12);
    SET birth_day = 1 + FLOOR(RAND() * 28);
    SET birth_date_val = DATE(CONCAT(birth_year, '-', LPAD(birth_month, 2, '0'), '-', LPAD(birth_day, 2, '0')));
    
    -- 生成身份证号（18位：6位地区码 + 8位出生日期 + 3位顺序码 + 1位校验码）
    -- 地区码：110101-110200（北京市，6位）
    SET id_card_prefix = CONCAT('110', LPAD(101 + FLOOR(RAND() * 100), 3, '0'));
    -- 出生日期：YYYYMMDD（8位）
    SET @birth_date_str = CONCAT(
      LPAD(birth_year, 4, '0'),
      LPAD(birth_month, 2, '0'),
      LPAD(birth_day, 2, '0')
    );
    -- 顺序码：3位（000-999），最后一位决定性别（奇数=男，偶数=女）
    SET @seq_first_two = LPAD(FLOOR(RAND() * 100), 2, '0');
    -- 根据性别生成最后一位顺序码（奇数=男，偶数=女）
    IF gender_val = 1 THEN
      -- 男性：奇数（1,3,5,7,9）
      SET @seq_last = LPAD(1 + FLOOR(RAND() * 5) * 2, 1, '0');
    ELSE
      -- 女性：偶数（0,2,4,6,8）
      SET @seq_last = LPAD(FLOOR(RAND() * 5) * 2, 1, '0');
    END IF;
    SET @sequence = CONCAT(@seq_first_two, @seq_last);
    -- 校验码：1位（0-9，简化处理）
    SET @check_code = FLOOR(RAND() * 10);
    -- 组合成18位身份证号（6+8+3+1=18位）
    SET id_card_full = CONCAT(id_card_prefix, @birth_date_str, @sequence, @check_code);
    
    -- 生成姓名
    SET name_prefix = SUBSTRING(surnames, 1 + FLOOR(RAND() * CHAR_LENGTH(surnames)), 1);
    SET name_suffix = SUBSTRING(given_names, 1 + FLOOR(RAND() * CHAR_LENGTH(given_names)), 1);
    SET full_name = CONCAT(name_prefix, name_suffix);
    IF RAND() > 0.5 THEN
      SET full_name = CONCAT(full_name, SUBSTRING(given_names, 1 + FLOOR(RAND() * CHAR_LENGTH(given_names)), 1));
    END IF;
    
    INSERT INTO `resident` (
      `user_id`, `real_name`, `id_card`, `gender`, `birth_date`, `nationality`,
      `registered_address`, `current_address`, `occupation`, `education`,
      `marital_status`, `contact_phone`, `emergency_contact`, `emergency_phone`,
      `create_time`, `update_time`, `deleted`
    ) VALUES (
      CASE WHEN i <= 245 THEN i ELSE NULL END,
      full_name,
      id_card_full,
      gender_val,
      birth_date_val,
      CASE FLOOR(RAND() * 5)
        WHEN 0 THEN '汉族'
        WHEN 1 THEN '回族'
        WHEN 2 THEN '满族'
        WHEN 3 THEN '蒙古族'
        ELSE '壮族'
      END,
      CONCAT('北京市', CASE FLOOR(RAND() * 5) WHEN 0 THEN '朝阳区' WHEN 1 THEN '海淀区' WHEN 2 THEN '西城区' WHEN 3 THEN '东城区' ELSE '丰台区' END, CASE FLOOR(RAND() * 10) WHEN 0 THEN '第一街道' WHEN 1 THEN '第二街道' WHEN 2 THEN '第三街道' WHEN 3 THEN '第四街道' WHEN 4 THEN '第五街道' WHEN 5 THEN '第六街道' WHEN 6 THEN '第七街道' WHEN 7 THEN '第八街道' WHEN 8 THEN '第九街道' ELSE '第十街道' END, FLOOR(RAND() * 100) + 1, '号'),
      CONCAT('北京市', CASE FLOOR(RAND() * 5) WHEN 0 THEN '朝阳区' WHEN 1 THEN '海淀区' WHEN 2 THEN '西城区' WHEN 3 THEN '东城区' ELSE '丰台区' END, CASE FLOOR(RAND() * 10) WHEN 0 THEN '第一街道' WHEN 1 THEN '第二街道' WHEN 2 THEN '第三街道' WHEN 3 THEN '第四街道' WHEN 4 THEN '第五街道' WHEN 5 THEN '第六街道' WHEN 6 THEN '第七街道' WHEN 7 THEN '第八街道' WHEN 8 THEN '第九街道' ELSE '第十街道' END, FLOOR(RAND() * 100) + 1, '号'),
      CASE FLOOR(RAND() * 8)
        WHEN 0 THEN '教师'
        WHEN 1 THEN '医生'
        WHEN 2 THEN '工程师'
        WHEN 3 THEN '公务员'
        WHEN 4 THEN '企业职员'
        WHEN 5 THEN '自由职业'
        WHEN 6 THEN '学生'
        ELSE '退休'
      END,
      CASE FLOOR(RAND() * 6)
        WHEN 0 THEN '小学'
        WHEN 1 THEN '初中'
        WHEN 2 THEN '高中'
        WHEN 3 THEN '大专'
        WHEN 4 THEN '本科'
        ELSE '研究生'
      END,
      FLOOR(RAND() * 4),
      CONCAT('138', LPAD(1000 + i, 8, '0')),
      CONCAT('紧急联系人', i),
      CONCAT('139', LPAD(2000 + i, 8, '0')),
      DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY),
      DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY),
      0
    );
    
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;

CALL insert_residents();
DROP PROCEDURE insert_residents;

-- 插入户籍数据（约200条）
DROP PROCEDURE IF EXISTS insert_households;
DELIMITER $$
CREATE PROCEDURE insert_households()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE head_id_val BIGINT;
  DECLARE head_name_val VARCHAR(50);
  DECLARE head_id_card_val VARCHAR(18);
  DECLARE household_no_val VARCHAR(50);
  DECLARE address_val VARCHAR(255);
  DECLARE household_type_val INT;
  DECLARE member_count_val INT;
  DECLARE move_in_date_val DATETIME;
  DECLARE status_val INT;
  
  WHILE i <= 200 DO
    -- 随机选择一个居民作为户主（从已插入的居民中选择）
    SET head_id_val = 1 + FLOOR(RAND() * 450);
    SELECT `real_name`, `id_card` INTO head_name_val, head_id_card_val FROM `resident` WHERE `id` = head_id_val LIMIT 1;
    
    -- 生成户籍编号
    SET household_no_val = CONCAT('HJ', LPAD(100000 + i, 6, '0'));
    
    -- 生成地址
    SET address_val = CONCAT('北京市', CASE FLOOR(RAND() * 5) WHEN 0 THEN '朝阳区' WHEN 1 THEN '海淀区' WHEN 2 THEN '西城区' WHEN 3 THEN '东城区' ELSE '丰台区' END, CASE FLOOR(RAND() * 10) WHEN 0 THEN '第一街道' WHEN 1 THEN '第二街道' WHEN 2 THEN '第三街道' WHEN 3 THEN '第四街道' WHEN 4 THEN '第五街道' WHEN 5 THEN '第六街道' WHEN 6 THEN '第七街道' WHEN 7 THEN '第八街道' WHEN 8 THEN '第九街道' ELSE '第十街道' END, FLOOR(RAND() * 100) + 1, '号');
    
    -- 户别：1-家庭户, 2-集体户
    SET household_type_val = CASE WHEN RAND() > 0.1 THEN 1 ELSE 2 END;
    
    -- 户人数（1-5人）
    SET member_count_val = 1 + FLOOR(RAND() * 5);
    
    -- 迁入日期（2020-2024年）
    SET move_in_date_val = DATE_ADD('2020-01-01 00:00:00', INTERVAL FLOOR(RAND() * 1460) DAY);
    
    -- 状态：大部分正常，少数迁出
    SET status_val = CASE WHEN RAND() > 0.05 THEN 1 ELSE 0 END;
    
    INSERT INTO `household` (
      `head_id`, `head_name`, `head_id_card`, `household_no`, `address`,
      `household_type`, `member_count`, `contact_phone`, `move_in_date`,
      `move_in_reason`, `status`, `create_time`, `update_time`, `deleted`
    ) VALUES (
      head_id_val,
      head_name_val,
      head_id_card_val,
      household_no_val,
      address_val,
      household_type_val,
      member_count_val,
      CONCAT('138', LPAD(3000 + i, 8, '0')),
      move_in_date_val,
      CASE FLOOR(RAND() * 4)
        WHEN 0 THEN '工作调动'
        WHEN 1 THEN '购房迁入'
        WHEN 2 THEN '结婚迁入'
        ELSE '其他原因'
      END,
      status_val,
      DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY),
      DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY),
      0
    );
    
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;

CALL insert_households();
DROP PROCEDURE insert_households;

-- 插入户籍成员关系数据（约300条）
-- 使用INSERT INTO ... SELECT语句，为每个户籍生成成员关系
-- 首先插入户主（每个户籍的第一个成员）
INSERT INTO `household_member` (`household_id`, `resident_id`, `relationship`, `create_time`, `update_time`, `deleted`)
SELECT 
  h.`id` AS household_id,
  h.`head_id` AS resident_id,
  '户主' AS relationship,
  NOW() AS create_time,
  NOW() AS update_time,
  0 AS deleted
FROM `household` h
WHERE h.`deleted` = 0 
  AND h.`head_id` IS NOT NULL
  AND h.`member_count` > 0;

-- 为每个户籍生成其他成员（配偶、子女、父母等）
-- 通过数字表（1-5）与户籍表CROSS JOIN，为每个户籍生成对应数量的其他成员
INSERT INTO `household_member` (`household_id`, `resident_id`, `relationship`, `create_time`, `update_time`, `deleted`)
SELECT 
  h.`id` AS household_id,
  (1 + FLOOR(RAND() * 450)) AS resident_id,
  CASE FLOOR(RAND() * 5)
    WHEN 0 THEN '配偶'
    WHEN 1 THEN '子女'
    WHEN 2 THEN '父母'
    WHEN 3 THEN '其他'
    ELSE '子女'
  END AS relationship,
  DATE_ADD(NOW(), INTERVAL h.`id` * 10 + numbers.n SECOND) AS create_time,
  DATE_ADD(NOW(), INTERVAL h.`id` * 10 + numbers.n SECOND) AS update_time,
  0 AS deleted
FROM `household` h
CROSS JOIN (
  SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) numbers
WHERE h.`deleted` = 0 
  AND h.`member_count` > 1
  AND numbers.n <= (h.`member_count` - 1);

-- ============================================
-- 数据统计
-- ============================================
-- SELECT 'sys_user' AS table_name, COUNT(*) AS count FROM sys_user WHERE deleted = 0
-- UNION ALL
-- SELECT 'sys_admin', COUNT(*) FROM sys_admin WHERE deleted = 0
-- UNION ALL
-- SELECT 'resident', COUNT(*) FROM resident WHERE deleted = 0
-- UNION ALL
-- SELECT 'household', COUNT(*) FROM household WHERE deleted = 0
-- UNION ALL
-- SELECT 'household_member', COUNT(*) FROM household_member WHERE deleted = 0;
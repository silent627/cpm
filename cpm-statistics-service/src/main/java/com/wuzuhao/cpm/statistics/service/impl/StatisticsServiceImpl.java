package com.wuzuhao.cpm.statistics.service.impl;

import com.wuzuhao.cpm.common.Result;
import com.wuzuhao.cpm.statistics.feign.HouseholdServiceClient;
import com.wuzuhao.cpm.statistics.feign.ResidentServiceClient;
import com.wuzuhao.cpm.statistics.service.StatisticsService;
import com.wuzuhao.cpm.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    
    private static final String CACHE_PREFIX = "statistics:";
    private static final String CACHE_AGE_DISTRIBUTION = CACHE_PREFIX + "age-distribution";
    private static final String CACHE_GENDER = CACHE_PREFIX + "gender";
    private static final String CACHE_HOUSEHOLD_TYPE = CACHE_PREFIX + "household-type";
    private static final String CACHE_MOVE_TREND = CACHE_PREFIX + "move-trend:";
    private static final String CACHE_MONTHLY = CACHE_PREFIX + "monthly";
    private static final String CACHE_YEARLY = CACHE_PREFIX + "yearly";
    private static final long CACHE_EXPIRE_TIME = 1800; // 缓存过期时间：30分钟

    @Autowired
    private ResidentServiceClient residentServiceClient;

    @Autowired
    private HouseholdServiceClient householdServiceClient;

    @Autowired
    private RedisUtil redisUtil;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取所有居民列表（通过Feign调用）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getAllResidents() {
        try {
            Result<Object> result = residentServiceClient.getAllResidents();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                Object data = result.getData();
                if (data instanceof List) {
                    List<?> list = (List<?>) data;
                    List<Map<String, Object>> residents = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof Map) {
                            residents.add((Map<String, Object>) item);
                        }
                    }
                    log.info("成功获取居民列表，数量: {}", residents.size());
                    return residents;
                } else {
                    log.warn("居民服务返回的数据格式不正确: {}", data.getClass().getName());
                }
            } else {
                log.warn("居民服务调用失败，code: {}, message: {}", 
                        result != null ? result.getCode() : "null", 
                        result != null ? result.getMessage() : "result is null");
            }
        } catch (Exception e) {
            log.error("调用居民服务获取数据失败", e);
        }
        return new ArrayList<>();
    }

    /**
     * 获取所有户籍列表（通过Feign调用）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getAllHouseholds() {
        try {
            Result<Object> result = householdServiceClient.getAllHouseholds();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                Object data = result.getData();
                if (data instanceof List) {
                    List<?> list = (List<?>) data;
                    List<Map<String, Object>> households = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof Map) {
                            households.add((Map<String, Object>) item);
                        }
                    }
                    log.info("成功获取户籍列表，数量: {}", households.size());
                    return households;
                } else {
                    log.warn("户籍服务返回的数据格式不正确: {}", data.getClass().getName());
                }
            } else {
                log.warn("户籍服务调用失败，code: {}, message: {}", 
                        result != null ? result.getCode() : "null", 
                        result != null ? result.getMessage() : "result is null");
            }
        } catch (Exception e) {
            log.error("调用户籍服务获取数据失败", e);
        }
        return new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResidentAgeDistribution() {
        // 先从缓存获取
        Map<String, Object> cached = (Map<String, Object>) redisUtil.get(CACHE_AGE_DISTRIBUTION);
        if (cached != null) {
            return cached;
        }

        List<Map<String, Object>> residents = getAllResidents();
        
        // 年龄分组：0-18, 19-30, 31-45, 46-60, 60+
        Map<String, Integer> ageGroups = new LinkedHashMap<>();
        ageGroups.put("0-18岁", 0);
        ageGroups.put("19-30岁", 0);
        ageGroups.put("31-45岁", 0);
        ageGroups.put("46-60岁", 0);
        ageGroups.put("60岁以上", 0);
        
        LocalDate now = LocalDate.now();
        for (Map<String, Object> resident : residents) {
            Object birthDateObj = resident.get("birthDate");
            if (birthDateObj == null) continue;
            
            LocalDate birthDate;
            if (birthDateObj instanceof String) {
                birthDate = LocalDate.parse((String) birthDateObj);
            } else if (birthDateObj instanceof LocalDate) {
                birthDate = (LocalDate) birthDateObj;
            } else {
                continue;
            }
            
            int age = now.getYear() - birthDate.getYear();
            if (birthDate.getDayOfYear() > now.getDayOfYear()) {
                age--;
            }
            
            if (age <= 18) {
                ageGroups.put("0-18岁", ageGroups.get("0-18岁") + 1);
            } else if (age <= 30) {
                ageGroups.put("19-30岁", ageGroups.get("19-30岁") + 1);
            } else if (age <= 45) {
                ageGroups.put("31-45岁", ageGroups.get("31-45岁") + 1);
            } else if (age <= 60) {
                ageGroups.put("46-60岁", ageGroups.get("46-60岁") + 1);
            } else {
                ageGroups.put("60岁以上", ageGroups.get("60岁以上") + 1);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("categories", new ArrayList<>(ageGroups.keySet()));
        result.put("data", new ArrayList<>(ageGroups.values()));
        // 存入缓存
        redisUtil.set(CACHE_AGE_DISTRIBUTION, result, CACHE_EXPIRE_TIME);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResidentGenderStatistics() {
        // 先从缓存获取
        Map<String, Object> cached = (Map<String, Object>) redisUtil.get(CACHE_GENDER);
        if (cached != null) {
            return cached;
        }

        List<Map<String, Object>> residents = getAllResidents();
        
        long maleCount = residents.stream()
                .filter(r -> {
                    Object gender = r.get("gender");
                    return gender != null && (gender.equals(1) || gender.toString().equals("1"));
                })
                .count();
        long femaleCount = residents.size() - maleCount;
        
        Map<String, Object> result = new HashMap<>();
        result.put("male", (int) maleCount);
        result.put("female", (int) femaleCount);
        result.put("total", residents.size());
        // 存入缓存
        redisUtil.set(CACHE_GENDER, result, CACHE_EXPIRE_TIME);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getHouseholdTypeStatistics() {
        // 先从缓存获取
        Map<String, Object> cached = (Map<String, Object>) redisUtil.get(CACHE_HOUSEHOLD_TYPE);
        if (cached != null) {
            return cached;
        }

        List<Map<String, Object>> households = getAllHouseholds();
        
        long familyCount = households.stream()
                .filter(h -> {
                    Object type = h.get("householdType");
                    return type != null && (type.equals(1) || type.toString().equals("1"));
                })
                .count();
        long collectiveCount = households.size() - familyCount;
        
        Map<String, Object> result = new HashMap<>();
        result.put("family", (int) familyCount);
        result.put("collective", (int) collectiveCount);
        result.put("total", households.size());
        // 存入缓存
        redisUtil.set(CACHE_HOUSEHOLD_TYPE, result, CACHE_EXPIRE_TIME);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getHouseholdMoveTrend(String type) {
        String cacheKey = CACHE_MOVE_TREND + type;
        // 先从缓存获取
        Map<String, Object> cached = (Map<String, Object>) redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Map<String, Object>> households = getAllHouseholds();
        
        Map<String, Integer> moveInMap = new TreeMap<>();
        Map<String, Integer> moveOutMap = new TreeMap<>();
        
        DateTimeFormatter formatter = "year".equals(type) ? YEAR_FORMATTER : MONTH_FORMATTER;
        
        for (Map<String, Object> household : households) {
            Object moveInDateObj = household.get("moveInDate");
            if (moveInDateObj != null) {
                LocalDateTime moveInDate;
                if (moveInDateObj instanceof String) {
                    moveInDate = LocalDateTime.parse((String) moveInDateObj, DATE_TIME_FORMATTER);
                } else if (moveInDateObj instanceof LocalDateTime) {
                    moveInDate = (LocalDateTime) moveInDateObj;
                } else {
                    continue;
                }
                String key = moveInDate.format(formatter);
                moveInMap.put(key, moveInMap.getOrDefault(key, 0) + 1);
            }
            
            // 如果状态为迁出，记录迁出时间
            Object statusObj = household.get("status");
            if (statusObj != null && (statusObj.equals(0) || statusObj.toString().equals("0"))) {
                Object moveOutDateObj = household.get("moveOutDate");
                LocalDateTime moveOutDateTime = null;
                
                if (moveOutDateObj != null) {
                    if (moveOutDateObj instanceof String) {
                        moveOutDateTime = LocalDateTime.parse((String) moveOutDateObj, DATE_TIME_FORMATTER);
                    } else if (moveOutDateObj instanceof LocalDateTime) {
                        moveOutDateTime = (LocalDateTime) moveOutDateObj;
                    }
                } else {
                    Object updateTimeObj = household.get("updateTime");
                    if (updateTimeObj != null) {
                        if (updateTimeObj instanceof String) {
                            moveOutDateTime = LocalDateTime.parse((String) updateTimeObj, DATE_TIME_FORMATTER);
                        } else if (updateTimeObj instanceof LocalDateTime) {
                            moveOutDateTime = (LocalDateTime) updateTimeObj;
                        }
                    }
                }
                
                if (moveOutDateTime != null) {
                    String key = moveOutDateTime.format(formatter);
                    moveOutMap.put(key, moveOutMap.getOrDefault(key, 0) + 1);
                }
            }
        }
        
        // 合并所有时间点，确保数据对齐
        Set<String> allKeys = new TreeSet<>(moveInMap.keySet());
        allKeys.addAll(moveOutMap.keySet());
        
        List<String> categories = new ArrayList<>(allKeys);
        List<Integer> moveInList = new ArrayList<>();
        List<Integer> moveOutList = new ArrayList<>();
        
        for (String key : categories) {
            moveInList.add(moveInMap.getOrDefault(key, 0));
            moveOutList.add(moveOutMap.getOrDefault(key, 0));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("moveIn", moveInList);
        result.put("moveOut", moveOutList);
        // 存入缓存
        redisUtil.set(cacheKey, result, CACHE_EXPIRE_TIME);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMonthlyStatistics() {
        // 先从缓存获取
        Map<String, Object> cached = (Map<String, Object>) redisUtil.get(CACHE_MONTHLY);
        if (cached != null) {
            return cached;
        }

        List<Map<String, Object>> residents = getAllResidents();
        List<Map<String, Object>> households = getAllHouseholds();
        
        // 按月份统计新增数据
        Map<String, Map<String, Integer>> monthlyData = new TreeMap<>();
        
        for (Map<String, Object> resident : residents) {
            Object createTimeObj = resident.get("createTime");
            if (createTimeObj != null) {
                LocalDateTime createTime;
                if (createTimeObj instanceof String) {
                    createTime = LocalDateTime.parse((String) createTimeObj, DATE_TIME_FORMATTER);
                } else if (createTimeObj instanceof LocalDateTime) {
                    createTime = (LocalDateTime) createTimeObj;
                } else {
                    continue;
                }
                String month = createTime.format(MONTH_FORMATTER);
                monthlyData.putIfAbsent(month, new HashMap<>());
                monthlyData.get(month).put("resident", monthlyData.get(month).getOrDefault("resident", 0) + 1);
            }
        }
        
        for (Map<String, Object> household : households) {
            Object createTimeObj = household.get("createTime");
            if (createTimeObj != null) {
                LocalDateTime createTime;
                if (createTimeObj instanceof String) {
                    createTime = LocalDateTime.parse((String) createTimeObj, DATE_TIME_FORMATTER);
                } else if (createTimeObj instanceof LocalDateTime) {
                    createTime = (LocalDateTime) createTimeObj;
                } else {
                    continue;
                }
                String month = createTime.format(MONTH_FORMATTER);
                monthlyData.putIfAbsent(month, new HashMap<>());
                monthlyData.get(month).put("household", monthlyData.get(month).getOrDefault("household", 0) + 1);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("categories", new ArrayList<>(monthlyData.keySet()));
        result.put("resident", monthlyData.values().stream()
                .map(m -> m.getOrDefault("resident", 0))
                .collect(Collectors.toList()));
        result.put("household", monthlyData.values().stream()
                .map(m -> m.getOrDefault("household", 0))
                .collect(Collectors.toList()));
        // 存入缓存
        redisUtil.set(CACHE_MONTHLY, result, CACHE_EXPIRE_TIME);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getYearlyStatistics() {
        // 先从缓存获取
        Map<String, Object> cached = (Map<String, Object>) redisUtil.get(CACHE_YEARLY);
        if (cached != null) {
            return cached;
        }

        List<Map<String, Object>> residents = getAllResidents();
        List<Map<String, Object>> households = getAllHouseholds();
        
        // 按年份统计新增数据
        Map<String, Map<String, Integer>> yearlyData = new TreeMap<>();
        
        for (Map<String, Object> resident : residents) {
            Object createTimeObj = resident.get("createTime");
            if (createTimeObj != null) {
                LocalDateTime createTime;
                if (createTimeObj instanceof String) {
                    createTime = LocalDateTime.parse((String) createTimeObj, DATE_TIME_FORMATTER);
                } else if (createTimeObj instanceof LocalDateTime) {
                    createTime = (LocalDateTime) createTimeObj;
                } else {
                    continue;
                }
                String year = createTime.format(YEAR_FORMATTER);
                yearlyData.putIfAbsent(year, new HashMap<>());
                yearlyData.get(year).put("resident", yearlyData.get(year).getOrDefault("resident", 0) + 1);
            }
        }
        
        for (Map<String, Object> household : households) {
            Object createTimeObj = household.get("createTime");
            if (createTimeObj != null) {
                LocalDateTime createTime;
                if (createTimeObj instanceof String) {
                    createTime = LocalDateTime.parse((String) createTimeObj, DATE_TIME_FORMATTER);
                } else if (createTimeObj instanceof LocalDateTime) {
                    createTime = (LocalDateTime) createTimeObj;
                } else {
                    continue;
                }
                String year = createTime.format(YEAR_FORMATTER);
                yearlyData.putIfAbsent(year, new HashMap<>());
                yearlyData.get(year).put("household", yearlyData.get(year).getOrDefault("household", 0) + 1);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("categories", new ArrayList<>(yearlyData.keySet()));
        result.put("resident", yearlyData.values().stream()
                .map(m -> m.getOrDefault("resident", 0))
                .collect(Collectors.toList()));
        result.put("household", yearlyData.values().stream()
                .map(m -> m.getOrDefault("household", 0))
                .collect(Collectors.toList()));
        // 存入缓存
        redisUtil.set(CACHE_YEARLY, result, CACHE_EXPIRE_TIME);
        return result;
    }

    /**
     * 清除所有统计缓存（在数据更新时调用）
     */
    public void clearStatisticsCache() {
        redisUtil.deleteByPattern(CACHE_PREFIX + "*");
    }
}


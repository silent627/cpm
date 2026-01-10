package com.wuzuhao.cpm.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 提供统一的缓存操作方法
 */
@Component
public class RedisUtil {

    @Autowired
    @NonNull
    private RedisTemplate<String, Object> redisTemplate;

    // =============================common============================
    /**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间(秒)
     */
    public boolean expire(@NonNull String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(@NonNull String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(@NonNull String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key 可以传一个值 或多个
     */
    public void del(@NonNull String... key) {
        if (key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                java.util.HashSet<String> keySet = new java.util.HashSet<>();
                for (String k : key) {
                    keySet.add(k);
                }
                redisTemplate.delete(keySet);
            }
        }
    }

    // ============================String=============================
    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    @Nullable
    public Object get(@NonNull String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值，如果为null则删除该键
     * @return true成功 false失败
     */
    public boolean set(@NonNull String key, @Nullable Object value) {
        try {
            if (value == null) {
                redisTemplate.delete(key);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值，如果为null则删除该键
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(@NonNull String key, @Nullable Object value, long time) {
        try {
            if (value == null) {
                redisTemplate.delete(key);
            } else if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     * @param key 键
     * @param delta 要增加几(大于0)
     */
    public long incr(@NonNull String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0L;
    }

    /**
     * 递减
     * @param key 键
     * @param delta 要减少几(小于0)
     */
    public long decr(@NonNull String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        Long result = redisTemplate.opsForValue().increment(key, -delta);
        return result != null ? result : 0L;
    }

    // ================================Hash=================================
    /**
     * HashGet
     * @param key 键 不能为null
     * @param item 项 不能为null
     */
    @Nullable
    public Object hget(@NonNull String key, @NonNull String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值，如果为null则删除该项
     * @return true 成功 false失败
     */
    public boolean hset(@NonNull String key, @NonNull String item, @Nullable Object value) {
        try {
            if (value == null) {
                redisTemplate.opsForHash().delete(key, item);
            } else {
                redisTemplate.opsForHash().put(key, item, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值，如果为null则删除该项
     * @param time 时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(@NonNull String key, @NonNull String item, @Nullable Object value, long time) {
        try {
            if (value == null) {
                redisTemplate.opsForHash().delete(key, item);
            } else {
                redisTemplate.opsForHash().put(key, item, value);
                if (time > 0) {
                    expire(key, time);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key 键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(@NonNull String key, @NonNull Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    // ============================Set=============================
    /**
     * 根据key获取Set中的所有值
     * @param key 键
     */
    @Nullable
    public Set<Object> sGet(@NonNull String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     * @param key 键
     * @param value 值，不能为null
     * @return true 存在 false不存在
     */
    public boolean sHasKey(@NonNull String key, @NonNull Object value) {
        try {
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            return result != null && result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(@NonNull String key, @NonNull Object... values) {
        try {
            Long result = redisTemplate.opsForSet().add(key, values);
            return result != null ? result : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     * @param key 键
     * @param time 时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(@NonNull String key, long time, @NonNull Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     * @param key 键
     */
    public long sGetSetSize(@NonNull String key) {
        try {
            Long result = redisTemplate.opsForSet().size(key);
            return result != null ? result : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     * @param key 键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(@NonNull String key, @NonNull Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据模式删除key
     * @param pattern 模式，如 "resident:*"
     */
    public void deleteByPattern(@NonNull String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


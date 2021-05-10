-- 解锁脚本
 -- KEYS[1]表示key
 -- KEYS[2]表示value
 -- return -1 表示未能获取到key或者key的值与传入的值不相等
 if redis.call("get",KEYS[1]) == KEYS[2] then
     return redis.call("del",KEYS[1])
 else
     return -1
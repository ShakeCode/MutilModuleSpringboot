-- 加锁脚本，其中KEYS[]为外部传入参数
 -- KEYS[1]表示key
 -- KEYS[2]表示value
 -- KEYS[3]表示过期时间
 if redis.call("setnx", KEYS[1], KEYS[2]) == 1 then
     return redis.call("pexpire", KEYS[1], KEYS[3])
 else
     return 0
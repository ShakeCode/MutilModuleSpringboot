// 事务性操作
if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then
   redis.call('expire',KEYS[1],ARGV[2])
else
   return 0
end;

// 说明：先获取指定key的值，然后和传入的arg比较是否相等，相等值删除key，否则直接返回0
if redis.call("get",KEYS[1]) == ARGV[1] then
    return redis.call("del",KEYS[1])
else
    return 0
end
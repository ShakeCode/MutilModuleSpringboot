 if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then
    redis.call('expire',KEYS[1],ARGV[2])
 else
    return 0
 end;

 // java代码：
 String lua_scripts = "if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then" +
            " redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end";
Object result = jedis.eval(lua_scripts, Collections.singletonList(key_resource_id), Collections.singletonList(values));
// 判断是否成功
return result.equals(1L);
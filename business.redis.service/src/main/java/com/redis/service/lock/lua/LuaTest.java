package com.redis.service.lock.lua;

/**
 * https://www.cnblogs.com/better-farther-world2099/articles/12208398.html
 *
 * 1、背景
 * 有时候，我们需要一次性操作多个 Redis 命令，但是 这样的多个操作不具备原子性，而且 Redis 的事务也不够强大，不支持事务的回滚，还无法实现命令之间的逻辑关系计算。所以，一般在开发中，我们会利用 lua 脚本来实现 Redis 的事务。
 *
 * 2、lua 脚本
 * Redis 中使用 lua 脚本，我们需要注意的是，从 Redis 2.6.0后才支持 lua 脚本的执行。
 * [使用 lua 脚本的好处]：
 *
 * 原子操作：lua脚本是作为一个整体执行的，所以中间不会被其他命令插入。
 * 减少网络开销：可以将多个请求通过脚本的形式一次发送，减少网络时延。
 * 复用性：lua脚本可以常驻在redis内存中，所以在使用的时候，可以直接拿来复用，也减少了代码量。
 *
 * 3、Redis 中执行 lua 脚本
 * 1、命令格式：
 *
 * EVAL script numkeys key [key ...] arg [arg ...]
 * 说明：
 *
 * script是第一个参数，为Lua 5.1脚本(字符串)。
 * 第二个参数numkeys指定后续参数有几个key。
 * key [key ...]，被操作的key，可以多个，在lua脚本中通过KEYS[1], KEYS[2]获取
 * arg [arg ...]，参数，可以多个，在lua脚本中通过ARGV[1], ARGV[2]获取。
 *
 * 2、如果直接使用 redis-cli命令：
 *
 * redis-cli --eval lua_file key1 key2 , arg1 arg2 arg3
 * 说明：
 *
 * eval 命令后不再是 lua 脚本的字符串形式，而是一个 lua 脚本文件。后缀为.lua
 * 不再需要numkeys参数，而是用 , 隔开多个key和多个arg
 *
 * The type Lua test.
 */
public class LuaTest {
    
}

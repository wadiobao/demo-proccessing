package com.example.demo.utils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class TestRedis {
    public static void connectBasic() {
        RedisURI uri = RedisURI.Builder
                .redis("redis-16288.c241.us-east-1-4.ec2.cloud.redislabs.com", 16288)
                .withAuthentication("default", "7AqBov91NYgoc4SxnIrFyl3S0Jb7BiVh")
                .build();
        RedisClient client = RedisClient.create(uri);
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisCommands<String, String> commands = connection.sync();

        commands.set("foo", "bar");
        String result = commands.get("foo");
        System.out.println(result); // >>> bar

        connection.close();

        client.shutdown();
    }
    
    public static void main(String[] args) {
		connectBasic();
	}
}

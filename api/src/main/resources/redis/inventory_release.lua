redis.call("INCRBY", KEYS[1], ARGV[1])
      return 1
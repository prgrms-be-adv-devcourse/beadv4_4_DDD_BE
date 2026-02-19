for i=1,#KEYS do
          local available = tonumber(redis.call("GET", KEYS[i]) or "0")
          local requestQty = tonumber(ARGV[i])
          if available < requestQty then
              return -1
          end
      end
      for i=1,#KEYS do
          redis.call("DECRBY", KEYS[i], ARGV[i])
      end
      return 1
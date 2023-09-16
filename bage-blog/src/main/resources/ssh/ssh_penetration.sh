#!/bin/bash

# 设置远程服务器信息
REMOTE_HOST="region-42.seetacloud.com"
REMOTE_PORT="16528"
LOCAL_PORT="6111"
PASSWORD="iWvwjee5rf" # 设置密码

# 创建一个交互式 expect 脚本
expect -c "
spawn ssh -CNg -L $LOCAL_PORT:127.0.0.1:$LOCAL_PORT root@$REMOTE_HOST -p $REMOTE_PORT
expect \"password:\"
send \"$PASSWORD\\r\"
interact
"


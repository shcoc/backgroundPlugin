# 需要全部重写，代码好乱
# backgroundPlugin
IDEA插件
#### 自己使用,未完成全部，本地背景图片切换已完成
设置位置 
1. File/setting/Apperance/Apperance/background
2. View/backgroundSetting

#### 原计划
1. 本地文件夹
2. 本地数据库(随缘修,有bug，已经禁用)
3. 远程数据库(同上)

#### 数据库格式
```sql
CREATE TABLE IF NOT EXISTS image(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    path VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    type INTEGER NOT NULL default 0
)

```

path 路径
url 本地或者网络
type  -1 未知 0 本地 1 网络

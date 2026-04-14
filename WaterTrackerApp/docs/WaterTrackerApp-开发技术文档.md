# WaterTrackerApp 开发技术文档

## 1. 项目简介

### 1.1 项目名称
WaterTrackerApp 喝水健康记录系统

### 1.2 开发目标
基于 Android 平台开发一款本地喝水记录应用，实现饮水记录、每日目标设置、进度统计、今日明细、历史查询和达标特效展示等功能。

### 1.3 开发环境
- 开发工具：Android Studio
- 开发语言：Kotlin
- 界面实现：XML + Activity
- 最低支持版本：Android 7.0（minSdk 24）
- 数据存储：Room 数据库 + SharedPreferences
- UI 组件：Material Design、RecyclerView、NestedScrollView

## 2. 系统功能模块设计

WaterTrackerApp 主要分为 4 个功能模块。

### 2.1 饮水记录模块

#### 功能说明
- 快捷记录 `+250ml`
- 快捷记录 `+500ml`
- 自定义毫升数记录

#### 实现说明
- 通过按钮和输入框快速创建饮水记录
- 每次记录都写入 Room 数据库
- 页面数据自动刷新

#### 涉及文件
- `app/src/main/java/com/example/watertrackerapp/MainActivity.kt`
- `app/src/main/java/com/example/watertrackerapp/data/WaterRecord.kt`
- `app/src/main/java/com/example/watertrackerapp/data/WaterDao.kt`
- `app/src/main/res/layout/activity_main.xml`

### 2.2 目标设置与统计模块

#### 功能说明
- 用户可设置每日饮水目标
- 展示今日饮水总量
- 展示今日记录次数
- 展示达标状态与进度条

#### 实现说明
- 使用 SharedPreferences 保存目标值
- 根据今日总量和目标值动态计算进度
- 页面实时显示已达成或剩余饮水量

#### 涉及文件
- `app/src/main/java/com/example/watertrackerapp/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

### 2.3 今日明细与历史查询模块

#### 功能说明
- 展示今日每次饮水明细
- 展示历史按天汇总记录

#### 实现说明
- 今日明细通过 Room 查询当天所有记录并按时间倒序排列
- 历史记录通过 SQL 聚合查询按日期汇总
- 分别使用两个 RecyclerView 展示

#### 涉及文件
- `app/src/main/java/com/example/watertrackerapp/data/WaterDao.kt`
- `app/src/main/java/com/example/watertrackerapp/ui/TodayRecordAdapter.kt`
- `app/src/main/java/com/example/watertrackerapp/ui/WaterSummaryAdapter.kt`
- `app/src/main/res/layout/item_today_record.xml`
- `app/src/main/res/layout/item_water_summary.xml`

### 2.4 达标反馈特效模块

#### 功能说明
- 当天第一次达到目标时触发庆祝动画
- 动画包含粒子爆发、扩散光环和达标文字

#### 实现说明
- 自定义 View 绘制粒子动画
- 使用 SharedPreferences 记录当天是否已播放
- 仅在首次达标时播放，避免重复触发

#### 涉及文件
- `app/src/main/java/com/example/watertrackerapp/ui/GoalCelebrationView.kt`
- `app/src/main/java/com/example/watertrackerapp/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

## 3. 系统架构设计

### 3.1 架构分层
本项目采用简洁分层结构：

1. `ui` 层
- 负责页面展示、列表适配和特效视图

2. `data` 层
- 负责 Room 实体、数据库和数据访问接口

3. `config` 思想
- 使用 SharedPreferences 管理目标值和达标状态

### 3.2 分层职责

#### UI 层
- 处理按钮点击和输入交互
- 展示统计信息、进度条、明细和历史列表
- 播放达标庆祝动画

#### Data 层
- 保存饮水记录
- 查询今日数据和历史汇总数据

#### 配置层
- 保存每日目标
- 保存当天是否已触发特效

## 4. 数据库设计

### 4.1 数据表设计
表名：`water_records`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | Int | 主键，自增 |
| date | String | 日期 |
| amountMl | Int | 饮水量 |
| createdAt | Long | 记录时间戳 |

### 4.2 实体设计
实体类：`WaterRecord`

说明：
- 使用 `@Entity(tableName = "water_records")` 标记
- 使用 `@PrimaryKey(autoGenerate = true)` 定义主键

## 5. 接口设计

### 5.1 数据访问接口设计

`WaterDao` 中定义的主要接口如下：

#### 新增饮水记录
```kotlin
suspend fun insert(record: WaterRecord)
```

#### 获取历史按天汇总
```kotlin
suspend fun getDailySummaries(): List<DailyWaterSummary>
```

#### 获取今日总饮水量
```kotlin
suspend fun getTodayTotal(date: String): Int
```

#### 获取今日记录次数
```kotlin
suspend fun getTodayRecordCount(date: String): Int
```

#### 获取今日所有记录
```kotlin
suspend fun getTodayRecords(date: String): List<WaterRecord>
```

### 5.2 主业务控制接口设计

`MainActivity` 中主要业务方法如下：

#### 添加饮水记录
```kotlin
private fun addWaterRecord(amountMl: Int)
```

#### 刷新页面数据
```kotlin
private fun refreshData()
```

#### 加载并显示数据
```kotlin
private suspend fun loadDataOnMain()
```

#### 获取当前目标值
```kotlin
private fun currentGoal(): Int
```

#### 判断并播放达标特效
```kotlin
private fun maybePlayGoalCelebration(date: String, todayTotal: Int, goal: Int)
```

## 6. 页面与交互实现设计

### 6.1 主页面设计
主页面包含：
- 顶部标题栏
- 今日统计卡片
- 快捷记录按钮
- 自定义饮水输入区
- 每日目标设置区
- 今日明细列表
- 历史记录列表
- 达标动画覆盖层

### 6.2 今日明细列表设计
每条明细项包含：
- 本次饮水量
- 记录时间

### 6.3 历史列表设计
每条历史项包含：
- 日期
- 当日总饮水量
- 当日记录次数

## 7. 核心业务流程

### 7.1 快捷记录流程
1. 用户点击 `+250ml` 或 `+500ml`
2. 系统写入一条饮水记录
3. 刷新今日总量、进度条和明细列表

### 7.2 自定义记录流程
1. 用户输入毫升数
2. 点击添加按钮
3. 系统校验数值是否合法
4. 保存记录并刷新界面

### 7.3 目标设置流程
1. 用户输入新的每日目标
2. 点击保存目标
3. 系统写入本地配置
4. 刷新进度和状态信息

### 7.4 达标特效流程
1. 页面刷新时计算今日总量与目标值
2. 若今日首次达到目标
3. 记录当天已庆祝状态
4. 播放粒子庆祝动画

### 7.5 历史查询流程
1. 系统读取数据库数据
2. 按日期聚合
3. 在历史区域展示汇总结果

## 8. 关键技术说明

### 8.1 Room 数据库
用于保存所有饮水记录，便于后续进行统计和历史展示。

### 8.2 SharedPreferences
用于保存每日目标和达标特效触发状态，适合轻量级配置数据存储。

### 8.3 RecyclerView
用于展示今日明细和历史汇总数据。

### 8.4 NestedScrollView
用于解决页面内容较多时的滚动和键盘遮挡问题。

### 8.5 自定义 View 动画
通过自定义 `GoalCelebrationView` 绘制粒子动画，实现更炫酷的达标反馈效果。

## 9. 测试说明

### 9.1 功能测试项
- `+250ml`、`+500ml` 记录是否成功
- 自定义毫升数记录是否成功
- 输入非法数值时是否有提示
- 修改每日目标后是否立即生效
- 今日进度条是否正确更新
- 今日明细是否按时间显示
- 历史记录是否按天汇总
- 达成目标时是否触发庆祝动画
- 同一天特效是否只触发一次
- 输入目标时页面是否不会被键盘遮挡

### 9.2 测试结果
当前版本已完成核心功能联调，在真机环境下可正常完成饮水记录、目标设置、进度展示、历史查看和达标动画展示。

## 10. 可扩展方向

- 增加定时喝水提醒
- 增加按周或按月统计图表
- 增加不同杯型快捷选择
- 增加震动与音效反馈
- 增加健康建议提示

## 11. 总结

WaterTrackerApp 使用 Kotlin、XML、Room、SharedPreferences、RecyclerView 和自定义动画 View 等技术，实现了一个功能完整、交互友好、视觉效果较强的健康记录应用。项目结构清晰，功能模块明确，适合作为课程作业项目进行展示、答辩和文档撰写。

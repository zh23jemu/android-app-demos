# PomodoroApp 开发技术文档

## 1. 项目简介

### 1.1 项目名称
PomodoroApp 番茄钟学习计时系统

### 1.2 开发目标
基于 Android 平台开发一款番茄钟学习计时应用，实现专注计时、休息计时、自定义时长设置、周期跳过、学习记录保存、统计展示以及提醒通知等功能。

### 1.3 开发环境
- 开发工具：Android Studio
- 开发语言：Kotlin
- 界面实现：XML + Activity
- 最低支持版本：Android 7.0（minSdk 24）
- 数据存储：SharedPreferences
- UI 组件：Material Design、RecyclerView

## 2. 系统功能模块设计

PomodoroApp 主要分为 4 个功能模块。

### 2.1 计时控制模块

#### 功能说明
- 开始计时
- 暂停计时
- 重置计时
- 跳过当前周期
- 自动切换学习阶段和休息阶段

#### 实现说明
- 使用 `CountDownTimer` 实现倒计时
- 使用布尔变量记录当前是学习阶段还是休息阶段
- 使用 `remainingMillis` 保存当前剩余时间
- 点击跳过后直接切换到下一个阶段

#### 涉及文件
- `app/src/main/java/com/example/pomodoroapp/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

### 2.2 参数设置模块

#### 功能说明
- 用户可自定义专注时间
- 用户可自定义休息时间

#### 实现说明
- 在主页面提供两个数字输入框
- 读取用户输入并限制在合理范围内
- 专注时间范围为 1 至 180 分钟
- 休息时间范围为 1 至 60 分钟
- 重置或开始时应用当前设置值

#### 涉及文件
- `app/src/main/java/com/example/pomodoroapp/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

### 2.3 打卡记录与统计模块

#### 功能说明
- 每完成一次专注周期自动记录一次打卡
- 展示今日完成次数
- 展示今日学习时长
- 展示累计完成次数
- 展示按天汇总的历史记录

#### 实现说明
- 使用 `SharedPreferences` 保存专注记录
- 使用 JSON 数组形式持久化多条记录
- 按日期聚合生成历史统计结果
- 使用 RecyclerView 展示历史列表

#### 涉及文件
- `app/src/main/java/com/example/pomodoroapp/data/SessionModels.kt`
- `app/src/main/java/com/example/pomodoroapp/data/SessionStorage.kt`
- `app/src/main/java/com/example/pomodoroapp/ui/HistoryAdapter.kt`
- `app/src/main/java/com/example/pomodoroapp/MainActivity.kt`
- `app/src/main/res/layout/item_history.xml`

### 2.4 提醒通知模块

#### 功能说明
- 周期结束时弹出通知
- 周期结束时播放声音提醒

#### 实现说明
- 使用通知渠道创建系统通知
- 使用 `NotificationCompat` 发送提醒
- 使用 `ToneGenerator` 播放提示音

#### 涉及文件
- `app/src/main/java/com/example/pomodoroapp/MainActivity.kt`
- `app/src/main/AndroidManifest.xml`

## 3. 系统架构设计

### 3.1 架构分层
本项目采用简洁分层结构：

1. `ui` 层
- 负责页面展示与交互处理

2. `data` 层
- 负责记录模型定义与本地持久化

### 3.2 分层职责

#### UI 层
- 显示当前阶段、状态和剩余时间
- 响应开始、暂停、重置、跳过等操作
- 展示统计卡片和历史记录列表

#### Data 层
- 保存专注完成记录
- 聚合每日统计数据
- 向 UI 层返回可直接展示的数据结构

## 4. 数据设计

### 4.1 专注记录结构

`SessionRecord` 字段如下：

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| timestamp | Long | 完成时间戳 |
| durationMinutes | Int | 本次专注分钟数 |

### 4.2 每日统计结构

`DailySummary` 字段如下：

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| date | String | 日期 |
| completedCount | Int | 完成次数 |
| totalMinutes | Int | 当日累计专注时长 |

## 5. 接口设计

### 5.1 本地存储接口设计

`SessionStorage` 中封装的主要方法如下：

#### 保存一次专注记录
```kotlin
fun saveStudySession(durationMinutes: Int)
```

#### 获取按天汇总的历史统计
```kotlin
fun getDailySummaries(): List<DailySummary>
```

### 5.2 主业务控制接口设计

`MainActivity` 中主要业务方法如下：

#### 开始计时
```kotlin
private fun startTimer()
```

#### 暂停计时
```kotlin
private fun pauseTimer()
```

#### 重置计时
```kotlin
private fun resetTimer()
```

#### 跳过当前阶段
```kotlin
private fun skipCurrentPhase()
```

#### 应用自定义时间
```kotlin
private fun applyCustomDurations()
```

#### 获取当前阶段对应时长
```kotlin
private fun currentPhaseDurationMs(): Long
```

#### 播放提醒音
```kotlin
private fun playReminderTone()
```

## 6. 页面与交互实现设计

### 6.1 主页面设计
主页面包含：
- 顶部标题栏
- 当前阶段和状态文本
- 倒计时显示区域
- 专注时间与休息时间输入框
- 开始、暂停、重置、跳过按钮
- 今日统计卡片
- 累计统计卡片
- 历史记录列表

### 6.2 历史列表设计
每条历史记录项包含：
- 日期
- 当日完成次数
- 当日累计时长

### 6.3 提醒反馈设计
- 周期结束后发送通知
- 同时播放简短提示音
- 自动切换到下一阶段

## 7. 核心业务流程

### 7.1 开始计时流程
1. 用户输入专注时间和休息时间
2. 点击开始按钮
3. 调用 `applyCustomDurations()`
4. 创建倒计时对象并开始计时

### 7.2 完成专注周期流程
1. 倒计时结束
2. 如果当前为学习阶段，则保存一条记录
3. 发送通知并播放提示音
4. 自动切换到休息阶段
5. 刷新统计信息和历史记录

### 7.3 跳过当前周期流程
1. 用户点击跳过按钮
2. 系统取消当前计时
3. 自动切换到下一阶段
4. 刷新界面显示

### 7.4 统计展示流程
1. 页面启动或计时完成后读取记录
2. 调用 `getDailySummaries()`
3. 计算今日完成次数和学习时长
4. 刷新列表与卡片显示

## 8. 关键技术说明

### 8.1 CountDownTimer
用于实现秒级倒计时，适合番茄钟类应用的核心时间控制逻辑。

### 8.2 SharedPreferences
用于轻量级本地数据持久化，适合保存专注记录和历史统计所需数据。

### 8.3 RecyclerView
用于展示按天汇总的历史学习记录。

### 8.4 NotificationCompat
用于创建兼容性更好的系统通知提醒。

### 8.5 ToneGenerator
用于在周期结束时播放提示音，增强反馈效果。

## 9. 测试说明

### 9.1 功能测试项
- 开始计时是否正常
- 暂停后是否保留剩余时间
- 重置后是否恢复默认专注阶段
- 跳过功能是否正确切换阶段
- 自定义专注和休息时间是否生效
- 完成专注周期后是否生成记录
- 今日统计和历史记录是否正确
- 周期结束时是否有通知和声音提醒

### 9.2 测试结果
当前版本已完成核心功能联调，应用可在真机环境下进行番茄钟计时、历史记录查看和提醒测试。

## 10. 可扩展方向

- 增加循环轮数设置
- 增加长休息模式
- 增加震动提醒
- 增加主题切换
- 增加图表统计展示

## 11. 总结

PomodoroApp 使用 Kotlin、XML、CountDownTimer、SharedPreferences、RecyclerView 和系统通知等技术，实现了一个完整的番茄钟学习计时应用。项目结构清晰，核心模块明确，既满足课程作业需求，也具备较好的实用性和展示效果。

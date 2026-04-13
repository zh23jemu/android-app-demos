# TodoApp 开发技术文档

## 1. 项目简介

### 1.1 项目名称
TodoApp 待办事项管理系统

### 1.2 开发目标
基于 Android 平台开发一款本地待办事项管理应用，实现任务管理、数据持久化、筛选查询和统计展示等功能。

### 1.3 开发环境
- 开发工具：Android Studio
- 开发语言：Kotlin
- 界面实现：XML + Activity
- 最低支持版本：Android 7.0（minSdk 24）
- 数据存储：Room 数据库
- UI 组件：Material Design、RecyclerView

## 2. 系统功能模块设计

TodoApp 主要分为 4 个功能模块。

### 2.1 任务管理模块

#### 功能说明
- 新增任务
- 编辑任务
- 删除任务
- 修改任务完成状态

#### 实现说明
- 通过 `FloatingActionButton` 触发新增任务对话框
- 通过列表项中的编辑按钮触发编辑任务对话框
- 通过列表项中的删除按钮删除任务
- 通过复选框切换任务完成状态

#### 涉及文件
- `app/src/main/java/com/example/todoapp/MainActivity.kt`
- `app/src/main/java/com/example/todoapp/ui/TaskAdapter.kt`
- `app/src/main/res/layout/dialog_add_task.xml`
- `app/src/main/res/layout/item_task.xml`

### 2.2 数据存储模块

#### 功能说明
- 使用 Room 数据库存储任务数据
- 支持应用退出后数据保留

#### 实现说明
- `Task` 作为实体类映射数据库表
- `TaskDao` 提供增删改查方法
- `TodoDatabase` 负责数据库实例创建
- `TaskRepository` 对数据访问进行封装

#### 涉及文件
- `app/src/main/java/com/example/todoapp/data/Task.kt`
- `app/src/main/java/com/example/todoapp/data/TaskDao.kt`
- `app/src/main/java/com/example/todoapp/data/TodoDatabase.kt`
- `app/src/main/java/com/example/todoapp/data/TaskRepository.kt`

### 2.3 筛选查询模块

#### 功能说明
- 按任务状态筛选
- 按任务分类筛选
- 支持组合筛选

#### 实现说明
- 状态筛选包括：全部、未完成、已完成
- 分类筛选包括：全部、学习、生活
- 用户点击筛选按钮后，读取当前下拉框条件并刷新列表

#### 涉及文件
- `app/src/main/java/com/example/todoapp/MainActivity.kt`
- `app/src/main/java/com/example/todoapp/data/TaskRepository.kt`
- `app/src/main/res/layout/activity_main.xml`

### 2.4 统计展示模块

#### 功能说明
- 显示总任务数
- 显示已完成任务数
- 显示未完成任务数

#### 实现说明
- 在 `TaskRepository` 中统计任务汇总信息
- 在主界面顶部以卡片形式展示统计数据
- 每次新增、编辑、删除、勾选完成后同步更新统计结果

#### 涉及文件
- `app/src/main/java/com/example/todoapp/data/TaskRepository.kt`
- `app/src/main/java/com/example/todoapp/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

## 3. 系统架构设计

### 3.1 架构分层
本项目采用简洁分层结构：

1. `ui` 层
- 负责页面展示与交互处理

2. `data` 层
- 负责数据实体、数据库访问与仓库封装

3. `model` 思想
- 以任务实体为中心组织业务数据

### 3.2 分层职责

#### UI 层
- 展示列表、对话框、筛选框、统计卡片
- 响应用户点击事件
- 调用 Repository 获取或更新数据

#### Data 层
- 管理 Room 数据库
- 提供任务增删改查接口
- 返回筛选结果与统计结果

## 4. 数据库设计

### 4.1 数据表设计
表名：`tasks`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | Int | 主键，自增 |
| title | String | 任务标题 |
| content | String | 任务内容 |
| category | String | 任务分类 |
| isCompleted | Boolean | 是否完成 |
| createdAt | Long | 创建时间戳 |

### 4.2 实体设计
实体类：`Task`

说明：
- 使用 `@Entity(tableName = "tasks")` 标记
- 使用 `@PrimaryKey(autoGenerate = true)` 定义主键

## 5. 接口设计

### 5.1 数据访问接口设计

`TaskDao` 中定义的主要接口如下：

#### 查询全部任务
```kotlin
suspend fun getAllTasks(): List<Task>
```

#### 查询未完成任务
```kotlin
suspend fun getActiveTasks(): List<Task>
```

#### 查询已完成任务
```kotlin
suspend fun getCompletedTasks(): List<Task>
```

#### 新增任务
```kotlin
suspend fun insert(task: Task)
```

#### 更新任务
```kotlin
suspend fun update(task: Task)
```

#### 删除任务
```kotlin
suspend fun delete(task: Task)
```

### 5.2 仓库层接口设计

`TaskRepository` 中封装的主要方法如下：

#### 获取筛选后的任务列表
```kotlin
suspend fun getTasks(
    statusFilter: TaskFilter,
    categoryFilter: TaskCategoryFilter
): List<Task>
```

#### 新增任务
```kotlin
suspend fun addTask(title: String, content: String, category: String)
```

#### 更新任务
```kotlin
suspend fun updateTask(task: Task)
```

#### 删除任务
```kotlin
suspend fun deleteTask(task: Task)
```

#### 获取统计信息
```kotlin
suspend fun getSummary(): TaskSummary
```

## 6. 页面与交互实现设计

### 6.1 主页面设计
主页面包含：
- 顶部标题栏
- 双筛选下拉框
- 筛选按钮
- 统计信息卡片
- 任务列表
- 浮动添加按钮

### 6.2 列表项设计
每个任务项包含：
- 完成复选框
- 标题
- 内容
- 分类与时间信息
- 编辑按钮
- 删除按钮

### 6.3 对话框设计
新增和编辑任务共用同一个对话框布局，减少重复开发。

## 7. 核心业务流程

### 7.1 新增任务流程
1. 用户点击新增按钮
2. 弹出输入对话框
3. 输入标题、内容、分类
4. 调用 `repository.addTask()`
5. 数据写入数据库
6. 刷新列表与统计信息

### 7.2 编辑任务流程
1. 用户点击编辑按钮
2. 弹出对话框并回填原值
3. 修改后点击保存
4. 调用 `repository.updateTask()`
5. 数据更新后刷新页面

### 7.3 删除任务流程
1. 用户点击删除按钮
2. 调用 `repository.deleteTask()`
3. 刷新列表与统计信息

### 7.4 筛选任务流程
1. 用户选择状态和分类条件
2. 点击筛选按钮
3. 调用 `repository.getTasks()`
4. 刷新任务列表

### 7.5 统计更新流程
1. 页面加载或数据发生变化
2. 调用 `repository.getSummary()`
3. 刷新总数、已完成数、未完成数

## 8. 关键技术说明

### 8.1 Room 数据库
使用 Room 简化 SQLite 操作，优点如下：
- 代码结构清晰
- 注解开发效率高
- 便于后续扩展

### 8.2 RecyclerView
使用 RecyclerView 展示任务列表，适合动态更新和大批量数据展示。

### 8.3 ViewBinding
使用 ViewBinding 替代 `findViewById`，提升代码安全性和可读性。

### 8.4 Kotlin 协程
使用协程在 IO 线程中处理数据库操作，避免阻塞主线程。

## 9. 测试说明

### 9.1 功能测试项
- 新增任务是否成功
- 编辑任务是否成功
- 删除任务是否成功
- 勾选完成状态是否成功
- 状态筛选是否正确
- 分类筛选是否正确
- 统计数据是否正确
- 应用重启后数据是否保留

### 9.2 测试结果
当前版本已完成基础功能联调，核心功能可在真机环境下正常运行。

## 10. 可扩展方向

- 增加任务提醒功能
- 增加截止日期字段
- 增加搜索功能
- 增加更细粒度分类
- 增加深色模式与主题切换

## 11. 总结

TodoApp 使用 Kotlin、XML、Room、RecyclerView 等 Android 常用技术实现了一个完整的本地任务管理应用。项目结构清晰，功能模块明确，既满足课程作业对核心模块与技术实现的要求，也具备良好的扩展性与演示价值。

# Java 转 Kotlin 批量转换工具使用说明

## 📋 概述

本项目提供了三个工具来帮助将 Java 代码批量转换为 Kotlin：

1. **convert_to_kotlin.py** - Python 脚本，用于分析和生成转换指南
2. **convert_to_kotlin.sh** - Shell 脚本，基础版本
3. **batch_convert.sh** - 交互式批量转换脚本（推荐）

## 🚀 快速开始

### 方法 1: 使用交互式脚本（推荐）

```bash
./batch_convert.sh
```

脚本会：
- 自动检测所有 Java 文件
- 显示统计信息
- 提供交互式菜单选择转换方式
- 可以自动打开 Android Studio

### 方法 2: 使用 Python 脚本分析

```bash
# 分析所有模块
python3 convert_to_kotlin.py

# 分析特定模块
python3 convert_to_kotlin.py selector
```

### 方法 3: 使用 Shell 脚本

```bash
# 分析所有模块
./convert_to_kotlin.sh

# 分析特定模块
./convert_to_kotlin.sh selector
```

## 📊 当前统计

根据最新分析，项目中共有 **337 个 Java 文件**需要转换：

| 模块 | 文件数 | 优先级 |
|------|--------|--------|
| compress | 20 | ⭐ 1 (最简单) |
| ijkplayer-java | 28 | ⭐ 2 |
| app | 29 | ⭐ 3 |
| camerax | 30 | ⭐ 4 |
| ucrop | 41 | ⭐ 5 |
| selector | 189 | ⭐ 6 (最复杂) |

## 🔧 转换步骤

### 推荐转换顺序

1. **compress** (20 个文件) - 最简单的模块，先测试转换流程
2. **ijkplayer-java** (28 个文件) - 第三方库，相对独立
3. **app** (29 个文件) - Demo 应用，不影响核心功能
4. **camerax** (30 个文件) - 相机模块
5. **ucrop** (41 个文件) - 裁剪模块
6. **selector** (189 个文件) - 核心模块，最后转换

### 使用 Android Studio 转换

#### 单个文件转换

1. 在 Android Studio 中打开项目
2. 找到要转换的 Java 文件
3. 右键点击文件
4. 选择 `Code` > `Convert Java File to Kotlin File`
5. 或使用快捷键：
   - **Mac**: `Cmd + Option + Shift + K`
   - **Windows/Linux**: `Ctrl + Alt + Shift + K`

#### 批量转换目录

1. 在 Project 视图中选择整个包或目录
2. 右键选择 `Code` > `Convert Java File to Kotlin File`
3. Android Studio 会自动转换目录下所有 Java 文件

#### 按模块转换示例

以 `compress` 模块为例：

1. 在 Project 视图中展开 `compress/src/main/java/`
2. 选择整个 `top/zibin/luban` 包
3. 右键选择 `Code` > `Convert Java File to Kotlin File`
4. 等待转换完成
5. 立即编译测试：`./gradlew :compress:build`

## ✅ 转换后检查清单

每个模块转换完成后，请执行以下检查：

- [ ] **编译检查**: `./gradlew :模块名:build`
- [ ] **代码风格**: 检查 Kotlin 代码是否符合规范
- [ ] **功能测试**: 运行相关功能测试
- [ ] **性能检查**: 确保转换后性能没有下降
- [ ] **文档更新**: 更新相关 API 文档

## 📝 生成的文件

运行脚本后会生成以下文件：

- **CONVERT_GUIDE.md** - 详细的转换指南，包含所有文件列表
- **java_files_list.txt** - 所有 Java 文件的完整路径列表

## ⚠️ 注意事项

1. **备份代码**: 转换前建议先提交当前代码到 Git
2. **逐个模块**: 不要一次性转换所有文件，按模块逐个转换
3. **及时测试**: 每个模块转换后立即编译和测试
4. **代码审查**: 转换后的代码可能需要手动优化
5. **API 兼容**: 确保转换后的 Kotlin 代码保持与 Java 的互操作性（使用 `@JvmStatic` 等注解）

## 🐛 常见问题

### Q: 转换后编译错误怎么办？

A: 
1. 检查是否有语法错误
2. 检查导入语句是否正确
3. 检查是否需要添加 `@JvmStatic` 等注解
4. 查看 Android Studio 的错误提示进行修复

### Q: 如何保持与 Java 代码的兼容性？

A: 对于需要被 Java 代码调用的 Kotlin 代码，使用：
- `@JvmStatic` - 静态方法
- `@JvmName` - 重命名
- `@JvmOverloads` - 重载方法

### Q: 转换后性能会下降吗？

A: 通常不会，Kotlin 编译后的字节码与 Java 类似。但建议：
- 运行性能测试
- 检查是否有不必要的对象创建
- 优化 Kotlin 特有的语法糖

## 📚 参考资源

- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)
- [Java 转 Kotlin 指南](https://kotlinlang.org/docs/java-to-kotlin-interop.html)
- [Android Kotlin 指南](https://developer.android.com/kotlin)

## 🎯 转换进度跟踪

建议创建一个进度跟踪文件，记录转换进度：

```markdown
## 转换进度

- [x] compress (20/20) - 2024-XX-XX
- [ ] ijkplayer-java (0/28)
- [ ] app (0/29)
- [ ] camerax (0/30)
- [ ] ucrop (0/41)
- [ ] selector (0/189)

总计: 20/337 (5.9%)
```

---

**祝转换顺利！** 🎉


#!/bin/bash

# Java 转 Kotlin 批量转换脚本
# 使用方法: ./convert_to_kotlin.sh [模块名]
# 如果不指定模块名，将转换所有模块

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查 Android Studio 是否安装
ANDROID_STUDIO_PATH=""
if [ -d "/Applications/Android Studio.app" ]; then
    ANDROID_STUDIO_PATH="/Applications/Android Studio.app"
elif [ -d "$HOME/Applications/Android Studio.app" ]; then
    ANDROID_STUDIO_PATH="$HOME/Applications/Android Studio.app"
else
    echo -e "${RED}错误: 未找到 Android Studio，请手动安装或修改脚本中的路径${NC}"
    exit 1
fi

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo -e "${GREEN}项目根目录: $PROJECT_ROOT${NC}"

# 查找所有 Java 文件
find_java_files() {
    local module=$1
    if [ -z "$module" ]; then
        find "$PROJECT_ROOT" -name "*.java" -type f | grep -v "/build/" | grep -v "/.gradle/"
    else
        find "$PROJECT_ROOT/$module" -name "*.java" -type f | grep -v "/build/"
    fi
}

# 统计 Java 文件数量
count_java_files() {
    local module=$1
    find_java_files "$module" | wc -l | tr -d ' '
}

# 显示需要转换的文件列表
show_files() {
    local module=$1
    echo -e "${YELLOW}找到以下 Java 文件需要转换:${NC}"
    find_java_files "$module" | while read -r file; do
        echo "  - $file"
    done
}

# 主函数
main() {
    local module=$1
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Java 转 Kotlin 批量转换工具${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    if [ -n "$module" ]; then
        if [ ! -d "$PROJECT_ROOT/$module" ]; then
            echo -e "${RED}错误: 模块 '$module' 不存在${NC}"
            exit 1
        fi
        echo -e "${YELLOW}目标模块: $module${NC}"
    else
        echo -e "${YELLOW}目标: 所有模块${NC}"
    fi
    
    local file_count=$(count_java_files "$module")
    echo -e "${YELLOW}找到 $file_count 个 Java 文件${NC}"
    echo ""
    
    if [ "$file_count" -eq 0 ]; then
        echo -e "${GREEN}没有找到需要转换的 Java 文件${NC}"
        exit 0
    fi
    
    # 显示文件列表
    show_files "$module"
    echo ""
    
    echo -e "${YELLOW}注意: 此脚本会生成转换指南${NC}"
    echo -e "${YELLOW}实际转换需要使用 Android Studio 的自动转换功能${NC}"
    echo ""
    
    # 生成转换指南文件
    local guide_file="$PROJECT_ROOT/CONVERT_GUIDE.md"
    cat > "$guide_file" << EOF
# Java 转 Kotlin 转换指南

## 文件统计
- 总文件数: $file_count
- 模块: ${module:-"所有模块"}

## 转换方法

### 方法 1: 使用 Android Studio GUI（推荐）

1. 打开 Android Studio
2. 打开项目: $PROJECT_ROOT
3. 在 Project 视图中，选择要转换的文件或目录
4. 右键点击，选择 \`Code\` > \`Convert Java File to Kotlin File\`
5. 或使用快捷键:
   - Windows/Linux: \`Ctrl+Alt+Shift+K\`
   - Mac: \`Cmd+Option+Shift+K\`

### 方法 2: 批量转换单个目录

1. 在 Project 视图中选择整个包或目录
2. 右键选择 \`Code\` > \`Convert Java File to Kotlin File\`
3. Android Studio 会转换目录下所有 Java 文件

### 方法 3: 使用命令行（需要配置）

如果 Android Studio 命令行工具已配置，可以使用:

\`\`\`bash
# 转换单个文件
studio convert-to-kotlin <file_path>

# 批量转换（需要编写脚本调用）
\`\`\`

## 需要转换的文件列表

EOF
    
    find_java_files "$module" | while read -r file; do
        echo "- \`$file\`" >> "$guide_file"
    done
    
    echo ""
    echo -e "${GREEN}转换指南已生成: $guide_file${NC}"
    echo ""
    echo -e "${GREEN}下一步操作:${NC}"
    echo "1. 打开 Android Studio"
    echo "2. 打开项目: $PROJECT_ROOT"
    echo "3. 按照 $guide_file 中的指南进行转换"
    echo ""
    echo -e "${YELLOW}提示: 建议按模块逐个转换，转换后立即测试编译${NC}"
}

# 执行主函数
main "$@"


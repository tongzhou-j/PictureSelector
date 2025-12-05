#!/bin/bash

# 批量转换 Java 文件为 Kotlin 的自动化脚本
# 此脚本使用 Android Studio 的命令行工具进行转换
# 注意: 需要先配置 Android Studio 的命令行工具

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_FILES_LIST="$PROJECT_ROOT/java_files_list.txt"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Java 转 Kotlin 批量转换脚本${NC}"
echo -e "${GREEN}========================================${NC}\n"

# 检查文件列表是否存在
if [ ! -f "$JAVA_FILES_LIST" ]; then
    echo -e "${YELLOW}生成文件列表...${NC}"
    python3 "$PROJECT_ROOT/convert_to_kotlin.py" > /dev/null
fi

# 统计文件数量
TOTAL_FILES=$(wc -l < "$JAVA_FILES_LIST" | tr -d ' ')
echo -e "${BLUE}找到 $TOTAL_FILES 个 Java 文件需要转换${NC}\n"

# 检查 Android Studio 是否安装
if command -v studio &> /dev/null; then
    STUDIO_CMD="studio"
elif [ -d "/Applications/Android Studio.app" ]; then
    STUDIO_CMD="/Applications/Android Studio.app/Contents/MacOS/studio"
else
    echo -e "${RED}错误: 未找到 Android Studio 命令行工具${NC}"
    echo -e "${YELLOW}请先配置 Android Studio 命令行工具:${NC}"
    echo "1. 打开 Android Studio"
    echo "2. Tools > Create Command-line Launcher"
    echo "3. 或手动添加到 PATH"
    exit 1
fi

echo -e "${YELLOW}注意: 批量自动转换需要 Android Studio 的命令行工具支持${NC}"
echo -e "${YELLOW}如果命令行工具不可用，请使用 Android Studio GUI 进行转换${NC}\n"

# 提供交互式选择
echo -e "${BLUE}请选择转换方式:${NC}"
echo "1) 使用 Android Studio GUI（推荐，最可靠）"
echo "2) 尝试使用命令行工具（需要配置）"
echo "3) 仅显示文件列表"
read -p "请输入选项 (1-3): " choice

case $choice in
    1)
        echo -e "\n${GREEN}使用 Android Studio GUI 转换:${NC}"
        echo "1. 打开 Android Studio"
        echo "2. 打开项目: $PROJECT_ROOT"
        echo "3. 在 Project 视图中，选择要转换的文件或目录"
        echo "4. 右键点击，选择 Code > Convert Java File to Kotlin File"
        echo "5. 或使用快捷键: Cmd+Option+Shift+K (Mac) 或 Ctrl+Alt+Shift+K (Windows/Linux)"
        echo ""
        echo -e "${YELLOW}建议转换顺序（按文件数量从少到多）:${NC}"
        echo "1. compress (20 个文件)"
        echo "2. ijkplayer-java (28 个文件)"
        echo "3. app (29 个文件)"
        echo "4. camerax (30 个文件)"
        echo "5. ucrop (41 个文件)"
        echo "6. selector (189 个文件)"
        echo ""
        echo -e "${GREEN}文件列表已保存在: $JAVA_FILES_LIST${NC}"
        echo -e "${GREEN}详细指南请查看: $PROJECT_ROOT/CONVERT_GUIDE.md${NC}"
        
        # 尝试打开 Android Studio
        if [ -d "/Applications/Android Studio.app" ]; then
            read -p "是否现在打开 Android Studio? (y/n): " open_studio
            if [ "$open_studio" = "y" ] || [ "$open_studio" = "Y" ]; then
                open -a "Android Studio" "$PROJECT_ROOT"
            fi
        fi
        ;;
    2)
        echo -e "\n${YELLOW}尝试使用命令行工具转换...${NC}"
        echo -e "${RED}警告: 命令行转换可能不稳定，建议使用 GUI 方式${NC}\n"
        
        # 这里可以添加实际的命令行转换逻辑
        # 但 Android Studio 的命令行工具对批量转换支持有限
        echo "Android Studio 命令行工具对批量转换支持有限"
        echo "建议使用 GUI 方式进行转换"
        ;;
    3)
        echo -e "\n${BLUE}文件列表:${NC}\n"
        cat "$JAVA_FILES_LIST" | head -20
        if [ "$TOTAL_FILES" -gt 20 ]; then
            echo -e "\n${YELLOW}... (共 $TOTAL_FILES 个文件，仅显示前 20 个)${NC}"
            echo -e "${BLUE}完整列表请查看: $JAVA_FILES_LIST${NC}"
        fi
        ;;
    *)
        echo -e "${RED}无效选项${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}转换完成后，请:${NC}"
echo "1. 编译项目确保没有错误"
echo "2. 运行测试确保功能正常"
echo "3. 检查代码风格"


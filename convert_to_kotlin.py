#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Java 转 Kotlin 批量转换辅助脚本
此脚本可以帮助识别和准备需要转换的 Java 文件
实际转换需要使用 Android Studio 的自动转换功能
"""

import os
import sys
from pathlib import Path
from typing import List, Tuple

# 颜色输出
class Colors:
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    RED = '\033[0;31m'
    BLUE = '\033[0;34m'
    NC = '\033[0m'  # No Color

def find_java_files(project_root: Path, module: str = None) -> List[Path]:
    """查找所有 Java 文件"""
    java_files = []
    search_path = project_root / module if module else project_root
    
    if not search_path.exists():
        print(f"{Colors.RED}错误: 路径不存在: {search_path}{Colors.NC}")
        return []
    
    for java_file in search_path.rglob("*.java"):
        # 排除 build 目录和 .gradle 目录
        if "build" not in java_file.parts and ".gradle" not in java_file.parts:
            java_files.append(java_file)
    
    return sorted(java_files)

def get_module_name(file_path: Path, project_root: Path) -> str:
    """获取文件所属的模块名"""
    parts = file_path.parts
    try:
        root_index = parts.index(project_root.name)
        if root_index + 1 < len(parts):
            return parts[root_index + 1]
    except ValueError:
        pass
    return "unknown"

def analyze_files(java_files: List[Path], project_root: Path) -> dict:
    """分析文件统计信息"""
    stats = {
        "total": len(java_files),
        "by_module": {},
        "by_package": {}
    }
    
    for java_file in java_files:
        module = get_module_name(java_file, project_root)
        if module not in stats["by_module"]:
            stats["by_module"][module] = 0
        stats["by_module"][module] += 1
        
        # 获取包名
        try:
            with open(java_file, 'r', encoding='utf-8') as f:
                for line in f:
                    if line.strip().startswith('package '):
                        package = line.strip().replace('package ', '').replace(';', '')
                        if package not in stats["by_package"]:
                            stats["by_package"][package] = 0
                        stats["by_package"][package] += 1
                        break
        except Exception:
            pass
    
    return stats

def generate_conversion_guide(java_files: List[Path], project_root: Path, output_file: Path):
    """生成转换指南文件"""
    stats = analyze_files(java_files, project_root)
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("# Java 转 Kotlin 转换指南\n\n")
        f.write("## 文件统计\n\n")
        f.write(f"- **总文件数**: {stats['total']}\n")
        f.write(f"- **模块数**: {len(stats['by_module'])}\n\n")
        
        f.write("### 按模块统计\n\n")
        for module, count in sorted(stats['by_module'].items()):
            f.write(f"- `{module}`: {count} 个文件\n")
        f.write("\n")
        
        f.write("## 转换方法\n\n")
        f.write("### 方法 1: 使用 Android Studio GUI（推荐）\n\n")
        f.write("1. 打开 Android Studio\n")
        f.write(f"2. 打开项目: `{project_root}`\n")
        f.write("3. 在 Project 视图中，选择要转换的文件或目录\n")
        f.write("4. 右键点击，选择 `Code` > `Convert Java File to Kotlin File`\n")
        f.write("5. 或使用快捷键:\n")
        f.write("   - Windows/Linux: `Ctrl+Alt+Shift+K`\n")
        f.write("   - Mac: `Cmd+Option+Shift+K`\n\n")
        
        f.write("### 方法 2: 批量转换单个目录\n\n")
        f.write("1. 在 Project 视图中选择整个包或目录\n")
        f.write("2. 右键选择 `Code` > `Convert Java File to Kotlin File`\n")
        f.write("3. Android Studio 会转换目录下所有 Java 文件\n\n")
        
        f.write("### 方法 3: 按模块转换（推荐顺序）\n\n")
        # 按文件数量排序，建议先转换文件少的模块
        modules_sorted = sorted(stats['by_module'].items(), key=lambda x: x[1])
        for i, (module, count) in enumerate(modules_sorted, 1):
            f.write(f"{i}. **{module}** ({count} 个文件)\n")
            module_files = [f for f in java_files if get_module_name(f, project_root) == module]
            f.write(f"   - 路径: `{project_root / module}/src/main/java/`\n")
            f.write(f"   - 建议: 先转换工具类，再转换业务类\n\n")
        
        f.write("## 需要转换的文件列表\n\n")
        f.write("### 按模块分组\n\n")
        
        current_module = None
        for java_file in java_files:
            module = get_module_name(java_file, project_root)
            if module != current_module:
                if current_module is not None:
                    f.write("\n")
                f.write(f"#### {module}\n\n")
                current_module = module
            
            rel_path = java_file.relative_to(project_root)
            f.write(f"- `{rel_path}`\n")
        
        f.write("\n## 转换后检查清单\n\n")
        f.write("- [ ] 编译项目，确保没有语法错误\n")
        f.write("- [ ] 检查 Kotlin 代码风格（使用 ktlint 或 detekt）\n")
        f.write("- [ ] 运行单元测试\n")
        f.write("- [ ] 运行应用，测试核心功能\n")
        f.write("- [ ] 检查性能是否有影响\n")
        f.write("- [ ] 更新相关文档\n")

def main():
    project_root = Path(__file__).parent.resolve()
    module = sys.argv[1] if len(sys.argv) > 1 else None
    
    print(f"{Colors.GREEN}{'='*50}{Colors.NC}")
    print(f"{Colors.GREEN}Java 转 Kotlin 批量转换工具{Colors.NC}")
    print(f"{Colors.GREEN}{'='*50}{Colors.NC}\n")
    
    print(f"{Colors.BLUE}项目根目录: {project_root}{Colors.NC}")
    if module:
        print(f"{Colors.YELLOW}目标模块: {module}{Colors.NC}")
    else:
        print(f"{Colors.YELLOW}目标: 所有模块{Colors.NC}")
    print()
    
    # 查找 Java 文件
    java_files = find_java_files(project_root, module)
    
    if not java_files:
        print(f"{Colors.GREEN}没有找到需要转换的 Java 文件{Colors.NC}")
        return
    
    print(f"{Colors.YELLOW}找到 {len(java_files)} 个 Java 文件{Colors.NC}\n")
    
    # 分析统计
    stats = analyze_files(java_files, project_root)
    print(f"{Colors.BLUE}按模块统计:{Colors.NC}")
    for module_name, count in sorted(stats['by_module'].items()):
        print(f"  - {module_name}: {count} 个文件")
    print()
    
    # 生成转换指南
    guide_file = project_root / "CONVERT_GUIDE.md"
    generate_conversion_guide(java_files, project_root, guide_file)
    print(f"{Colors.GREEN}转换指南已生成: {guide_file}{Colors.NC}\n")
    
    # 生成文件列表（用于脚本处理）
    list_file = project_root / "java_files_list.txt"
    with open(list_file, 'w', encoding='utf-8') as f:
        for java_file in java_files:
            f.write(f"{java_file}\n")
    print(f"{Colors.GREEN}文件列表已保存: {list_file}{Colors.NC}\n")
    
    print(f"{Colors.GREEN}下一步操作:{Colors.NC}")
    print("1. 打开 Android Studio")
    print(f"2. 打开项目: {project_root}")
    print(f"3. 查看转换指南: {guide_file}")
    print("4. 按照指南逐个模块进行转换")
    print()
    print(f"{Colors.YELLOW}提示: 建议按模块逐个转换，转换后立即测试编译{Colors.NC}")

if __name__ == "__main__":
    main()


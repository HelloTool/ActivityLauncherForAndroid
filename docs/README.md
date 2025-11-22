# 活动启动器

一个 Android 活动 (Activity) 启动器应用，允许用户浏览并启动设备上安装的应用程序及其活动。

本应用只是一个活动启动器。如需其他功能，请使用 [Anywhere-](https://github.com/zhaobozhen/Anywhere-) 等产品。

## 特性

- **兼容 Android 2.3**
- **打开 URI（包括意图 (Intent) URI）**
- 浏览设备上安装的所有应用程序以及它们的活动。
- 支持大屏设备，适配了平行视界、多窗口。

## 计划（不一定能实现，不一定将实现）

- [ ] ~~支持使用 Root、Shizuku 打开活动~~
- [ ] 添加高级启动，允许自定义意图参数
- [ ] 支持 Android 2.3 的图标缓存
- [ ] 抛弃 AsyncTask
- [ ] 允许只显示已导出的活动
- [ ] 允许只显示有活动的应用
- [ ] 标注需要权限的活动
- [ ] 标注并置顶入口活动
- [ ] 复制意图 URI
- [X] 启动应用

## 此应用绝对不会

- 抛弃 Android 2.3 的兼容
- 使用 AppCompat、Material Components、Compose、Kotlin 协程等不支持 Android 2.3 的工具
- 支持使用 Root、Shizuku 打开活动
- 添加启动服务、发送广播功能
- 添加历史记录、收藏功能

## 使用

1. 打开应用后，主界面会显示设备上安装的所有应用程序。
2. 点击任意应用图标或名称，可以查看该应用的活动列表。
3. 在活动列表页面，可以选择并启动特定的活动。
4. 使用搜索功能快速找到所需的应用或活动。
5. 利用排序和过滤选项来更好地组织应用列表。

## 贡献

欢迎贡献代码和提出建议！请通过提交 Pull Request 或 Issue 来参与改进项目。

## 许可证

本应用的代码采用 Apache 2.0 许可证，详情请查看 [LICENSE](./LICENSE) 文件。

```txt
Copyright 2025 Jesse205

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 开源声明

本项目遵循开源软件的相关法律规定，所有第三方库均遵守其各自的许可证。更多详情请参阅 [os-notices.md](./legal/os-notices.md) 文件。

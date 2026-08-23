# 资源页 `ElOption value` 类型警告

## 问题现象

控制台报错：

```text
Invalid prop: type check failed for prop "value". Expected String | Number | Boolean | Object, got Null
```

日志指向 `ResourceList` 页面。

## 根因分析

资源页文件类型筛选下拉框中，`全部` 选项原来写成了：

```vue
<el-option label="全部" :value="null" />
```

但是 Element Plus 的 `ElOption` 不接受 `null` 作为 `value`，因此触发类型警告。

## 影响范围

- 页面功能基本还能使用
- 但控制台会持续报警
- 后续如果版本升级，兼容性风险会增大

## 处理方式

前端将 `null` 改为 `''`：

```vue
<el-option label="全部" value="" />
```

同时把筛选表单中的默认值、重置值也同步改为 `''`。

## 修复位置

- [src/views/Resource/ResourceList.vue](/C:/Users/lenovo/Desktop/qianduan-campus/src/views/Resource/ResourceList.vue:18)

## 结论

这是一个前端组件类型使用问题，不是后端问题。

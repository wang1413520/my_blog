# 项目上传到 GitHub 操作指南

适用仓库：

- GitHub 仓库地址：`https://github.com/wang1413520/myBlog`
- 远程仓库 HTTPS 地址：`https://github.com/wang1413520/myBlog.git`

这份文档按“**一步一步照着做**”来写，尽量不讲太多原理。

---

## 一、你现在的状态

你当前项目已经满足下面这些条件：

1. 已经登录了 VS Code 的 GitHub 账号
2. 已经在 GitHub 上创建了仓库 `myBlog`
3. 本地项目已经初始化了 Git
4. 当前分支是 `main`

你现在差的主要是：

1. 确认哪些文件不要上传
2. 把本地仓库连接到 GitHub 仓库
3. 提交本地代码
4. 推送到 GitHub

---

## 二、先做最重要的检查

## 1. 不建议直接上传的文件

你当前项目里，有几个文件建议先确认：

- `.env`
- `.env.production`
- `dist.zip`

原因：

1. `.env` 和 `.env.production` 里可能有接口地址、账号配置、密钥
2. `dist.zip` 一般是打包产物，不建议放源码仓库

如果你**不确定能不能传**，那就先**不要上传**。

---

## 三、推荐的安全做法

先在终端里执行下面 3 条命令，把这些文件从“准备上传列表”里拿掉：

```bash
git restore --staged .env
git restore --staged .env.production
git restore --staged dist.zip
```

执行完后，再看一次状态：

```bash
git status
```

如果你看到这 3 个文件不在绿色的“待提交列表”里了，就说明处理成功。

---

## 四、建议顺手修改 `.gitignore`

打开项目根目录下的 `.gitignore`，在文件最后面补上这些内容：

```gitignore
.env
.env.production
dist.zip
```

这样以后它们就不会被误传。

如果你还想更稳一点，也可以保留：

```gitignore
node_modules/
dist/
```

---

## 五、正式上传到 GitHub

下面这些步骤最关键。

## 第 1 步：给本地仓库绑定远程 GitHub 仓库

在项目根目录终端执行：

```bash
git remote add origin https://github.com/wang1413520/myBlog.git
```

执行后检查一下有没有绑定成功：

```bash
git remote -v
```

如果成功，你会看到类似：

```bash
origin  https://github.com/wang1413520/myBlog.git (fetch)
origin  https://github.com/wang1413520/myBlog.git (push)
```

---

## 第 2 步：提交本地代码

先把你要上传的代码加入提交：

```bash
git add .
```

然后提交：

```bash
git commit -m "init: first commit"
```

如果提交成功，会看到类似“`files changed`”这样的提示。

---

## 第 3 步：推送到 GitHub

执行：

```bash
git push -u origin main
```

第一次推送成功后，后面你再提交代码，只需要：

```bash
git add .
git commit -m "你的提交说明"
git push
```

---

## 六、如果推送时报错怎么办

## 情况 1：提示远程仓库不是空的

这通常说明你在 GitHub 创建仓库时，勾选了：

- `README`
- `.gitignore`
- `LICENSE`

如果报类似这种错误：

```bash
rejected
failed to push some refs
```

那就执行：

```bash
git pull --rebase origin main
git push -u origin main
```

如果中间没有冲突，一般就能成功。

---

## 情况 2：提示 `remote origin already exists`

说明你之前已经加过远程仓库了。

这时不要再 `add`，改用：

```bash
git remote set-url origin https://github.com/wang1413520/myBlog.git
```

然后再推送：

```bash
git push -u origin main
```

---

## 情况 3：推送很慢或者超时

这时再考虑开梯子。

优先顺序：

1. 先重试一次
2. 再检查 GitHub 网页能不能打开
3. 如果网页都很慢，再开梯子

---

## 七、最短操作版本

如果你已经确认：

1. `.env` 不上传
2. 仓库是你刚创建的
3. 本地项目就在当前目录

那你可以直接按下面顺序执行：

```bash
git restore --staged .env
git restore --staged .env.production
git restore --staged dist.zip
git remote add origin https://github.com/wang1413520/myBlog.git
git add .
git commit -m "init: first commit"
git push -u origin main
```

---

## 八、以后更新代码怎么继续上传

以后你改完代码，只要重复下面 3 步：

```bash
git add .
git commit -m "feat: 更新了个人中心页面"
git push
```

提交说明可以参考：

```bash
feat: 新增功能
fix: 修复问题
style: 调整样式
docs: 更新文档
refactor: 重构代码
```

---

## 九、如果你更喜欢用 VS Code 图形界面

也可以不用全靠命令。

操作顺序：

1. 点左侧“源代码管理”
2. 检查变更文件列表
3. 在输入框里写提交说明，比如 `init: first commit`
4. 点击“提交”
5. 点击“发布分支”或“同步更改”

但因为你现在是第一次上传，我更推荐你先走一次命令行，最稳。

---

## 十、你现在最推荐的实际操作顺序

就按下面来：

1. 先把 `.env`、`.env.production`、`dist.zip` 从暂存区移出去
2. 修改 `.gitignore`
3. 执行 `git remote add origin https://github.com/wang1413520/myBlog.git`
4. 执行 `git add .`
5. 执行 `git commit -m "init: first commit"`
6. 执行 `git push -u origin main`

---

## 十一、复制即用命令区

```bash
git restore --staged .env
git restore --staged .env.production
git restore --staged dist.zip
git remote add origin https://github.com/wang1413520/myBlog.git
git add .
git commit -m "init: first commit"
git push -u origin main
```

如果推送时报“远程仓库不是空的”，改用：

```bash
git pull --rebase origin main
git push -u origin main
```

---

## 十二、一句话总结

你现在其实已经快完成了，核心就是：

1. 先别把 `.env` 这种敏感文件传上去
2. 绑定远程仓库 `myBlog`
3. 提交
4. 推送

照着这份文档一步一步来就行。

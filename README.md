# 4 位数字验证码生成器（Java）

一个零依赖的 4 位数字验证码小程序：生成、校验、有效期和错误次数限制，附控制台演示。

## 环境要求

- JDK 8 或更高版本（开发与测试环境为 JDK 1.8.0_202）
- 不需要任何第三方依赖

## 项目结构

| 文件 | 说明 |
| --- | --- |
| `VerificationCode.java` | 主程序：生成、校验、控制台演示 |
| `run.bat` | Windows 一键编译并运行 |

## 运行

### Windows 双击运行

双击 `run.bat`。脚本里的 `JAVA_HOME` 指向本机 JDK 路径，换环境时改这一行即可：

```bat
set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202"
```

### 命令行运行

```bash
javac -encoding UTF-8 VerificationCode.java
java VerificationCode
```

如果 `javac` 不在 PATH 里（只装了 JRE），用完整路径编译：

```bash
"C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe" -encoding UTF-8 VerificationCode.java
```

## 主要 API

```java
VerificationCode.generate();                 // "7391"，首位不为 0（1000 ~ 9999）
VerificationCode.generateWithLeadingZero();  // 允许 0 开头（0000 ~ 9999）
VerificationCode.generateBatch(5);           // 一次生成 5 个
VerificationCode.verify(input, code);        // true / false，常量时间比较

VerificationCode.CodeSession session = new VerificationCode.CodeSession();
session.getCode();              // 下发给用户的验证码
session.verify("7391");         // OK / WRONG / EXPIRED / LOCKED
session.getAttemptsLeft();      // 剩余尝试次数
session.getRemainingMillis();   // 剩余有效毫秒数
```

`CodeSession` 默认有效期 60 秒、最多输错 3 次，第三次错误后返回 `LOCKED`，超时返回 `EXPIRED`。

## 设计要点

- 使用 `SecureRandom` 而不是 `Random`：`Random` 的种子可由时间推测，生成的验证码容易被猜出。
- 校验使用 `MessageDigest.isEqual` 做常量时间比较，避免根据比较耗时逐位爆破验证码。
- `CodeSession` 把「过期时间」和「错误次数」绑定在同一个会话对象上，避免把校验逻辑散落到调用处。

## 输出示例

```
===== 4 位数字验证码演示 =====
已生成验证码：1092（有效期 60 秒，最多可输错 3 次）

1. 重新生成一个 4 位验证码
2. 输入验证码进行校验
3. 批量生成 5 个验证码
0. 退出
请选择：
```

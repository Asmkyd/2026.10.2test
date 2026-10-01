import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;
import java.util.Scanner;

/**
 * 4 位数字验证码工具：生成、校验、过期时间与尝试次数限制。
 *
 * 编译：javac -encoding UTF-8 VerificationCode.java
 * 运行：java VerificationCode
 */
public class VerificationCode {

    /** 验证码位数 */
    public static final int LENGTH = 4;

    /** 默认有效期：60 秒 */
    public static final int DEFAULT_EXPIRE_SECONDS = 60;

    /** 默认最多允许输错 3 次 */
    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * 用 SecureRandom 而不是 Random：验证码属于安全场景，
     * Random 的种子可由时间推测，生成的验证码容易被猜出。
     */
    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    private VerificationCode() {
    }

    // ==================== 生成 ====================

    /** 生成一个 4 位数字验证码，首位不为 0，取值范围 1000 ~ 9999。 */
    public static String generate() {
        return generate(SECURE_RANDOM);
    }

    /** 可传入自定义随机源的版本，方便测试时固定结果。 */
    public static String generate(Random random) {
        StringBuilder sb = new StringBuilder(LENGTH);
        sb.append(random.nextInt(9) + 1);            // 首位：1 ~ 9
        for (int i = 1; i < LENGTH; i++) {
            sb.append(random.nextInt(10));           // 其余位：0 ~ 9
        }
        return sb.toString();
    }

    /** 生成允许 0 开头的 4 位验证码，取值范围 0000 ~ 9999。 */
    public static String generateWithLeadingZero() {
        return generateWithLeadingZero(SECURE_RANDOM);
    }

    /** 允许 0 开头的版本，可传入自定义随机源。 */
    public static String generateWithLeadingZero(Random random) {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /** 一次性生成 count 个验证码，元素之间可能有重复，用于批量展示或测试。 */
    public static String[] generateBatch(int count) {
        if (count <= 0) {
            return new String[0];
        }
        String[] codes = new String[count];
        for (int i = 0; i < count; i++) {
            codes[i] = generate();
        }
        return codes;
    }

    // ==================== 校验 ====================

    /**
     * 使用常量时间比较，避免根据比较耗时逐位猜出验证码。
     * 普通的 String.equals 一旦发现不同字符就会立刻返回。
     */
    public static boolean verify(String input, String expected) {
        if (input == null || expected == null) {
            return false;
        }
        byte[] a = input.trim().getBytes(StandardCharsets.UTF_8);
        byte[] b = expected.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    // ==================== 会话（带过期时间 + 错误次数限制） ====================

    /** 校验结果 */
    public enum Result {
        OK("校验通过"),
        WRONG("验证码错误"),
        EXPIRED("验证码已过期"),
        LOCKED("错误次数过多，请重新获取验证码");

        private final String message;

        Result(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /** 一次验证码从下发到校验的完整生命周期。 */
    public static class CodeSession {

        private final String code;
        private final long createTime = System.currentTimeMillis();
        private final int expireSeconds;
        private final int maxAttempts;
        private int wrongCount;

        public CodeSession() {
            this(generate(), DEFAULT_EXPIRE_SECONDS, DEFAULT_MAX_ATTEMPTS);
        }

        public CodeSession(String code, int expireSeconds, int maxAttempts) {
            this.code = code;
            this.expireSeconds = expireSeconds;
            this.maxAttempts = maxAttempts;
        }

        /** 获取要下发（短信/邮件）给用户的验证码。 */
        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createTime > expireSeconds * 1000L;
        }

        /** 剩余有效毫秒数，已过期返回 0。 */
        public long getRemainingMillis() {
            long left = expireSeconds * 1000L - (System.currentTimeMillis() - createTime);
            return Math.max(left, 0L);
        }

        public int getAttemptsLeft() {
            return Math.max(maxAttempts - wrongCount, 0);
        }

        /** 校验用户输入，并处理过期与尝试次数。 */
        public Result verify(String input) {
            if (isExpired()) {
                return Result.EXPIRED;
            }
            if (wrongCount >= maxAttempts) {
                return Result.LOCKED;
            }
            if (VerificationCode.verify(input, code)) {
                return Result.OK;
            }
            wrongCount++;
            return wrongCount >= maxAttempts ? Result.LOCKED : Result.WRONG;
        }
    }

    // ==================== 控制台演示 ====================

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CodeSession session = new CodeSession();
        System.out.println("===== 4 位数字验证码演示 =====");
        System.out.println("已生成验证码：" + session.getCode()
                + "（有效期 " + DEFAULT_EXPIRE_SECONDS + " 秒，最多可输错 "
                + DEFAULT_MAX_ATTEMPTS + " 次）");

        while (true) {
            System.out.println();
            System.out.println("1. 重新生成一个 4 位验证码");
            System.out.println("2. 输入验证码进行校验");
            System.out.println("3. 批量生成 5 个验证码");
            System.out.println("0. 退出");
            System.out.print("请选择：");

            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
            switch (choice) {
                case "1":
                    session = new CodeSession();
                    System.out.println("新验证码：" + session.getCode());
                    break;
                case "2":
                    System.out.print("请输入验证码：");
                    String input = scanner.hasNextLine() ? scanner.nextLine() : "";
                    Result result = session.verify(input);
                    System.out.println("结果：" + result.getMessage());
                    if (result == Result.OK) {
                        System.out.println("验证通过，验证码已失效，请重新生成。");
                        session = new CodeSession();
                        System.out.println("新验证码：" + session.getCode());
                    } else if (result != Result.EXPIRED && result != Result.LOCKED) {
                        System.out.println("剩余尝试次数：" + session.getAttemptsLeft());
                    }
                    break;
                case "3":
                    System.out.println("批量生成结果：");
                    for (String code : generateBatch(5)) {
                        System.out.println("  " + code);
                    }
                    break;
                case "0":
                    System.out.println("已退出。");
                    scanner.close();
                    return;
                default:
                    System.out.println("输入无效，请输入 0 ~ 3。");
            }
        }
    }
}

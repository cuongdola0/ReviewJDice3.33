import java.util.*;
import java.util.logging.*;

/**
 * A parser for dice expressions such as "2d6+3; d10 & 3d4".
 * 
 * ✅ CHỨC NĂNG MỚI: Logging bằng java.util.logging
 * - Ghi lại các sự kiện quan trọng khi phân tích biểu thức xúc xắc.
 * - Ghi log lỗi nếu input không hợp lệ.
 * 
 * 🔧 Lý do: Hỗ trợ debug, kiểm tra hoạt động parser khi tích hợp vào dự án thực tế.
 */
public class DiceParser {

    /**
     * Logger dùng để ghi log toàn bộ quá trình phân tích cú pháp.
     */
    private static final Logger logger = Logger.getLogger(DiceParser.class.getName());

    static {
        // Cấu hình đơn giản cho Logger
        Logger rootLogger = Logger.getLogger("");
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);
        rootLogger.addHandler(consoleHandler);
        logger.setLevel(Level.FINE);
    }

    /**
     * StringStream là một lớp hỗ trợ để quản lý chuỗi đầu vào trong việc phân tích cú pháp.
     * Nó bao gồm các phương thức để cắt bỏ khoảng trắng, lấy số nguyên, và kiểm tra các biểu thức.
     */
    private static class StringStream {
        StringBuffer buff;

        public StringStream(String s) {
            buff = new StringBuffer(s);
        }

        private void munchWhiteSpace() {
            int index = 0;
            char curr;
            while (index < buff.length()) {
                curr = buff.charAt(index);
                if (!Character.isWhitespace(curr))
                    break;
                index++;
            }
            buff = buff.delete(0, index);
        }

        public boolean isEmpty() {
            munchWhiteSpace();
            return buff.toString().equals("");
        }

        public Integer getInt() {
            return readInt();
        }

        public Integer readInt() {
            int index = 0;
            char curr;
            munchWhiteSpace();
            while (index < buff.length()) {
                curr = buff.charAt(index);
                if (!Character.isDigit(curr))
                    break;
                index++;
            }
            try {
                Integer ans = Integer.parseInt(buff.substring(0, index));
                buff = buff.delete(0, index);
                return ans;
            } catch (Exception e) {
                return null;
            }
        }

        public Integer readSgnInt() {
            munchWhiteSpace();
            StringStream state = save();
            if (checkAndEat("+")) {
                Integer ans = readInt();
                if (ans != null)
                    return ans;
                restore(state);
                return null;
            }
            if (checkAndEat("-")) {
                Integer ans = readInt();
                if (ans != null)
                    return -ans;
                restore(state);
                return null;
            }
            return readInt();
        }

        public boolean checkAndEat(String s) {
            munchWhiteSpace();
            if (buff.indexOf(s) == 0) {
                buff = buff.delete(0, s.length());
                return true;
            }
            return false;
        }

        public StringStream save() {
            return new StringStream(buff.toString());
        }

        public void restore(StringStream ss) {
            this.buff = new StringBuffer(ss.buff);
        }

        public String toString() {
            return buff.toString();
        }
    }

    /**
     * Parses a full dice expression with optional ";" separated parts.
     * 
     * @param input Chuỗi biểu thức xúc xắc (ví dụ: "2d6+3; d10 & 3d4")
     * @return Danh sách các DieRoll đã phân tích thành công hoặc null nếu lỗi.
     */
    public static Vector<DieRoll> parseRoll(String input) {
        logger.fine("Parsing input: " + input);
        StringStream stream = new StringStream(input.toLowerCase());
        Vector<DieRoll> result = parseRollRecursive(stream, new Vector<>());
        if (stream.isEmpty()) {
            logger.fine("Successfully parsed: " + input);
            return result;
        } else {
            logger.warning("Failed to fully parse input: " + input);
            return null;
        }
    }

    /**
     * Parse phần roll của biểu thức xúc xắc, sử dụng đệ quy nếu có dấu phân cách ";"
     * 
     * @param ss Chuỗi đầu vào
     * @param v Danh sách kết quả
     * @return Danh sách các DieRoll
     */
    private static Vector<DieRoll> parseRollRecursive(StringStream ss, Vector<DieRoll> v) {
        Vector<DieRoll> r = parseXDice(ss);
        if (r == null) {
            return null;
        }
        v.addAll(r);
        if (ss.checkAndEat(";")) {
            return parseRollRecursive(ss, v);
        }
        return v;
    }

    /**
     * Parse phần xúc xắc trong biểu thức, có thể có số lượng xúc xắc lặp lại (X)
     * 
     * @param ss Chuỗi đầu vào
     * @return Danh sách DieRoll
     */
    private static Vector<DieRoll> parseXDice(StringStream ss) {
        StringStream saved = ss.save();
        Integer x = ss.getInt();
        int num = (x == null) ? 1 : x;
        if (ss.checkAndEat("x")) {
            num = x;
        } else {
            ss.restore(saved);
        }
        DieRoll dr = parseDice(ss);
        if (dr == null) {
            return null;
        }
        Vector<DieRoll> ans = new Vector<>();
        for (int i = 0; i < num; i++) {
            ans.add(dr);
        }
        return ans;
    }

    /**
     * Parse phần dice của biểu thức xúc xắc.
     * 
     * @param ss Chuỗi đầu vào
     * @return DieRoll đã phân tích hoặc null nếu lỗi
     */
    private static DieRoll parseDice(StringStream ss) {
        return parseDTail(parseDiceInner(ss), ss);
    }

    /**
     * Parse phần dice cơ bản trong biểu thức xúc xắc, bao gồm số xúc xắc và số mặt.
     * 
     * @param ss Chuỗi đầu vào
     * @return DieRoll đã phân tích hoặc null nếu lỗi
     */
    private static DieRoll parseDiceInner(StringStream ss) {
        Integer num = ss.getInt();
        int ndice = (num == null) ? 1 : num;
        if (ss.checkAndEat("d")) {
            num = ss.getInt();
            if (num == null) {
                return null;
            }
            int dsides = num;
            num = ss.readSgnInt();
            int bonus = (num == null) ? 0 : num;
            return new DieRoll(ndice, dsides, bonus);
        } else {
            return null;
        }
    }

    /**
     * Kiểm tra phần tail của biểu thức xúc xắc (sử dụng toán tử '&' nối nhiều dice)
     * 
     * @param r1 DieRoll đầu tiên
     * @param ss Chuỗi đầu vào
     * @return DieRoll đã phân tích
     */
    private static DieRoll parseDTail(DieRoll r1, StringStream ss) {
        if (r1 == null) {
            return null;
        }
        if (ss.checkAndEat("&")) {
            DieRoll d2 = parseDice(ss);
            return parseDTail(new DiceSum(r1, d2), ss);
        } else {
            return r1;
        }
    }

    /**
     * Test method to evaluate expressions with logging and output.
     * 
     * @param input Biểu thức xúc xắc để test
     */
    private static void test(String input) {
        Vector<DieRoll> rolls = parseRoll(input);
        if (rolls == null) {
            System.out.println("Invalid input: " + input);
        } else {
            System.out.println("Parsing: " + input);
            for (DieRoll roll : rolls) {
                System.out.println(roll + ": " + roll.makeRoll());
            }
        }
    }

    /**
     * Main method để kiểm thử nhanh các biểu thức dice.
     */
    public static void main(String[] args) {
        test("d6");
        test("2d6");
        test("d6+5");
        test("4X3d8-5");
        test("12d10+5 & 4d6+2");
        test("d6 ; 2d4+3");
        test("4d6+3 ; 8d12 -15 ; 9d10 & 3d6 & 4d12 +17");
        test("4d4d4");
        test("hi");
    }
}
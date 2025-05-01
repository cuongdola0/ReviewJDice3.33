import java.util.logging.Logger;
import java.util.logging.Level;

// Lớp DiceSum kế thừa từ DieRoll, dùng để cộng kết quả của 2 lần tung xúc xắc
public class DiceSum extends DieRoll {
    private static final Logger logger = Logger.getLogger(DiceSum.class.getName()); // Logger cho lớp DiceSum

    private DieRoll r1; // Biến thành viên đại diện cho lần tung xúc xắc thứ nhất
    private DieRoll r2; // Biến thành viên đại diện cho lần tung xúc xắc thứ hai

    // Constructor nhận vào 2 đối tượng DieRoll, khởi tạo lớp cha với giá trị mặc định
    public DiceSum(DieRoll r1, DieRoll r2) {
        super(0, 0, 0); // Gọi constructor của lớp cha DieRoll với các tham số giả định
        this.r1 = r1;
        this.r2 = r2;

        logger.info("DiceSum instance created with two DieRoll objects.");
    }

    // Ghi đè phương thức makeRoll() để trả về kết quả là sự kết hợp của 2 lần tung xúc xắc
    @Override
    public RollResult makeRoll() {
        logger.info("Starting makeRoll() in DiceSum.");

        RollResult result1 = r1.makeRoll(); // Gọi tung xúc xắc lần 1
        logger.fine("First roll result: " + result1);

        RollResult result2 = r2.makeRoll(); // Gọi tung xúc xắc lần 2
        logger.fine("Second roll result: " + result2);

        RollResult combined = result1.andThen(result2); // Kết hợp kết quả
        logger.info("Combined roll result: " + combined);

        return combined;
    }

    // Ghi đè phương thức toString() để hiển thị thông tin cả 2 lần tung xúc xắc
    @Override
    public String toString() {
        return r1.toString() + " & " + r2.toString();
    }
}

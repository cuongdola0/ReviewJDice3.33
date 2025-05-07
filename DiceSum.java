import java.util.logging.Logger;
import java.util.logging.Level;

/**quoccuong
 * Lớp DiceSum mở rộng từ DieRoll, đại diện cho phép cộng kết quả của hai lần tung xúc xắc.
 * Lớp này sử dụng hai đối tượng DieRoll r1 và r2, và cho phép kết hợp kết quả.
 */
public class DiceSum extends DieRoll {
    private static final Logger logger = Logger.getLogger(DiceSum.class.getName());

    private final DieRoll r1;
    private final DieRoll r2;

    /**
     * Constructor khởi tạo DiceSum từ hai đối tượng DieRoll.
     *
     * @param r1 Đối tượng DieRoll thứ nhất, không được null
     * @param r2 Đối tượng DieRoll thứ hai, không được null
     * @throws IllegalArgumentException nếu r1 hoặc r2 null
     */
    public DiceSum(DieRoll r1, DieRoll r2) {
        super(0, 0, 0); // Thông tin không dùng, vì DiceSum dùng composition

        if (r1 == null || r2 == null) {
            logger.severe("One or both DieRoll objects are null.");
            throw new IllegalArgumentException("DieRoll parameters cannot be null.");
        }

        this.r1 = r1;
        this.r2 = r2;

        logger.info("DiceSum initialized with two DieRoll objects.");
    }

    /**
     * Thực hiện hai lần tung xúc xắc và kết hợp kết quả.
     *
     * @return RollResult kết hợp từ r1 và r2
     */
    @Override
    public RollResult makeRoll() {
        logger.info("makeRoll() called in DiceSum.");

        RollResult result1 = r1.makeRoll();
        logger.fine("First roll result: " + result1);

        RollResult result2 = r2.makeRoll();
        logger.fine("Second roll result: " + result2);

        RollResult combined = result1.addResult(result2);
        logger.info("Combined roll result: " + combined);

        return combined;
    }

    /**
     * Phương thức tiện ích: Trả về tổng điểm của hai lần tung.
     *
     * @return tổng điểm (int)
     */
    public int getTotalRollValue() {
        return makeRoll().getTotal();
    }

    /**
     *
     * @return chuỗi mô tả
     */
    @Override
    public String toString() {
        return "DiceSum: [" + r1.toString() + "] + [" + r2.toString() + "] => Total: " + getTotalRollValue();
    }
}

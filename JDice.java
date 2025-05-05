import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/*vanquy
JDice: Java Dice Rolling Program
Copyright (C) 2006 Andrew D. Hilton  (adhilton@cis.upenn.edu)


This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

 */
public class JDice {

    // Thêm Logger để ghi log sự kiện & lỗi
    private static final Logger LOGGER = Logger.getLogger(JDice.class.getName());

    static final String CMD_CLEAR = "Clear";

    static final String ROLL = "Roll Selection";

    static void showError(String s) {
        JOptionPane.showConfirmDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
        LOGGER.warning("Error shown to user: " + s); //  Log lỗi hiển thị cho người dùng
    }

    private static class JDiceListener implements ActionListener {
        Vector<String> listItems;
        JList<String> resultList;
        JComboBox<String> inputBox;
        long lastEvent;

        public JDiceListener(JList<String> resultList, JComboBox<String> inputBox) {
            this.listItems = new Vector<>();
            this.resultList = resultList;
            this.inputBox = inputBox;
            lastEvent = 0;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getWhen() == lastEvent) return;
            lastEvent = e.getWhen();

            String command = e.getActionCommand();
            LOGGER.info("Action performed: " + command); //  Log hành động người dùng

            if (e.getSource() instanceof JComboBox || ROLL.equals(command)) {
                String s = inputBox.getSelectedItem().toString();
                String[] arr = s.split("=");
                String name = "";
                for (int i = 0; i < arr.length - 2; i++) {
                    name += arr[i] + "=";
                }
                if (arr.length >= 2) {
                    name += arr[arr.length - 2];
                }
                doRoll(name, arr[arr.length - 1]);
            } else if (CLEAR.equals(command)) {
                doClear();
            } else {
                doRoll(null, command);
            }
        }

        private void doClear() {
            resultList.clearSelection();
            listItems.clear();
            resultList.setListData(listItems);
            LOGGER.info("Result list cleared."); // Log khi danh sách bị xóa
        }

        private void doRoll(String name, String diceString) {
            String prepend = "";
            int start = 0;

            Vector<DieRoll> v = DiceParser.parseRoll(diceString);
            if (v == null) {
                showError("Invalid dice string: " + diceString);
                LOGGER.warning("Invalid dice input: " + diceString); //  Log input sai
                return;
            }

            if (name != null) {
                listItems.add(0, name);
                start = 1;
                prepend = "  ";
            }

            int[] selectionIndices = new int[start + v.size()];
            for (int i = 0; i < v.size(); i++) {
                DieRoll dr = v.get(i);
                RollResult rr = dr.roll();
                String toAdd = prepend + dr + "  =>  " + rr;
                listItems.add(i + start, toAdd);
            }

            for (int i = 0; i < selectionIndices.length; i++) {
                selectionIndices[i] = i;
            }

            resultList.setListData(listItems);
            resultList.setSelectedIndices(selectionIndices);

            LOGGER.info("Roll completed: " + diceString); // Log khi roll thành công
        }
    }

    public static void main(String[] args) {
        setupLogging(); //  Cấu hình logging trước khi chạy

        Vector<String> v = new Vector<>();
        if (args.length >= 1) {
            try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
                String s;
                while ((s = br.readLine()) != null) {
                    v.add(s);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                LOGGER.log(Level.SEVERE, "Could not read input file: " + args[0], ioe); //  Log lỗi file
            }
        }

        JFrame jf = new JFrame("Dice Roller");
        Container c = jf.getContentPane();
        c.setLayout(new BorderLayout());

        JList<String> jl = new JList<>();
        JScrollPane scrollPane = new JScrollPane(jl);
        c.add(scrollPane, BorderLayout.CENTER);

        JComboBox<String> jcb = new JComboBox<>(v);
        jcb.setEditable(true);
        c.add(jcb, BorderLayout.NORTH);

        JDiceListener jdl = new JDiceListener(jl, jcb);
        jcb.addActionListener(jdl);

        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        String[] buttons = {ROLL, "d4", "d6", "d8", "d10", "d12", "d20", "d100", CLEAR};

        for (String button : buttons) {
            JButton newButton = new JButton(button);
            rightSide.add(newButton);
            newButton.addActionListener(jdl);
        }

        c.add(rightSide, BorderLayout.EAST);

        jf.setSize(450, 500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);

        LOGGER.info("JDice GUI started."); //  Log khi giao diện được hiển thị
    }

    //  Hàm cấu hình logging đơn giản
    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);
    }
}
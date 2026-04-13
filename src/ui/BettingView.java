package ui;

import javax.swing.*;
import java.awt.*;
import model.GameModel;

public class BettingView extends JPanel {
    private JSlider wagerSlider;
    private JLabel wagerDisplay;
    private JButton confirmBtn;

    public BettingView(int maxScore) {
        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(40);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Final Round Betting");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        wagerDisplay = new JLabel("Selected Wager: 0");
        wagerDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        wagerDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        wagerSlider = new JSlider(0, maxScore, 0);
        wagerSlider.setBackground(Color.WHITE);
        wagerSlider.addChangeListener(e -> {
            wagerDisplay.setText("Selected Wager: " + wagerSlider.getValue());
        });

        confirmBtn = createStyledButton("Confirm Wager");

        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(new JLabel("Move the slider to choose your bet:"));
        card.add(wagerSlider);
        card.add(Box.createVerticalStrut(10));
        card.add(wagerDisplay);
        card.add(Box.createVerticalStrut(30));
        card.add(confirmBtn);

        add(card);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(200, 50));

        return btn;
    }

    public int getWager() {
        return wagerSlider.getValue();
    }

    public JButton getConfirmBtn() {
        return confirmBtn;
    }
}
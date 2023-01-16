package Dosimetry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import ij.IJ;
import ij.plugin.PlugIn;

public class GridPage extends JPanel   {

	public static final int MAX_ROWS = 40;
	public static final int MAX_COLS = 12;
	private JButton validatorButton = new JButton("Validate");
	private JSpinner columnsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, MAX_COLS, 1));
	private JSpinner rowsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, MAX_ROWS, 1));
	private List<JButton> buttonsList = new ArrayList<>();
	private JPanel gridPanel = new JPanel();

	public GridPage() {
		JPanel topPanel = new JPanel();
		topPanel.add(new JLabel("Columns:"));
		topPanel.add(columnsSpinner);
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(new JLabel("Rows:"));
		topPanel.add(rowsSpinner);
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(validatorButton);

		JScrollPane scrollPane = new JScrollPane(gridPanel);

		int gridWidth = 1000;
		int gridHeight = 600;
		scrollPane.setPreferredSize(new Dimension(gridWidth, gridHeight));

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);

		validatorButton.addActionListener(e -> validateGrid());
	}

	private void validateGrid() {
		int nbRows = (int) rowsSpinner.getValue();
		int nbColumns = (int) columnsSpinner.getValue();
		gridPanel.removeAll();
		buttonsList.clear();
		gridPanel.setLayout(new GridLayout(nbRows, nbColumns));
		for (int i = 0; i < nbRows * nbColumns; i++) {
			int column = i % nbColumns;
			int row = i / nbColumns;
			String text = String.format("[%02d, %02d]", column, row);
			JButton button = new JButton(text);
			button.addActionListener(e -> gridButtonAction(column, row));
			buttonsList.add(button);
			gridPanel.add(button);
		}
		gridPanel.revalidate();
		gridPanel.repaint();
	}

	private void gridButtonAction(int column, int row) {
		String message = String.format("Button pressed: [%02d, %02d]", column, row);
		String title = "Grid Button Press";
		int type = JOptionPane.INFORMATION_MESSAGE;
		JOptionPane.showMessageDialog(this, message, title, type);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			GridPage mainPanel = new GridPage();

			IJ.log("eseguo grid");
			JFrame frame = new JFrame("GUI");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(mainPanel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}


}

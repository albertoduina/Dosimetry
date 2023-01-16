package Dosimetry;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

/*
 * LabelDemo.java needs one other file:
 *   images/middle.gif
 */
public class LabelDemo extends JPanel {
	public LabelDemo() {
		super(new GridLayout(3, 1)); // 3 rows, 1 column
		JLabel label1, label2, label3, label4, label5, label6, label7, label8;

//        ImageIcon icon = createImageIcon("images/middle.gif",
//                                         "a pretty but meaningless splat");

		// Create the first label.
//        label1 = new JLabel("Image and Text",
//                            icon,
//                            JLabel.CENTER);
//        //Set the position of its text, relative to its icon:
//        label1.setVerticalTextPosition(JLabel.BOTTOM);
//        label1.setHorizontalTextPosition(JLabel.CENTER);

		// Create the other labels.
		label2 = new JLabel("Text-Only Label1 ");
		label3 = new JLabel("    Text-Only Label2 ");
		label4 = new JLabel("    Text-Only Label3 \n");
		label5 = new JLabel("Text-Only Label4 ");
		label6 = new JLabel("    Text-Only Label5 ");
		label7 = new JLabel("    Text-Only Label6 \n");
		label8 = new JLabel("Text-Only Label7 ");
//        label3 = new JLabel(icon);

		// Create tool tips, for the heck of it.
		// label1.setToolTipText("A label containing both image and text");
		label2.setToolTipText("A label containing only text");
//        label3.setToolTipText("A label containing only an image");

		// Add the labels.
//        add(label1);
		add(label2);
		add(label3);
		add(label4);
		add(label5);
		add(label6);
		add(label7);
		add(label8);
//        add(label3);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = LabelDemo.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event dispatch thread.
	 */
	static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("LabelDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		frame.add(new LabelDemo());

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				createAndShowGUI();
			}
		});
	}
}
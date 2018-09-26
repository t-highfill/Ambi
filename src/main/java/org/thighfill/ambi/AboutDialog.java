package org.thighfill.ambi;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.stream.Stream;

public class AboutDialog extends JDialog {

    JPanel panel = new JPanel();
    JLabel nameVersion = new JLabel();
    JLabel author = new JLabel();
    JLabel email = new JLabel();

    public AboutDialog(AmbiContext context) {
        super(context.getAmbi(), "About Ambi");
        this.getContentPane().add(panel);
        Stream.of(panel, nameVersion, author, email).forEach(c -> c.setVisible(true));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(nameVersion);
        panel.add(author);
        panel.add(email);

        nameVersion.setText(String.format("%s version %s", context.getProgramName(), context.getVersion()));
        author.setText("By: " + context.getAuthor());
        email.setText("Contact email: " + context.getContactEmail());
    }

}

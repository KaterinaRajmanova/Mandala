package cz.czechitas.mandala;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import net.miginfocom.swing.*;
import net.sevecek.util.*;

public class HlavniOkno extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    JLabel labAktualniBarva;
    JLabel labBarvaZelena;
    JLabel labBarvaRuzova;
    JLabel labBarvaModra;
    JLabel labBarvaZluta;
    JLabel labBarvaBrcalova;
    JLabel labVyberObrazek;
    JLabel labUlozitObrazek;
    JLabel labObrazek;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    JPanel contentPane;
    MigLayout migLayoutManager;
    BufferedImage obrazek;
    Graphics2D stetec;
    JFileChooser dialog = new JFileChooser(".");


    public HlavniOkno() {
        initComponents();
        nahrajVychoziObrazek();

        stetec = (Graphics2D)obrazek.getGraphics();
        
    }


    private void nahrajVychoziObrazek() {
        try {
            InputStream zdrojObrazku = getClass().getResourceAsStream("/cz/czechitas/mandala/vychozi-mandala.png");
            obrazek = ImageIO.read(zdrojObrazku);
            labObrazek.setIcon(new ImageIcon(obrazek));
            labObrazek.setMinimumSize(new Dimension(obrazek.getWidth(), obrazek.getHeight()));
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se nahrát zabudovaný obrázek mandaly:\n\n" + ex.getMessage());
        }
    }
    
    private void priStiskuBarvy(MouseEvent e) {

        JLabel barva = (JLabel)e.getSource();
        stetec.setColor(barva.getBackground());
        labBarvaZelena.setText("");
        labBarvaBrcalova.setText("");
        labBarvaModra.setText("");
        labBarvaRuzova.setText("");
        labBarvaZluta.setText("");
        barva.setText("o");

    }

    private void priStiskuLabObrazek(MouseEvent e) {
       vyplnObrazek(obrazek,e.getX(),e.getY(),stetec.getColor());
       labObrazek.repaint();
    }


    public void vyplnObrazek(BufferedImage obrazek, int x, int y, Color barva) {
        if (barva == null) {
            barva = new Color(255, 255, 0);
        }

        // Zamez vyplnovani mimo rozsah
        if (x < 0 || x >= obrazek.getWidth() || y < 0 || y >= obrazek.getHeight()) {
            return;
        }

        WritableRaster pixely = obrazek.getRaster();
        int[] novyPixel = new int[] {barva.getRed(), barva.getGreen(), barva.getBlue(), barva.getAlpha()};
        int[] staryPixel = new int[] {255, 255, 255, 255};
        staryPixel = pixely.getPixel(x, y, staryPixel);

        // Pokud uz je pocatecni pixel obarven na cilovou barvu, nic nemen
        if (pixelyMajiStejnouBarvu(novyPixel, staryPixel)) {
            return;
        }

        // Zamez prebarveni cerne cary
        int[] cernyPixel = new int[] {0, 0, 0, staryPixel[3]};
        if (pixelyMajiStejnouBarvu(cernyPixel, staryPixel)) {
            return;
        }

        vyplnovaciSmycka(pixely, x, y, novyPixel, staryPixel);
    }


    private void vyplnovaciSmycka(WritableRaster raster, int x, int y, int[] novaBarva, int[] nahrazovanaBarva) {
        Rectangle rozmery = raster.getBounds();
        int[] aktualniBarva = new int[] {255, 255, 255, 255};

        Deque<Point> zasobnik = new ArrayDeque<>(rozmery.width * rozmery.height);
        zasobnik.push(new Point(x, y));
        while (zasobnik.size() > 0) {
            Point point = zasobnik.pop();
            x = point.x;
            y = point.y;
            if (!pixelyMajiStejnouBarvu(raster.getPixel(x, y, aktualniBarva), nahrazovanaBarva)) {
                continue;
            }

            // Najdi levou zed, po ceste vyplnuj
            int levaZed = x;
            do {
                raster.setPixel(levaZed, y, novaBarva);
                levaZed--;
            }
            while (levaZed >= 0 && pixelyMajiStejnouBarvu(raster.getPixel(levaZed, y, aktualniBarva), nahrazovanaBarva));
            levaZed++;

            // Najdi pravou zed, po ceste vyplnuj
            int pravaZed = x;
            do {
                raster.setPixel(pravaZed, y, novaBarva);
                pravaZed++;
            }
            while (pravaZed < rozmery.width && pixelyMajiStejnouBarvu(raster.getPixel(pravaZed, y, aktualniBarva), nahrazovanaBarva));
            pravaZed--;

            // Pridej na zasobnik body nahore a dole
            for (int i = levaZed; i <= pravaZed; i++) {
                if (y > 0 && pixelyMajiStejnouBarvu(raster.getPixel(i, y - 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y - 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y - 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y - 1));
                    }
                }
                if (y < rozmery.height - 1 && pixelyMajiStejnouBarvu(raster.getPixel(i, y + 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y + 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y + 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y + 1));
                    }
                }
            }
        }
    }


    private boolean pixelyMajiStejnouBarvu(int[] barva1, int[] barva2) {
        return barva1[0] == barva2[0] && barva1[1] == barva2[1] && barva1[2] == barva2[2];
    }





    private void otevritObrazek() {
        int vysledek = dialog.showOpenDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File soubor = dialog.getSelectedFile();
        nahrajObrazekZeSouboru(soubor);

        // Zvetsi okno presne na obrazek
        pack();
        setMinimumSize(getSize());
    }


    private void nahrajObrazekZeSouboru(File soubor) {
        try {
            obrazek = ImageIO.read(soubor);
            labObrazek.setIcon(new ImageIcon(obrazek));
            labObrazek.setMinimumSize(new Dimension(obrazek.getWidth(), obrazek.getHeight()));
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se nahrát obrázek mandaly ze souboru " + soubor.getAbsolutePath());
        }
    }

    private void labVyberObrazekStisknutiMysi(MouseEvent e) {
        otevritObrazek();
    }

    private void ulozitJako() {
        int vysledek = dialog.showSaveDialog(this);
        if (vysledek != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File soubor = dialog.getSelectedFile();

        // Dopln pripadne chybejici priponu .png
        if (!soubor.getName().contains(".") && !soubor.exists()) {
            soubor = new File(soubor.getParentFile(), soubor.getName() + ".png");
        }

        // Opravdu prepsat existujici soubor?
        if (soubor.exists()) {
            int potvrzeni = JOptionPane.showConfirmDialog(this, "Soubor " + soubor.getName() + " už existuje.\nChcete jej přepsat?", "Přepsat soubor?", JOptionPane.YES_NO_OPTION);
            if (potvrzeni == JOptionPane.NO_OPTION) {
                return;
            }
        }

        ulozObrazekDoSouboru(soubor);
    }

    private void ulozObrazekDoSouboru(File soubor) {
        try {
            ImageIO.write(obrazek, "png", soubor);
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se uložit obrázek mandaly do souboru " + soubor.getAbsolutePath());
        }
    }
    
    private void labUlozitObrazekStisknutiMysi(MouseEvent e) {
        ulozitJako();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        labAktualniBarva = new JLabel();
        labBarvaZelena = new JLabel();
        labBarvaRuzova = new JLabel();
        labBarvaModra = new JLabel();
        labBarvaZluta = new JLabel();
        labBarvaBrcalova = new JLabel();
        labVyberObrazek = new JLabel();
        labUlozitObrazek = new JLabel();
        labObrazek = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mandala");
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "insets rel,hidemode 3",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[97,grow,fill]" +
            "[grow,fill]",
            // rows
            "[]" +
            "[]" +
            "[grow,fill]"));
        this.contentPane = (JPanel) this.getContentPane();
        this.contentPane.setBackground(this.getBackground());
        LayoutManager layout = this.contentPane.getLayout();
        if (layout instanceof MigLayout) {
            this.migLayoutManager = (MigLayout) layout;
        }

        //---- labAktualniBarva ----
        labAktualniBarva.setText("Aktu\u00e1ln\u00ed barva:");
        contentPane.add(labAktualniBarva, "cell 0 0");

        //---- labBarvaZelena ----
        labBarvaZelena.setMinimumSize(new Dimension(32, 32));
        labBarvaZelena.setBorder(new BevelBorder(BevelBorder.RAISED));
        labBarvaZelena.setOpaque(true);
        labBarvaZelena.setBackground(new Color(0, 153, 153));
        labBarvaZelena.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaZelena.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                priStiskuBarvy(e);
            }
        });
        contentPane.add(labBarvaZelena, "cell 1 0");

        //---- labBarvaRuzova ----
        labBarvaRuzova.setMinimumSize(new Dimension(32, 32));
        labBarvaRuzova.setBorder(new BevelBorder(BevelBorder.RAISED));
        labBarvaRuzova.setOpaque(true);
        labBarvaRuzova.setBackground(new Color(255, 204, 204));
        labBarvaRuzova.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaRuzova.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                priStiskuBarvy(e);
            }
        });
        contentPane.add(labBarvaRuzova, "cell 2 0");

        //---- labBarvaModra ----
        labBarvaModra.setMinimumSize(new Dimension(32, 32));
        labBarvaModra.setBorder(new BevelBorder(BevelBorder.RAISED));
        labBarvaModra.setOpaque(true);
        labBarvaModra.setBackground(new Color(204, 255, 255));
        labBarvaModra.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaModra.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                priStiskuBarvy(e);
            }
        });
        contentPane.add(labBarvaModra, "cell 3 0");

        //---- labBarvaZluta ----
        labBarvaZluta.setMinimumSize(new Dimension(32, 32));
        labBarvaZluta.setBorder(new BevelBorder(BevelBorder.RAISED));
        labBarvaZluta.setOpaque(true);
        labBarvaZluta.setBackground(new Color(255, 255, 153));
        labBarvaZluta.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaZluta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                priStiskuBarvy(e);
            }
        });
        contentPane.add(labBarvaZluta, "cell 4 0");

        //---- labBarvaBrcalova ----
        labBarvaBrcalova.setMinimumSize(new Dimension(32, 32));
        labBarvaBrcalova.setBorder(new BevelBorder(BevelBorder.RAISED));
        labBarvaBrcalova.setOpaque(true);
        labBarvaBrcalova.setBackground(new Color(153, 153, 0));
        labBarvaBrcalova.setHorizontalAlignment(SwingConstants.CENTER);
        labBarvaBrcalova.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                priStiskuBarvy(e);
            }
        });
        contentPane.add(labBarvaBrcalova, "cell 5 0");

        //---- labVyberObrazek ----
        labVyberObrazek.setText("Vyber obr\u00e1zek");
        labVyberObrazek.setBorder(new BevelBorder(BevelBorder.RAISED));
        labVyberObrazek.setHorizontalAlignment(SwingConstants.CENTER);
        labVyberObrazek.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                labVyberObrazekStisknutiMysi(e);
            }
        });
        contentPane.add(labVyberObrazek, "cell 6 0,growy");

        //---- labUlozitObrazek ----
        labUlozitObrazek.setText("Ulo\u017e obr\u00e1zek");
        labUlozitObrazek.setBorder(new BevelBorder(BevelBorder.RAISED));
        labUlozitObrazek.setHorizontalAlignment(SwingConstants.CENTER);
        labUlozitObrazek.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                labUlozitObrazekStisknutiMysi(e);
            }
        });
        contentPane.add(labUlozitObrazek, "cell 7 0,grow");

        //---- labObrazek ----
        labObrazek.setHorizontalAlignment(SwingConstants.LEFT);
        labObrazek.setVerticalAlignment(SwingConstants.TOP);
        labObrazek.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                priStiskuLabObrazek(e);
            }
        });
        contentPane.add(labObrazek, "cell 0 2 8 1");
        setSize(540, 540);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}

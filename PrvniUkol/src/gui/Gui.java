/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import app.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import transforms.Vec3D;

/**
 *
 * @author Matthes
 */
public class Gui extends JFrame
{
    private Renderer renderer;
    
    private JLabel lbBody;
    private JLabel lbDelkaSvetla;
    
    private JButton btPolymode, btBodyPlus, btBodyMinus, btVyberBarvySvetla;
    private JButton btDelkaSvetlaPlus, btDelkaSvetlaMinus, btSteny, btUtlumy;
    
    private JPanel pnlBody, pnlDelkaSvetla;
    private JPanel pnlCenter;
    
    private JComboBox<String> cbMaterialy, cbSvetlo, cbTextura, cbMapping, cbObarveni;
    
    public Gui(Renderer renderer)
    {
        this.renderer = renderer;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Ovládání");
        initGui();
    }
    
    private void initGui()
    {
        btPolymode = new JButton("Polygony");
        btPolymode.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                renderer.setPoly(!renderer.isPoly());
            }
        });
        
        btSteny = new JButton("Stěny");
        btSteny.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                renderer.setZdi(!renderer.isZdi());
            }
        });
        
        btUtlumy = new JButton("Útlumy světel");
        btUtlumy.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                renderer.setUtlumy(!renderer.isUtlumy());
            }
        });
        
        String[] materialy = new String[] {"Default", "Měď", "Černá guma", "Chrom", "Zlato", "Emerald", "Rubín", "Tyrkys"};
        
        cbMaterialy = new JComboBox<>(materialy);
        cbMaterialy.setSelectedIndex(0);
        cbMaterialy.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JComboBox cb = (JComboBox)e.getSource();
                renderer.setMaterial((Integer) cb.getSelectedIndex());
            }
        });
        
        String[] textury = new String[] {"Žádná", "Kamenná zeď", "Cihlová zeď"};
        
        cbTextura = new JComboBox<>(textury);
        cbTextura.setSelectedIndex(0);
        cbTextura.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JComboBox cb = (JComboBox)e.getSource();
                renderer.setTextura((Integer) cb.getSelectedIndex());
            }
        });
        
        String[] mapy = new String[] {"Vertex normála", "Normálová mapa", "Parallax mapping"};
        
        cbMapping = new JComboBox<>(mapy);
        cbMapping.setSelectedIndex(0);
        cbMapping.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JComboBox cb = (JComboBox)e.getSource();
                renderer.setMapping((Integer) cb.getSelectedIndex());
            }
        });
        
        String[] obarveni = new String[] {"Bílá", "Vybraná barva", "Normála vert.", "Souřadnice v gridu", "Pozice",
        "Souřadnice textury (modulo)", "Normála frag."};
        
        cbObarveni = new JComboBox<>(obarveni);
        cbObarveni.setSelectedIndex(0);
        cbObarveni.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JComboBox cb = (JComboBox)e.getSource();
                renderer.setObarveni((Integer) cb.getSelectedIndex());
            }
        });
        
        String[] svetla = new String[] {"Žádné", "Lambert vertex sh.", "Phong vertex sh.", "Blinn-Phong vert.", 
            "Ambientní složka vert.", "Difůzní složka vert.", "Speculární složka Phong v.",
            "Speculární. složka Blinn-Phong v.", "Lambert fragment sh.", "Phong fragment sh.", "Blinn-Phong frag.", 
            "Ambientní složka frag.", "Difůzní složka frag.", "Speculární složka Phong f.",
            "Speculární. složka Blinn-Phong f."};
        
        cbSvetlo = new JComboBox<>(svetla);
        cbSvetlo.setSelectedIndex(0);
        cbSvetlo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JComboBox cb = (JComboBox)e.getSource();
                renderer.setSvetlo((Integer) cb.getSelectedIndex());
            }
        });
        
        lbBody = new JLabel();
        lbBody.setText("Bodů v gridu: " + Integer.toString(renderer.getPocetBodu()));
        
        btBodyMinus = new JButton("-");
        btBodyMinus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (renderer.getPocetBodu() > 4) {
                    renderer.setPocetBodu(renderer.getPocetBodu() - 1);
                    lbBody.setText("Bodů v gridu: " + Integer.toString( renderer.getPocetBodu()));
                }
            }
        });
        
        btBodyPlus = new JButton("+");
        btBodyPlus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (renderer.getPocetBodu() < 100) {
                    renderer.setPocetBodu(renderer.getPocetBodu() + 1);
                    lbBody.setText("Bodů v gridu: " + Integer.toString( renderer.getPocetBodu()));
                }
            }
        });
        
        lbDelkaSvetla = new JLabel();
        lbDelkaSvetla.setText("Délka světelného kuželu: " + Integer.toString( renderer.getDelkaSvetla()));
        
        btDelkaSvetlaMinus = new JButton("-");
        btDelkaSvetlaMinus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (renderer.getDelkaSvetla() > 1) {
                    renderer.setDelkaSvetla(renderer.getDelkaSvetla() - 1);
                    lbDelkaSvetla.setText("Délka světelného kuželu: " + Integer.toString( renderer.getDelkaSvetla()));
                }
            }
        });
        
        btDelkaSvetlaPlus = new JButton("+");
        btDelkaSvetlaPlus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (renderer.getDelkaSvetla() < 100) {
                    renderer.setDelkaSvetla(renderer.getDelkaSvetla() + 1);
                    lbDelkaSvetla.setText("Délka světelného kuželu: " + Integer.toString( renderer.getDelkaSvetla()));
                }
            }
        });
        
        pnlBody = new JPanel(new FlowLayout());
        pnlBody.add(btBodyMinus);
        pnlBody.add(lbBody);
        pnlBody.add(btBodyPlus);
        
        pnlDelkaSvetla = new JPanel(new FlowLayout());
        pnlDelkaSvetla.add(btDelkaSvetlaMinus);
        pnlDelkaSvetla.add(lbDelkaSvetla);
        pnlDelkaSvetla.add(btDelkaSvetlaPlus);
        
        btVyberBarvySvetla = new JButton("Barva světla…");
        btVyberBarvySvetla.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Color barva = JColorChooser.showDialog(Gui.this, "Výběr barvy světla", Color.white);
                if (barva != null)
                {
                    renderer.setBarva(new Vec3D((double) barva.getRed()/255, (double) barva.getGreen()/255, (double) barva.getBlue()/255));
                }
            }
        });
        
        pnlCenter = new JPanel(new FlowLayout());
        pnlCenter.add(btUtlumy);
        pnlCenter.add(btSteny);
        pnlCenter.add(pnlBody);
        pnlCenter.add(cbMapping);
        pnlCenter.add(pnlDelkaSvetla);
        pnlCenter.add(cbSvetlo);
        pnlCenter.add(cbObarveni);
        pnlCenter.add(btVyberBarvySvetla);
        pnlCenter.add(cbTextura);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(btPolymode, "North");
        getContentPane().add(pnlCenter, "Center");
        getContentPane().add(cbMaterialy, "South");
        
        pack();
        setLocation(580, 0);
        setSize(300, 420);
    }
    
    public void update()
    {
        lbBody.setText("Bodů v gridu: " + Integer.toString(renderer.getPocetBodu()));
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import app.Renderer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

/**
 *
 * @author Matthes
 */
public class Gui extends JFrame
{
    private Renderer renderer;
    
    private JButton btPolymode;
    
    private JComboBox<String> cbMaterialy;
    
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
        
        String[] materialy = new String[] {"Default", "Měď", "Černá guma", "Chrom", "Zlato", "Emerald", "Rubín", "Tyrkys    "};
        
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
        
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(btPolymode, "North");
        getContentPane().add(cbMaterialy, "South");
        
        pack();
        setLocation(580, 0);
        setSize(300, 420);
    }
}

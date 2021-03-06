/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import uk.ac.rdg.resc.edal.dataset.DatasetFactory;
import uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.logging.RescLogging;

/**
 * Main class for the multi-globe video wall software.
 * 
 * @author Guy Griffiths
 */
@SuppressWarnings("serial")
public class VideoWall extends JFrame {
    static final String CACHE_NAME = "imageCache";

    private static final int BUTTON_WIDTH = 50;
    ExecutorService threadPool;

    private VideoWallCatalogue datasetLoader;
    private MultiGlobeFrame globePanels;

    private static VideoWall instance = null;

    static VideoWall getInstance() {
        if (instance == null) {
            try {
                instance = new VideoWall();
            } catch (IOException | EdalException | JAXBException e) {
                System.exit(0);
            }
        }
        return instance;
    }

    private VideoWall() throws IOException, EdalException, JAXBException {
        init();
    }

    /**
     * Set up necessary components and loads some buttons into the main panel
     * for adding/removing globe panels
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws EdalException
     */
    public void init() throws IOException, JAXBException, EdalException {
        int cachingThreads = Configuration.getIntegerValue(
                "uk.ac.rdg.resc.edal.multiglobe.CachingThreads", 3);
        if (cachingThreads < 1) {
            cachingThreads = 1;
        }
        threadPool = Executors.newFixedThreadPool(cachingThreads);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                threadPool.shutdown();
            }
        }));

        Configuration.setValue(AVKey.VIEW_CLASS_NAME, LinkedView.class.getName());

        /*
         * Set the default data reader. This means that we don't need to specify
         * a dataset factory for cases where we are reading gridded NetCDF data
         * (the majority)
         */
        DatasetFactory.setDefaultDatasetFactoryClass(CdmGridDatasetFactory.class);

        /*
         * Initialise the dataset catalogue
         */
        datasetLoader = new VideoWallCatalogue();
        /*
         * Create the main frame which will hold each of the globe panels
         */
        globePanels = new MultiGlobeFrame(datasetLoader);

        /*
         * Create and wire up the panel for adding/removing rows
         */
        JPanel addRemoveRowPanel = new JPanel();

        /* The add row button */
        Button addRowButton = new Button("+");
        addRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                globePanels.addRow();
            }
        });
        setDefaultButtonOptions(addRowButton);
        addRowButton.setMinimumSize(new Dimension(0, BUTTON_WIDTH));
        addRowButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_WIDTH));

        /* The remove row button */
        Button removeRowButton = new Button("-");
        removeRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                globePanels.removeRow();
            }
        });
        setDefaultButtonOptions(removeRowButton);
        removeRowButton.setMinimumSize(new Dimension(0, BUTTON_WIDTH));
        removeRowButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_WIDTH));
        addRemoveRowPanel.setLayout(new BoxLayout(addRemoveRowPanel, BoxLayout.Y_AXIS));
        addRemoveRowPanel.add(removeRowButton);
        addRemoveRowPanel.add(addRowButton);

        /*
         * Create and wire up the panel for adding/removing columns
         */
        JPanel addRemoveColumnPanel = new JPanel();

        /* The add column button */
        Button addColumnButton = new Button("+");
        addColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                globePanels.addColumn();
            }
        });
        setDefaultButtonOptions(addColumnButton);
        addColumnButton.setMinimumSize(new Dimension(BUTTON_WIDTH, 0));
        addColumnButton.setMaximumSize(new Dimension(BUTTON_WIDTH, Integer.MAX_VALUE));

        /* The remove column button */
        Button removeColumnButton = new Button(" - ");
        removeColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                globePanels.removeColumn();
            }
        });
        setDefaultButtonOptions(removeColumnButton);
        removeColumnButton.setMinimumSize(new Dimension(BUTTON_WIDTH, 0));
        removeColumnButton.setMaximumSize(new Dimension(BUTTON_WIDTH, Integer.MAX_VALUE));

        addRemoveColumnPanel.setLayout(new BoxLayout(addRemoveColumnPanel, BoxLayout.X_AXIS));

        addRemoveColumnPanel.add(removeColumnButton);
        addRemoveColumnPanel.add(addColumnButton);

        JButton optionsButton = new JButton(new ImageIcon(ImageIO.read(this.getClass().getResource(
                "/images/config.png"))));
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsWindow options = new OptionsWindow(globePanels, VideoWall.this);
                options.setVisible(true);
            }
        });
        optionsButton.setBackground(Color.black);
        optionsButton.setForeground(Color.lightGray);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(addRemoveRowPanel);
        bottomPanel.add(optionsButton);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(globePanels);
        this.getContentPane().add(addRemoveColumnPanel, BorderLayout.EAST);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    /*
     * Convenience method to style all buttons the same way
     */
    public static void setDefaultButtonOptions(Button button) {
        button.setBackground(Color.black);
        button.setForeground(Color.lightGray);
    }

    public static void main(String[] args) throws IOException, EdalException, JAXBException {
        /*
         * Set the config location
         */
        System.setProperty("gov.nasa.worldwind.config.document", "config/resc_worldwind.xml");

        /*
         * Configure shared cache
         */
        final int cacheSizeMB = Configuration.getIntegerValue(
                "uk.ac.rdg.resc.edal.multiglobe.CacheSize", 64);
        CacheManager singletonManager = CacheManager.create();
        CacheConfiguration config = new CacheConfiguration(CACHE_NAME, 0).eternal(true)
                .maxBytesLocalHeap(cacheSizeMB, MemoryUnit.MEGABYTES)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .persistence(new PersistenceConfiguration().strategy(Strategy.NONE))
                .transactionalMode(TransactionalMode.OFF);
        Cache memoryCache = new Cache(config);
        singletonManager.addCache(memoryCache);

        try {
            /*
             * This code sets the X Windows property WM_CLASS to
             * "VideoWallGlobes"
             * 
             * This is useful for identifying the window for e.g. automatically
             * handling it in a tiling window manager.
             */
            Toolkit xToolkit = Toolkit.getDefaultToolkit();
            java.lang.reflect.Field awtAppClassNameField;
            awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit, "VideoWallGlobes");
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            /*
             * It doesn't really matter if it fails, so ignore it
             */
        }

        final boolean fullscreen = Configuration.getBooleanValue(
                "uk.ac.rdg.resc.edal.multiglobe.Fullscreen", true);
        System.out.println("fullscreen: " + fullscreen);
        final int screenNumber = Configuration.getIntegerValue(
                "uk.ac.rdg.resc.edal.multiglobe.ScreenNumber", 0);

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = null;
                try {
                    frame = new VideoWall();
                } catch (Exception e) {
                    /*
                     * We have a problem instantiating the video wall. This is
                     * unrecoverable, so log it, print stack trace and exit.
                     */
                    String message = RescLogging.getMessage("resc.StartupError");
                    Logging.logger().severe(message);
                    e.printStackTrace();
                    System.exit(0);
                }

                if (fullscreen) {
                    /*
                     * Create a fullscreen application
                     */
                    frame.setUndecorated(true);
                    int size = frame.getExtendedState();
                    size |= Frame.MAXIMIZED_BOTH;
                    frame.setExtendedState(size);

                    /*
                     * Move to a particular screen if defined in the config
                     */
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice[] gd = ge.getScreenDevices();
                    if (screenNumber > -1 && screenNumber < gd.length) {
                        frame.setLocation(gd[screenNumber].getDefaultConfiguration().getBounds().x,
                                gd[screenNumber].getDefaultConfiguration().getBounds().y);
                    } else if (gd.length > 0) {
                        frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, gd[0]
                                .getDefaultConfiguration().getBounds().y);
                    } else {
                        throw new RuntimeException("No Screens Found");
                    }
                }
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setTitle("MultiGlobe");
                frame.setVisible(true);
            }
        });
    }
}
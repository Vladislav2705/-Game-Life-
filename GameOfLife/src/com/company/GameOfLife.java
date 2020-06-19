package com.company;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;

public class GameOfLife extends JFrame {

    final String NAME_OF_GAME = "Игра «Жизнь» ";
    final int LIFE_SIZE = 50;
    final int POINT_RADIUS = 10;
    final int FIELD_SIZE = LIFE_SIZE * POINT_RADIUS + 7;
    final int BTN_PANEL_HEIGHT = 58+4;
    final int START_LOCATION = 200;
    boolean[][] lifeGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] nextGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] tmp;
    int countGeneration = 0;
    int showDelay = 500;
    int showDelayStep = 50;
    volatile boolean goNextGeneration = false;
    boolean useColors = false;
    boolean showGrid = false;
    Random random = new Random();
    Dimension btnDimension = new Dimension(30, 26);
    JFrame frame;
    Canvas canvasPanel;

    // иконки для кнопок
    final ImageIcon icoFill = new ImageIcon(GameOfLife.class.getResource("img/btnFill.png"));
    final ImageIcon icoNew = new ImageIcon(GameOfLife.class.getResource("img/btnNew.png"));
    final ImageIcon icoOpen = new ImageIcon(GameOfLife.class.getResource("img/btnOpen.png"));
    final ImageIcon icoSave = new ImageIcon(GameOfLife.class.getResource("img/btnSave.png"));
    final ImageIcon icoStep = new ImageIcon(GameOfLife.class.getResource("img/btnStep.png"));
    final ImageIcon icoGo = new ImageIcon(GameOfLife.class.getResource("img/btnGo.png"));
    final ImageIcon icoStop = new ImageIcon(GameOfLife.class.getResource("img/btnStop.png"));
    final ImageIcon icoFaster = new ImageIcon(GameOfLife.class.getResource("img/btnFaster.png"));
    final ImageIcon icoSlower = new ImageIcon(GameOfLife.class.getResource("img/btnSlower.png"));
    final ImageIcon icoColor = new ImageIcon(GameOfLife.class.getResource("img/btnColor.png"));
    final ImageIcon icoNoColor = new ImageIcon(GameOfLife.class.getResource("img/btnNoColor.png"));
    final ImageIcon icoGrid = new ImageIcon(GameOfLife.class.getResource("img/btnGrid.png"));

    public static void main(String[] args) {
        new GameOfLife().go();
    }

    void go() {
        frame = new JFrame(NAME_OF_GAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FIELD_SIZE, FIELD_SIZE + BTN_PANEL_HEIGHT);
        frame.setLocation(START_LOCATION, START_LOCATION);
        frame.setResizable(false);

        // случайное заполнение ячеек
        JButton fillButton = new JButton();
        fillButton.setIcon(icoFill);
        fillButton.setPreferredSize(btnDimension);
        fillButton.setToolTipText("Заполнить случайно");
        fillButton.addActionListener(new FillButtonListener());

        // очистка поля
        JButton newButton = new JButton();
        newButton.setIcon(icoNew);
        newButton.setPreferredSize(btnDimension);
        newButton.setToolTipText("Очистить поле");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int x = 0; x < LIFE_SIZE; x++) {
                    Arrays.fill(lifeGeneration[x], false);
                }
                canvasPanel.repaint();
            }
        });



        //получить следующее поколение
        JButton stepButton = new JButton();
        stepButton.setIcon(icoStep);
        stepButton.setPreferredSize(btnDimension);
        stepButton.setToolTipText("Показать следующее поколение");
        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processOfLife();
                canvasPanel.repaint();
            }
        });

        // поколение за поколением без остановки
        final JButton goButton = new JButton();
        goButton.setIcon(icoGo);
        goButton.setPreferredSize(new Dimension(34, 30));
        goButton.setToolTipText("Старт/Стоп поколение за поколением");
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goNextGeneration = !goNextGeneration;
                goButton.setIcon(goNextGeneration? icoStop : icoGo);
                goButton.setFocusable(false);
            }
        });

        //ускоренная смена поколения
        JButton fasterButton = new JButton();
        fasterButton.setIcon(icoFaster);
        fasterButton.setPreferredSize(btnDimension);
        fasterButton.setToolTipText("Быстрее");
        fasterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDelay -= (showDelay - showDelayStep == 0) ? 0 : showDelayStep;
            }
        });

        // медленная смена поколения медленнее
        JButton slowerButton = new JButton();
        slowerButton.setIcon(icoSlower);
        slowerButton.setPreferredSize(btnDimension);
        slowerButton.setToolTipText("Медленнее");
        slowerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDelay += showDelay;
            }
        });

        // включить / выключить цвета
        final JButton colorButton = new JButton();
        colorButton.setIcon(icoColor);
        colorButton.setPreferredSize(btnDimension);
        colorButton.setToolTipText("Включить / Выключить цвета");
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                useColors = !useColors;
                colorButton.setIcon(useColors? icoNoColor : icoColor);
                canvasPanel.repaint();
            }
        });

        // показать / скрыть сетку
        final JButton gridButton = new JButton();
        gridButton.setIcon(icoGrid);
        gridButton.setPreferredSize(btnDimension);
        gridButton.setToolTipText("Показать / Скрыть сетку");
        gridButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showGrid = !showGrid;
                canvasPanel.repaint();
            }
        });

        canvasPanel = new Canvas();
        canvasPanel.setBackground(Color.white);
        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x = e.getX()/POINT_RADIUS;
                int y = e.getY()/POINT_RADIUS;
                lifeGeneration[x][y] = !lifeGeneration[x][y];
                canvasPanel.repaint();
            }
        });

        // Панель с кнопками:
        JPanel btnPanel = new JPanel();
        btnPanel.add(fillButton);
        btnPanel.add(newButton);
        btnPanel.add(stepButton);
        btnPanel.add(goButton);
        btnPanel.add(slowerButton);
        btnPanel.add(fasterButton);
        btnPanel.add(colorButton);
        btnPanel.add(gridButton);

        frame.getContentPane().add(BorderLayout.CENTER, canvasPanel);
        frame.getContentPane().add(BorderLayout.SOUTH, btnPanel);
        frame.setVisible(true);

        // бесконечная петля жизни
        while (true) {
            if (goNextGeneration) {
                processOfLife();
                canvasPanel.repaint();
                try {
                    Thread.sleep(showDelay);
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }

    // Случайно заполнить ячейки
    public class FillButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            countGeneration = 1;
            for (int x = 0; x < LIFE_SIZE; x++) {
                for (int y = 0; y < LIFE_SIZE; y++) {
                    lifeGeneration[x][y] = random.nextBoolean();
                }
            }
            canvasPanel.repaint();
        }
    }

    // подсчитывем количество соседних клеток
    int countNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                int nX = x + dx;
                int nY = y + dy;
                nX = (nX < 0) ? LIFE_SIZE - 1 : nX;
                nY = (nY < 0) ? LIFE_SIZE - 1 : nY;
                nX = (nX > LIFE_SIZE - 1) ? 0 : nX;
                nY = (nY > LIFE_SIZE - 1) ? 0 : nY;
                count += (lifeGeneration[nX][nY]) ? 1 : 0;
            }
        }
        if (lifeGeneration[x][y]) { count--; }
        return count;
    }

    // основной процесс жизни
    void processOfLife() {
        for (int x = 0; x < LIFE_SIZE; x++) {
            for (int y = 0; y < LIFE_SIZE; y++) {
                int count = countNeighbors(x, y);
                nextGeneration[x][y] = lifeGeneration[x][y];
                // Пустая (мёртвая) клетка ровно с 3 живымиклетками-соседями оживает
                nextGeneration[x][y] = (count == 3) ? true : nextGeneration[x][y];
                // если у живой клетки соседок меньше 2 или больше 3 то она умирает(одиночество или перенаселенности)
                nextGeneration[x][y] = ((count < 2) || (count > 3)) ? false : nextGeneration[x][y];
            }
        }
        // смена поколений
        tmp		= nextGeneration;
        nextGeneration	= lifeGeneration;
        lifeGeneration	= tmp;

        countGeneration++;
    }

    public class Canvas extends JPanel {

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (int x = 0; x < LIFE_SIZE; x++) {
                for (int y = 0; y < LIFE_SIZE; y++) {
                    if (lifeGeneration[x][y]) {
                        if (useColors) {
                            int count = countNeighbors(x, y);
                            g.setColor(((count < 2) || (count > 3))? Color.red : Color.blue);
                        } else {
                            g.setColor(Color.black);
                        }
                        g.fillOval(x*POINT_RADIUS, y*POINT_RADIUS, POINT_RADIUS, POINT_RADIUS);
                    } else {
                        if (useColors) {
                            int count = countNeighbors(x, y);
                            if (count == 3) {
                                g.setColor(new Color(225, 255, 235));
                                g.fillOval(x*POINT_RADIUS, y*POINT_RADIUS, POINT_RADIUS, POINT_RADIUS);
                            }
                        }
                    }
                    if (showGrid) {
                        g.setColor(Color.lightGray);
                        g.drawLine((x+1)*POINT_RADIUS-1, (y+1)*POINT_RADIUS, (x+1)*POINT_RADIUS+1, (y+1)*POINT_RADIUS);
                        g.drawLine((x+1)*POINT_RADIUS, (y+1)*POINT_RADIUS-1, (x+1)*POINT_RADIUS, (y+1)*POINT_RADIUS+1);
                    }
                }
            }
            frame.setTitle(NAME_OF_GAME + " Поколение:" + countGeneration);
        }
    }
}
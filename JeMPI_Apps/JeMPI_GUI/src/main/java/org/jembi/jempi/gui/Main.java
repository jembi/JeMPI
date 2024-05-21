package org.jembi.jempi.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.CustomDemographicData;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends JPanel {

   private static final Field[] DEMOGRAPHIC_FIELDS = CustomDemographicData.class.getDeclaredFields();


   private static final Logger LOGGER = LogManager.getLogger(Main.class);

   public Main() {
      this.setLayout(new GridLayout(1, 0));
      final MyJTable table = new MyJTable(new MyTableModel());
      this.add(new JScrollPane(table));
   }

   public static void main(final String[] args) {
      javax.swing.SwingUtilities.invokeLater(() -> new Main().display());
   }

   private void display() {
      JFrame f = new JFrame("JeMPI GUI");
      f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      f.setContentPane(this);
      f.pack();
      f.setVisible(true);
   }

   private static class MyTableModel extends AbstractTableModel {
      private final String[] colNames;
      private int rowIndex = -1;
      private String[] rowData = null;

      MyTableModel() {
         super();
         final var colNamesList = new ArrayList<String>(3 + DEMOGRAPHIC_FIELDS.length + 1);
         Collections.addAll(colNamesList, "Aux ID", "UID", "Created");
         Collections.addAll(colNamesList,
                            new ArrayList<>(Arrays.stream(DEMOGRAPHIC_FIELDS)
                                                  .map(Field::getName)
                                                  .toList()).toArray(new String[0]));
         Collections.addAll(colNamesList, "Score");
         colNames = colNamesList.toArray(new String[0]);
      }

      public int getColumnCount() {
         return colNames.length;
      }

      public int getRowCount() {
         return (int) Cache.getNumberRows();
      }

      @Override
      public String getColumnName(final int col) {
         return colNames[col];
      }

      public Object getValueAt(
            final int row,
            final int col) {
         if (row != rowIndex) {
            rowData = Cache.get(row);
            rowIndex = row;
         }
         return rowData[col];
      }

      @Override
      public Class getColumnClass(final int c) {
         var i = 0;
         while (getValueAt(i, c) == null) {
            i++;
         }
         return getValueAt(i, c).getClass();
      }

      @Override
      public boolean isCellEditable(
            final int row,
            final int col) {
         return col >= 2;
      }

   }

   private static class MyJTable extends JTable {

      private DefaultTableCellRenderer goldenRecordRenderer;
      private DefaultTableCellRenderer interactionRenderer;

      MyJTable() {
         super();
         setUp();
      }

      MyJTable(final TableModel tm) {
         super(tm);
         setUp();
      }

      MyJTable(
            final Object[][] data,
            final Object[] columns) {
         super(data, columns);
         setUp();
      }

      MyJTable(
            final int rows,
            final int columns) {
         super(rows, columns);
         setUp();
      }

      private int setColWidth(
            final String label,
            final int charWidth,
            final int chars) {
         final var width = charWidth * chars;
         LOGGER.trace("{}", label);
         this.getColumn(label).setMinWidth(width);
         this.getColumn(label).setMaxWidth(width);
         this.getColumn(label).setPreferredWidth(width);
         this.getColumn(label).setWidth(width);
         return width;
      }

      private void setUp() {
         this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
         FontMetrics metrics = this.getFontMetrics(this.getFont());
         this.setRowHeight(Math.round((metrics.getHeight() * 1.4F)));
         final var charWidth = Math.round(metrics.charWidth('X') * 1.1F);
         AtomicInteger totalWidth = new AtomicInteger();
         totalWidth.addAndGet(setColWidth("Aux ID", charWidth, 17));
         totalWidth.addAndGet(setColWidth("UID", charWidth, 10));
         totalWidth.addAndGet(setColWidth("Created", charWidth, 28));
         Arrays.stream(DEMOGRAPHIC_FIELDS)
               .sequential()
               .forEach(x -> totalWidth.addAndGet(setColWidth(x.getName(), charWidth, 15)));
         totalWidth.addAndGet(setColWidth("Score", charWidth, 10));
         this.setPreferredScrollableViewportSize(new Dimension(totalWidth.get(), 30 * Math.round((metrics.getHeight() * 1.4F))));
         this.setFillsViewportHeight(true);
      }

      @Override
      public TableCellRenderer getCellRenderer(
            final int row,
            final int column) {
         if (goldenRecordRenderer == null) {
            goldenRecordRenderer = new DefaultTableCellRenderer();
            goldenRecordRenderer.setBackground(new Color(0xde, 0xed, 0xfe));
         }
         if (interactionRenderer == null) {
            interactionRenderer = new DefaultTableCellRenderer();
            interactionRenderer.setBackground(new Color(0xfe, 0xf0, 0xde));
         }
         final var score = getValueAt(row, getColumnCount() - 1);
         if (score == null) {
            return goldenRecordRenderer;
         } else {
            return interactionRenderer;
         }
      }
   }

}

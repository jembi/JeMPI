package org.jembi.jempi.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.models.ApiModels;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends JPanel {
   private static final Logger LOGGER = LogManager.getLogger(Main.class);
   private final Cache cache;

   public Main() {
      super(new GridLayout(1, 0));
      cache = new Cache();
      cache.fetchGoldenIds();
      var goldenRecords = cache.fetchExpandedGoldenRecords();
      final var tableModel = new MyTableModel(goldenRecords);
      final JTable table = new JTable(tableModel);
      table.setPreferredScrollableViewportSize(new Dimension(2500, 1100));
      table.setFillsViewportHeight(true);
      JScrollPane scrollPane = new JScrollPane(table);
      add(scrollPane);
   }

   private static void createAndShowGUI() {
      JFrame frame = new JFrame("SimpleTableDemo");
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      Main newContentPane = new Main();
      newContentPane.setOpaque(true); //content panes must be opaque
      frame.setContentPane(newContentPane);
      frame.pack();
      frame.setVisible(true);
   }

   public static void main(final String[] args) {
      javax.swing.SwingUtilities.invokeLater(Main::createAndShowGUI);
   }


   private static class MyTableModel extends AbstractTableModel {
      final Vector<String> colNames = new Vector<>(Stream.of("Aux ID",
                                                             "UID",
                                                             "Created",
                                                             "Given Name",
                                                             "Family Name",
                                                             "Gender",
                                                             "DOB",
                                                             "City",
                                                             "Phone Number",
                                                             "National ID",
                                                             "Score").toList());
      final Vector<Vector<String>> rowData = new Vector<>();

      MyTableModel(final List<ApiModels.ApiExpandedGoldenRecord> expandedGoldenRecords) {
         expandedGoldenRecords.forEach(x -> {
            rowData.add(getGoldenRecordVector(x));
            x.interactionsWithScore().forEach(y -> rowData.add(getInteractionVector(y)));
         });
      }

      public int getColumnCount() {
         return colNames.size();
      }

      public int getRowCount() {
         return rowData.size();
      }

      @Override
      public String getColumnName(final int col) {
         return colNames.get(col);
      }

      public Object getValueAt(
            final int row,
            final int col) {
         return (rowData.get(row)).get(col);
      }

      @Override
      public Class getColumnClass(final int c) {
         var i = 0;
         while (getValueAt(i, c) == null) {
            i++;
         }
         return getValueAt(i, c).getClass();
      }

      /*
       * Don't need to implement this method unless your table's
       * editable.
       */
/*
      public boolean isCellEditable(int row, int col) {
         //Note that the data/cell address is constant,
         //no matter where the cell appears onscreen.
         if (col < 2) {
            return false;
         } else {
            return true;
         }
      }
*/

      /*
       * Don't need to implement this method unless your table's
       * data can change.
       */
/*
      public void setValueAt(Object value, int row, int col) {
         rowData.get(row).get(col) = value.toString();
         fireTableCellUpdated(row, col);
      }
*/

      private Vector<String> getGoldenRecordVector(final ApiModels.ApiExpandedGoldenRecord expandedGoldenRecord) {
         return Arrays.stream(new String[]{
                            expandedGoldenRecord.goldenRecord().uniqueGoldenRecordData().auxId(),
                            expandedGoldenRecord.goldenRecord().uid(),
                            expandedGoldenRecord.goldenRecord().uniqueGoldenRecordData().auxDateCreated().toString(),
                            expandedGoldenRecord.goldenRecord().demographicData().givenName,
                            expandedGoldenRecord.goldenRecord().demographicData().familyName,
                            expandedGoldenRecord.goldenRecord().demographicData().gender,
                            expandedGoldenRecord.goldenRecord().demographicData().dob,
                            expandedGoldenRecord.goldenRecord().demographicData().city,
                            expandedGoldenRecord.goldenRecord().demographicData().phoneNumber,
                            expandedGoldenRecord.goldenRecord().demographicData().nationalId,
                            null})
                      .collect(Collectors.toCollection(Vector::new));
      }

      private Vector<String> getInteractionVector(final ApiModels.ApiInteractionWithScore interactionWithScore) {
         return Arrays.stream(new String[]{
                            interactionWithScore.interaction().uniqueInteractionData().auxId(),
                            interactionWithScore.interaction().uid(),
                            interactionWithScore.interaction().uniqueInteractionData().auxDateCreated().toString(),                            interactionWithScore.interaction().demographicData().givenName,
                            interactionWithScore.interaction().demographicData().familyName,
                            interactionWithScore.interaction().demographicData().gender,
                            interactionWithScore.interaction().demographicData().dob,
                            interactionWithScore.interaction().demographicData().city,
                            interactionWithScore.interaction().demographicData().phoneNumber,
                            interactionWithScore.interaction().demographicData().nationalId,
                            Float.toString(interactionWithScore.score())})
                      .collect(Collectors.toCollection(Vector::new));
      }

   }

}

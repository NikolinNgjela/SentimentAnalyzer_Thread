/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentanalyzer;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author Nikolin
 */
public class CheckBoxModelListener implements TableModelListener {
    
    private SentimetAnalyzer mainFormControls;
    
    public CheckBoxModelListener(SentimetAnalyzer mainFormControls){
        this.mainFormControls = mainFormControls;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();

        if (column == 2) {
            TableModel model = (TableModel) e.getSource();
            String columnName = model.getColumnName(column);
            
            Object word = model.getValueAt(row, 0);
            Object score = model.getValueAt(row, 1);

            Boolean checked = (Boolean) model.getValueAt(row, column);
            if (checked) {
                System.out.println(word + " with " + score + " " +  ": " + true);
                
                try{
                    this.mainFormControls.plotDataToGraphs_forXYSeriesChart_WordsCount(word.toString(), row);
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
            }else{
                System.out.println(word + " with " + score + " " +  ": " + false);
                
                //JOptionPane.showMessageDialog(null, columnName, "CName", JOptionPane.INFORMATION_MESSAGE);
                
                try{
                    //EACH REMOVAL HAS POINT ALSO THE COLUMNT FROM WHERE TO REMOVE
                    if(columnName.equals("Plot")){
                        this.mainFormControls.removeDataFromGrpah_forXYSeriesChart_WordsCount(row, "words");
                    }
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
            }

            //CLEAN ALL OTHER SELECTIONS
            //this.mainFormControls.checkSelectedCells(word.toString());
            //JOptionPane.showMessageDialog(null, "Row Count: " + this.mainFormControls.tableModelTopTen.getRowCount(), "Rows", JOptionPane.INFORMATION_MESSAGE); 
        }
    }
}

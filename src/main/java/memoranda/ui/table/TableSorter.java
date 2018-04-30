package main.java.memoranda.ui.table;

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel) 
 * and itself implements TableModel. TableSorter does not store or copy 
 * the data in the TableModel, instead it maintains an array of 
 * integers which it keeps the same size as the number of rows in its 
 * model. When the model changes it notifies the sorter that something 
 * has changed eg. "rowsAdded" so that its internal array of integers 
 * can be reallocated. As requests are made of the sorter (like 
 * getValueAt(row, col) it redirects them to its model via the mapping 
 * array. That way the TableSorter appears to hold another copy of the table 
 * with the rows in a different order. The sorting algorthm used is stable 
 * which means that it does not move around rows when its comparison 
 * function returns 0 to denote that they are equivalent. 
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import main.java.memoranda.util.Local;

/*$Id: TableSorter.java,v 1.7 2004/10/07 08:52:32 ivanrise Exp $*/
public class TableSorter extends TableMap {
    int indexes[];
    Vector sortingColumns = new Vector();
    boolean ascending = true;
    int compares;
    int sortBy = 0;

    public TableSorter() {
        indexes = new int[0]; // for consistency
    }

    public TableSorter(TableModel model) {
        setModel(model);
    }

    public void setModel(TableModel model) {
        super.setModel(model);
        reallocateIndexes();
    }

    public int compareRowsByColumn(int row1, int row2, int column) {
        Class type = model.getColumnClass(column);
        TableModel data = model;

        // Check for nulls.

        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        // If both values are null, return 0.
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) { // Define null less than everything.
            return -1;
        } else if (o2 == null) {
            return 1;
        }

        /*
         * We copy all returned values from the getValue call in case an optimised model
         * is reusing one object to return many values. The Number subclasses in the JDK
         * are immutable and so will not be used in this way but other subclasses of
         * Number might want to do this to save space and avoid unnecessary heap
         * allocation.
         */

        if (type.getSuperclass() == java.lang.Integer.class) {
            Integer n1 = (Integer) data.getValueAt(row1, column);
            int i1 = n1.intValue();
            Integer n2 = (Integer) data.getValueAt(row2, column);
            int i2 = n2.intValue();

            return compareNums(i1, i2);
        }

        else if (type.getSuperclass() == java.lang.Number.class) {
            Number n1 = (Number) data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number) data.getValueAt(row2, column);
            double d2 = n2.doubleValue();

            return compareNums(d1, d2);
        } else if (type == java.util.Date.class) {
            Date d1 = (Date) data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date) data.getValueAt(row2, column);
            long n2 = d2.getTime();

            return compareNums(n1, n2);
        } else if (type == String.class) {
            int result;
            if (data.getColumnName(column).equals(Local.getString("Priority"))) {
                Hashtable priority = new Hashtable();
                priority.put(Local.getString("Lowest"), new Integer(1));
                priority.put(Local.getString("Low"), new Integer(2));
                priority.put(Local.getString("Normal"), new Integer(3));
                priority.put(Local.getString("High"), new Integer(4));
                priority.put(Local.getString("Highest"), new Integer(5));

                Integer s1 = (Integer) priority.get((String) data.getValueAt(row1, column));
                Integer s2 = (Integer) priority.get((String) data.getValueAt(row2, column));
                if (s1 == null || s2 == null)
                    return 0;
                result = s1.compareTo(s2);
            } else if (data.getColumnName(column).equals(Local.getString("Status"))) {
                Hashtable priority = new Hashtable();
                priority.put(Local.getString("Completed"), new Integer(1));
                priority.put(Local.getString("Failed"), new Integer(2));
                priority.put(Local.getString("Scheduled"), new Integer(3));
                priority.put(Local.getString("Active"), new Integer(4));
                priority.put(Local.getString("Deadline"), new Integer(5));

                Integer s1 = (Integer) priority.get((String) data.getValueAt(row1, column));
                Integer s2 = (Integer) priority.get((String) data.getValueAt(row2, column));
                if (s1 == null || s2 == null)
                    return 0;
                result = s1.compareTo(s2);
            } else {
                String s1 = (String) data.getValueAt(row1, column);
                String s2 = (String) data.getValueAt(row2, column);
                result = s1.compareTo(s2);
            }

            return compareNums(result, 0);
        } else if (type == Boolean.class) {
            Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();

            if (b1 == b2) {
                return 0;
            } else if (b1) { // Define false < true
                return 1;
            } else {
                return -1;
            }
        } else {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);

            return compareNums(result, 0);
        }
    }

    /**
     * SER316 Standard value returned upon comparing two ints.
     * 
     * @param n1
     * @param n2
     * @return int represent n1 compared to n2
     */

    public int compareNums(int n1, int n2) {
        if (n1 < n2) {
            return -1;
        } else if (n1 > n2) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * SER316 Standard value returned upon comparing two doubles.
     * 
     * @param n1
     * @param n2
     * @return int represent n1 compared to n2
     */
    public int compareNums(double n1, double n2) {
        if (n1 < n2) {
            return -1;
        } else if (n1 > n2) {
            return 1;
        } else {
            return 0;
        }
    }

    public int compare(int row1, int row2) {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++) {
            Integer column = (Integer) sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0) {
                return ascending ? result : -result;
            }
        }
        return 0;
    }

    public void reallocateIndexes() {
        int rowCount = model.getRowCount();

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public void tableChanged(TableModelEvent e) {
        // System.out.println("Sorter: tableChanged");
        reallocateIndexes();

        super.tableChanged(e);
    }

    public void checkModel() {
        if (indexes.length != model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
            setModel(model);
        }
    }

    public void sort(Object sender) {
        checkModel();

        compares = 0;
        // n2sort();
        // qsort(0, indexes.length-1);
        shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
        // System.out.println("Compares: "+compares);
    }

    public void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i + 1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    public void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /*
         * This is an optional short-cut; at each recursive call, check to see if the
         * elements in this subset are already ordered. If so, no further comparisons
         * are needed; the sub-array can just be copied. The array must be copied rather
         * than assigned otherwise sister calls in the recursion might get out of sinc.
         * When the number of elements is three they are partitioned so that the first
         * set, [low, mid), has one element and and the second, [mid, high), has two. We
         * skip the optimisation when the number of elements is three or less as the
         * first compare in the normal merge will produce the same sequence of steps.
         * This optimisation seems to be worthwhile for partially ordered lists but some
         * analysis is needed to find out how the performance drops to Nlog(N) as the
         * initial order diminishes - it may drop very quickly.
         */

        if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.

        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".

    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        return model.getValueAt(indexes[aRow], aColumn);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public void sortByColumn(int column) {
        // sortByColumn(column, true);
        sortByColumn(column, ascending);
    }

    public void sortByColumn(int column, boolean ascending) {
        sortBy = column;
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort(this);
        super.tableChanged(new TableModelEvent(this));
    }

    public int getSortedBy() {
        return sortBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    // There is no-where else to put this.
    // Add a mouse listener to the Table to trigger a table sort
    // when a column heading is clicked in the JTable.
    public void addMouseListenerToHeaderInTable(JTable table) {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {
            boolean ascending = false;

            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    // System.out.println("Sorting ...");
                    // int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
                    // boolean ascending = (shiftPressed == 0);
                    if (column == sortBy)
                        ascending = !ascending;
                    else
                        ascending = true;
                    sorter.sortByColumn(column, ascending);
                    tableView.getTableHeader().updateUI();
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
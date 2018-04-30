package main.java.memoranda;

import java.util.Collection;
import java.util.Vector;

import main.java.memoranda.interfaces.INote;

public class CurrentNote {

	private static INote currentNote = null;
    private static Vector noteListeners = new Vector();

    public static INote get() {
        return currentNote;
    }

    public static void set(INote note, boolean toSaveCurrentNote) {
        noteChanged(note, toSaveCurrentNote);
        currentNote = note;
    }

    public static void reset() {
//    	 set toSave to true to mimic status quo behaviour only. the appropriate setting could be false
        set(null, true);
    }

    public static void addNoteListener(NoteListener nl) {
        noteListeners.add(nl);
    }

    public static Collection getChangeListeners() {
        return noteListeners;
    }

    private static void noteChanged(INote note, boolean toSaveCurrentNote) {
        for (int i = 0; i < noteListeners.size(); i++) {
            ((NoteListener)noteListeners.get(i)).noteChange(note,toSaveCurrentNote);
		}
    }
}

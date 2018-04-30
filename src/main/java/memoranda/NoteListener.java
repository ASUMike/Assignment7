package main.java.memoranda;

import main.java.memoranda.interfaces.INote;

public interface NoteListener {
  void noteChange(INote note, boolean toSaveCurrentNote);
}

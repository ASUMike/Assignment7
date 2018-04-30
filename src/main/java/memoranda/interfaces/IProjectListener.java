package main.java.memoranda.interfaces;

import main.java.memoranda.ProjectData;

/*$Id: ProjectListener.java,v 1.3 2004/01/30 12:17:41 alexeya Exp $*/
public interface IProjectListener {
    
  //TASK 2-2 SMELL BETWEEN CLASSES
  void projectChange(ProjectData projectData);
  void projectWasChanged();
}
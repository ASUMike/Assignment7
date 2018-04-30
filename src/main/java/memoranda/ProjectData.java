package main.java.memoranda;

import main.java.memoranda.interfaces.INoteList;
import main.java.memoranda.interfaces.IProject;
import main.java.memoranda.interfaces.IResourcesList;
import main.java.memoranda.interfaces.ITaskList;
/**
 * TASK 2-2 SMELL BETWEEN CLASSES
 * Data clump
 * 
 * Pulled out these project references to their own class.
 * @author Mike
 */
public class ProjectData {
    private IProject _project = null;
    private ITaskList _tasklist = null;
    private INoteList _notelist = null;
    private IResourcesList _resources = null;
    
    public ProjectData() {
        
    }
    
    public ProjectData(IProject project, ITaskList tasklist, INoteList notelist, IResourcesList resources) {
        _project = project;
        _tasklist = tasklist;
        _notelist = notelist;
        _resources = resources;
    }
    
    //Getters and setters
    public IProject getProject() {
        return _project;
    }
    public void setProject(IProject project) {
        _project = project;
    }
    public ITaskList getTasklist() {
        return _tasklist;
    }
    public void setTasklist(ITaskList tasklist) {
        _tasklist = tasklist;
    }
    public INoteList getNotelist() {
        return _notelist;
    }
    public void setNotelist(INoteList notelist) {
        _notelist = notelist;
    }
    public IResourcesList getResources() {
        return _resources;
    }
    public void setResources(IResourcesList resources) {
        _resources = resources;
    }
}

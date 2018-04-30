/**
 * CurrentProject.java
 * Created on 13.02.2003, 13:16:52 Alex
 * Package: net.sf.memoranda
 *
 * @author Alex V. Alishevskikh, alex@openmechanics.net
 * Copyright (c) 2003 Memoranda Team. http://memoranda.sf.net
 *
 */
package main.java.memoranda;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Vector;

import main.java.memoranda.interfaces.INoteList;
import main.java.memoranda.interfaces.IProject;
import main.java.memoranda.interfaces.IProjectListener;
import main.java.memoranda.interfaces.IResourcesList;
import main.java.memoranda.interfaces.ITaskList;
import main.java.memoranda.ui.AppFrame;
import main.java.memoranda.util.Context;
import main.java.memoranda.util.CurrentStorage;
import main.java.memoranda.util.Storage;

/**
 *
 */
/*$Id: CurrentProject.java,v 1.6 2005/12/01 08:12:26 alexeya Exp $*/
public class CurrentProject {

    //TASK 2-2 SMELL BETWEEN CLASSES
    //projectData is used all throughout this class 
    private static ProjectData projectData = null;
    private static Vector projectListeners = new Vector();

        
    static {
        String prjId = (String)Context.get("LAST_OPENED_PROJECT_ID");
        if (prjId == null) {
            prjId = "__default";
            Context.put("LAST_OPENED_PROJECT_ID", prjId);
        }
        //ProjectManager.init();
        projectData = new ProjectData();
        projectData.setProject(ProjectManager.getProject(prjId));
		
		if (projectData.getProject() == null) {
			// alexeya: Fixed bug with NullPointer when LAST_OPENED_PROJECT_ID
			// references to missing project
			projectData.setProject(ProjectManager.getProject("__default"));
			if (projectData.getProject() == null) 
				projectData.setProject((IProject)ProjectManager.getActiveProjects().get(0));						
            Context.put("LAST_OPENED_PROJECT_ID", projectData.getProject().getID());
			
		}		
		
		projectData.setTasklist(CurrentStorage.get().openTaskList(projectData.getProject()));
		projectData.setNotelist(CurrentStorage.get().openNoteList(projectData.getProject()));
		projectData.setResources(CurrentStorage.get().openResourcesList(projectData.getProject()));
        AppFrame.addExitListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();                                               
            }
        });
    }
        

    public static IProject get() {
        return projectData.getProject();
    }

    public static ITaskList getTaskList() {
            return projectData.getTasklist();
    }

    public static INoteList getNoteList() {
            return projectData.getNotelist();
    }
    
    public static IResourcesList getResourcesList() {
            return projectData.getResources();
    }

    public static void set(IProject project) {
        if (project.getID().equals(projectData.getProject().getID())) return;
        ITaskList newtasklist = CurrentStorage.get().openTaskList(project);
        INoteList newnotelist = CurrentStorage.get().openNoteList(project);
        IResourcesList newresources = CurrentStorage.get().openResourcesList(project);
        notifyListenersBefore(new ProjectData(project, newtasklist, newnotelist, newresources));
        projectData.setProject(project);
        projectData.setTasklist(newtasklist);
        projectData.setNotelist(newnotelist);
        projectData.setResources(newresources);
        notifyListenersAfter();
        Context.put("LAST_OPENED_PROJECT_ID", project.getID());
    }

    public static void addProjectListener(IProjectListener pl) {
        projectListeners.add(pl);
    }

    public static Collection getChangeListeners() {
        return projectListeners;
    }

    private static void notifyListenersBefore(ProjectData projectData) {
        for (int i = 0; i < projectListeners.size(); i++) {
            ((IProjectListener)projectListeners.get(i)).projectChange(projectData);
            /*DEBUGSystem.out.println(projectListeners.get(i));*/
        }
    }
    
    private static void notifyListenersAfter() {
        for (int i = 0; i < projectListeners.size(); i++) {
            ((IProjectListener)projectListeners.get(i)).projectWasChanged();            
        }
    }

    public static void save() {
        Storage storage = CurrentStorage.get();

        storage.storeNoteList(projectData.getNotelist(), projectData.getProject());
        storage.storeTaskList(projectData.getTasklist(), projectData.getProject()); 
        storage.storeResourcesList(projectData.getResources(), projectData.getProject());
        storage.storeProjectManager();
    }
    
    public static void free() {
        projectData.setProject(null);
        projectData.setNotelist(null);
        projectData.setTasklist(null);
        projectData.setResources(null);
    }
}

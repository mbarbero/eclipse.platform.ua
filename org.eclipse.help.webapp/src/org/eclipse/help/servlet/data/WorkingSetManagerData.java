package org.eclipse.help.servlet.data;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.filter.*;
import org.eclipse.help.internal.toc.*;
import org.eclipse.help.servlet.*;

/**
 * This class manages help working sets
 */
public class WorkingSetManagerData extends RequestData {
	private final static int NONE = 0;
	private final static int ADD = 1;
	private final static int REMOVE = 2;
	private final static int EDIT = 3;
	
	private WorkingSetManager wsmgr =
		HelpSystem.getWorkingSetManager(getLocale());

	public WorkingSetManagerData(ServletContext context, HttpServletRequest request) {
		super(context, request);

		switch(getOperation()) {
			case ADD:
				addWorkingSet();
				break;
			case REMOVE:
				removeWorkingSet();
				break;
			case EDIT:
				editWorkingSet();
				break;
			default:
				break;
		}
	}

	public void addWorkingSet() {
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER)
			return;
		String name = request.getParameter("workingSet");
		if (name != null && name.length() > 0) {

			WorkingSet ws = wsmgr.createWorkingSet(name, new IHelpResource[0]);

			String[] books = request.getParameterValues("books");
			if (books == null)
				books = new String[0];

			TocManager tocmgr = HelpSystem.getTocManager();
			for (int i = 0; i < books.length; i++)
				ws.addElement(tocmgr.getToc(books[i], getLocale()));

			wsmgr.addWorkingSet(ws);
		}
	}

	public void removeWorkingSet() {
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER)
			return;
		String name = request.getParameter("workingSet");
		if (name != null && name.length() > 0) {

			WorkingSet ws = wsmgr.getWorkingSet(name);
			if (ws != null)
				wsmgr.removeWorkingSet(ws);
		}
	}

	public void editWorkingSet() {
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER)
			return;
		String name = request.getParameter("workingSet");
		if (name != null && name.length() > 0) {

			WorkingSet ws = wsmgr.getWorkingSet(name);
			if (ws != null) {
				String[] books = request.getParameterValues("books");
				if (books == null)
					books = new String[0];
				IHelpResource[] elements = ws.getElements();
				for (int i = 0; i < elements.length; i++)
					ws.removeElement(elements[i]);
				TocManager tocmgr = HelpSystem.getTocManager();
				for (int i = 0; i < books.length; i++)
					ws.addElement(tocmgr.getToc(books[i], getLocale()));
				wsmgr.workingSetChanged(ws);
			}
		}
	}

	public String[] getWorkingSets() {
		// sanity test for infocenter, but this could not work anyway...
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER)
			return new String[0];

		// this is workbench
		WorkingSet[] workingSets = wsmgr.getWorkingSets();
		String[] sets = new String[workingSets.length];
		for (int i = 0; i < workingSets.length; i++)
			sets[i] = workingSets[i].getName();

		return sets;
	}

	public String getWorkingSetName() {
		String name = request.getParameter("workingSet");
		if (name == null || name.length() == 0)
			name = WebappResources.getString("All", request);
		return name;
	}

	public WorkingSet getWorkingSet() {
		String name = request.getParameter("workingSet");
		if (name != null && name.length() > 0)
			return wsmgr.getWorkingSet(name);
		else
			return null;
	}

	public boolean isCurrentWorkingSet(int i) {
		WorkingSet[] workingSets = wsmgr.getWorkingSets();
		return workingSets[i].getName().equals(request.getParameter("workingSet"));
	}

	private int getOperation() {
		String op = request.getParameter("operation");
		if ("add".equals(op))
			return ADD;
		else if ("remove".equals(op))
			return REMOVE;
		else if ("edit".equals(op))
			return EDIT;
		else
			return NONE;
	}
}

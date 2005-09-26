package org.epic.perleditor.templates.perl;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.editors.text.TextEditor;
import org.epic.core.util.PerlExecutor;
import org.epic.perleditor.templates.ui.LinkedPositionManager;
import org.epic.perleditor.templates.ui.LinkedPositionUI;

/**
 * @author philipp
 */
public class ModuleCompletionHelper  {
	
	private static ModuleCompletionHelper gInstance;
	
	private final String perlCode;
	
	private String[] moduleNames;
	
	public synchronized static ModuleCompletionHelper getInstance() {
		if (gInstance == null) {
			gInstance = new ModuleCompletionHelper();				
		}
		return gInstance;
	}
	
	private ModuleCompletionHelper() {
		super();
		
		StringBuffer sb = new StringBuffer();
		sb.append("use strict;\n");
		sb.append("use File::Find;\n");
		sb.append("\n");
		sb.append("my %foundmods;\n");
		sb.append("\n");
		sb.append("find ( \n");
		sb.append("  sub {\n");
		sb.append("    return if ((/^\\./) || (! -f $File::Find::name) || (! /\\.pm$/));\n");
		sb.append("\n");
		sb.append("    open(MODFILE,$File::Find::name) || return;\n");
		sb.append("    while (<MODFILE>) { \n");
		sb.append("        if (/^\\s*package\\s+(\\S+);/){\n");
		sb.append("         $foundmods{$1} = 1;\n");
		sb.append("         last; \n");
		sb.append("     } \n");
		sb.append("    }  \n");
		sb.append("    close(MODFILE);\n");
		sb.append("  }, @INC\n");
		sb.append(");\n");
		sb.append("\n");
		sb.append("map { print $_ . \"\\n\"; } sort keys %foundmods;\n");
		
		perlCode = sb.toString();
	}
	
	public void scanForModules(TextEditor textEditor) {
		
		ArrayList al = new ArrayList();
		
		PerlExecutor executor = new PerlExecutor(textEditor);
		synchronized (ModuleCompletionHelper.class) { 
			moduleNames = executor.execute(perlCode);
		}
	}
	
	public ICompletionProposal[] getProposals(
			String moduleNameFragment, int documentOffset,
			ITextViewer viewer
	) {
		synchronized (ModuleCompletionHelper.class) {
			ArrayList al = new ArrayList();
			
			for (int loop = 0; loop < moduleNames.length; loop++) {
				String moduleName = moduleNames[loop];
				if ((moduleNameFragment == null) || 
						(moduleNameFragment.equals("")) ||
						(moduleName.startsWith(moduleNameFragment))
				) {
					al.add(
						new ModuleProposal(moduleName, moduleNameFragment, 
										   documentOffset, viewer));
				}
			}
			
			return (ICompletionProposal[]) al.toArray(new ICompletionProposal[0]);
		}
	}
	
	class ModuleProposal implements ICompletionProposal {
		
		private String moduleName;
		private String moduleNameFragment;
		private int documentOffset;
		private ITextViewer fViewer;
		
		public ModuleProposal(String moduleName, String moduleNameFragment, int documentOffset, ITextViewer viewer) {
			this.moduleName = moduleName;
			this.moduleNameFragment = moduleNameFragment;
			this.documentOffset = documentOffset;
			this.fViewer = viewer;
		}
		
		public void apply(IDocument document) {
			String replacementText = moduleName + ";";
			String moriturus = moduleNameFragment;
			int len = moriturus.length();
			
			int start = documentOffset - len;
			
			try {
				document.replace(start, len, replacementText);
				LinkedPositionManager manager = new LinkedPositionManager(document);
				LinkedPositionUI editor = new LinkedPositionUI(fViewer, manager);
				editor.setFinalCaretOffset(replacementText.length() + start);
				editor.enter();
				
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public Point getSelection(IDocument document) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getAdditionalProposalInfo() {		
			
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getDisplayString() {
			return moduleName;
		}
		
		public Image getImage() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public IContextInformation getContextInformation() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getModuleName() {
			return moduleName;
		}
		
	}
	
	public static void main (String[] args) {
		String text = "\nuse X";
		Pattern pattern = Pattern.compile(".*use\\s*(.*)$", Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(text);
		if (matcher.matches()) {
			System.out.println("matches");
			System.out.println(matcher.group(1));
		}
	}
	
}

package cse446mlhw;

import java.io.File;

import javatools.administrative.D;
import javatools.filehandlers.DW;

public class PrintHomework {

	public static void main(String[] args) {
		String dir_submission = "o:/unix/projects/pardosa/s5/clzhang/class/ta_machine_learning/myhw4/hw4submission/";
		DW dw= new DW("o:/unix/projects/pardosa/s5/clzhang/class/ta_machine_learning/myhw4/hw4submission/a.sh");
		
		File Fdir = new File(dir_submission);
		String[] list = Fdir.list();
		for (String s : list) {
			if(s.contains("a.sh")){
				continue;
			}
			String sdir = dir_submission + "/" + s;
			File f0 = new File(sdir);
			boolean findWriteUp = false;
			for (String ss : f0.list()) {
				if(ss.endsWith(".pdf")||ss.endsWith(".docx") || ss.endsWith(".doc")||ss.endsWith(".odt")){
					//D.p(ss);
					String []spt= ss.split("\\.");
					String suffix = spt[spt.length-1];
					//ss = ss.replace(" ", "\\ ");
					dw.write("cp \""+s+"/"+ss+"\" "+"../report/"+s+"."+suffix);
					findWriteUp = true;
				}
			}
			if(!findWriteUp){
				D.p(f0);
			}
		}
		dw.close();
	}
}

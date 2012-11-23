package ibmcontest;

import javatools.administrative.D;
import javatools.filehandlers.DR;

public class Main {

	public static void main(String[] args) {
		String file = "C:/Users/IBM_ADMIN/workspace_systemt/ContestLC/data/sample_unlabeled.del";
		DR dr = new DR(file);
		String[] l;
		String key = "wild ones";
		while ((l = dr.read()) != null) {
			if (l[0].toLowerCase().contains(key)
					&& !l[0].toLowerCase().contains("flo rida")
					) {
				D.p(l);
			}
		}
		dr.close();

	}
}

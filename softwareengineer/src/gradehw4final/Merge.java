package gradehw4final;

import java.util.HashMap;

import javatools.administrative.D;
import javatools.filehandlers.DR;

public class Merge {
	public static void main(String[] args) {
		HashMap<String, String> finalscore = new HashMap<String, String>();
		HashMap<String, String> hw4score = new HashMap<String, String>();
		HashMap<String, String> hw4comment = new HashMap<String, String>();
		{
			DR dr = new DR("final");
			String[] l;
			while ((l = dr.read()) != null) {
				finalscore.put(l[0] + "\t" + l[1], l[2]);
			}
			dr.close();
		}
		{
			DR dr = new DR("hw4");
			String[] l;
			while ((l = dr.read()) != null) {
				hw4score.put(l[0], l[1]);
				hw4comment.put(l[0], l[2]);
			}
			dr.close();
		}
		{
			DR dr = new DR("name");
			String[] l;
			while ((l = dr.read()) != null) {
				String username = l[0];
				String hw4s = "", finals = "", comment = "";
				if (hw4score.containsKey(username)) {
					hw4s = hw4score.get(username);
					finals = finalscore.get(l[1] + "\t" + l[2]);
					comment = hw4comment.get(username);
				}
				D.p(username, hw4s, finals, comment);
			}
			dr.close();
		}
	}

}

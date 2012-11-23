package cse446mlhw;

import java.util.HashSet;

import javatools.administrative.D;
import javatools.filehandlers.DR;

public class CompareFile {
	public static void main(String[] args) {
		String myoutput = "clzhang.output";
		String theiroutput = "test.output";
		HashSet<String> answer = new HashSet<String>();
		{
			DR dr = new DR(myoutput);
			String[] l;
			int writer = 0;
			while ((l = dr.read()) != null) {
				String in = l[0].toLowerCase();

				answer.add(in);
				if(in.contains("writer")){
					writer++;
				}
			}

			D.p(writer);
			dr.close();
		}
		{
			DR dr = new DR(theiroutput);
			String[] l;
			int right = 0;
			int writerRight = 0;
			while ((l = dr.read()) != null) {
				String in = l[0].toLowerCase();
				if(in.contains("data/classifier_data_test/")){
					in = in.replace("data/classifier_data_test/", "");
				}
				if (answer.contains(in)) {
					right++;
					if(in.contains("writer")){
						writerRight++;
					}
				}
			}
			dr.close();
			D.p(right,writerRight);
		}
	}
}

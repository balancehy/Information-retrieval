import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
public class ReadIndex3
{
	static String PATH;
	static String inputfile;
	static String outputfile;
	public ReadIndex3(String _path,  String _outputfile,String _inputfile) // constructor
	{
	//		System.out.println(path + inputfile) ;
		PATH=_path;
		outputfile=_outputfile;
		inputfile=_inputfile;
	}
	public List<ArrayList<String>> readInput(String path,String inputfile)//read input file and store the terms
	{
		List<ArrayList<String>> allline=new ArrayList<ArrayList<String>>();// store each line as arraylist
		File file= new File(inputfile); // create a file , which is the input file
		Scanner sc;
		try
		{
			sc = new Scanner(file);
			while (sc.hasNextLine())
			{
				ArrayList<String> aline = new ArrayList<String>();
				String line = sc.nextLine();// scan nextline, return the skipped line as string array
				String[] words = line.split(" ");// use space as splitter
				for (int i = 0; i < words.length; i++)
				{
					aline.add(words[i]);// store the token in each line into an arraylist
				}
				allline.add(aline);
			}
			sc.close();
			
			/*// print test
			int i=0;
			while (i<allline.size())
			{
				System.out.println((i+1)+" term set is: "+allline.get(i));
				i++;
			}*/
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return allline;		
	}
	public List<List<Object>> getTaatand(List<ArrayList<String>> querys,HashMap<String,LinkedList<Integer>> dictionary)
	{
		List<List<Object>> result = new ArrayList<List<Object>>();
		for (int i=0;i<querys.size();i++)
		{
			int[] pointers= new int[querys.get(i).size()];
			List<LinkedList<Integer>> postings = new ArrayList<LinkedList<Integer>>();
			List<String> terms= new ArrayList<String>();
			int minsizeposting=1000000;// initial size of posting that is used for comparing
			int minsizepointer=-1;// initial pointer that points to the min size posting
			for (int j=0;j<pointers.length;j++) // get term names and their posting lists, j iterates term
			{
				terms.add(querys.get(i).get(j));
				postings.add(dictionary.get(querys.get(i).get(j)));
//				System.out.println("i,j is "+i+" "+j+"\n"+querys.get(i).get(1));
				if (postings.get(j).size()<minsizeposting)// maintain min size of positing and its pointer
				{
					minsizeposting=postings.get(j).size();
					minsizepointer=j;
				}
				System.out.println(terms.get(j)+" " + postings.get(j));
			}
			LinkedList<Integer> tempmerge= new LinkedList<Integer>();
			tempmerge=postings.get(minsizepointer); // start merge from posting list with minimum length
			int totalnumcomp=0;
			for (int j=0;j<pointers.length;j++) // if a query has n term, need n-1 number of merges
			{
				if (j==minsizepointer)// if looped posting list is the same as initial one, skip it
				{
					
				}else
				{
					List<Object> obj = twoMergeAND(postings.get(j),tempmerge);
					tempmerge=(LinkedList<Integer>) obj.get(0);
					totalnumcomp=totalnumcomp+(int)obj.get(1);
				}
			}
			System.out.println("the merged posting is "+tempmerge);
			System.out.println("the number of comparison is "+totalnumcomp);
			List<Object> obj= Arrays.asList(terms,postings,tempmerge,totalnumcomp);
			result.add(obj);
		}
		
		return result;
		
	}
	public List<List<Object>> getTaator(List<ArrayList<String>> querys,HashMap<String,LinkedList<Integer>> dictionary)
	{
		List<List<Object>> result = new ArrayList<List<Object>>();
		for (int i=0;i<querys.size();i++)
		{
			int[] pointers = new int[querys.get(i).size()];
			List<LinkedList<Integer>> postings = new ArrayList<LinkedList<Integer>>();
			List<String> terms = new ArrayList<String>();
			for (int j=0;j<pointers.length;j++) // get term names and their posting lists, j iterates term
			{
				terms.add(querys.get(i).get(j));
				postings.add( dictionary.get(querys.get(i).get(j)));
				System.out.println(terms.get(j)+" " + postings.get(j));
			}
			LinkedList<Integer> tempmerge = new LinkedList<Integer>();
			int totalnumcomp = 0;
			for (int j = 0; j < pointers.length; j++) // if a query has n term, need n-1 number of merges
			{

				List<Object> obj = twoMergeOR(postings.get(j), tempmerge);
				tempmerge = (LinkedList<Integer>) obj.get(0);
				totalnumcomp = totalnumcomp + (int) obj.get(1);

			}
			System.out.println("the merged posting is " + tempmerge);
			System.out.println("the number of comparison is "+totalnumcomp);
			List<Object> obj= Arrays.asList(terms,postings,tempmerge,totalnumcomp);
			result.add(obj);
		}
		return result;
		
	}
	public List<List<Object>> getDaatand(List<ArrayList<String>> querys,HashMap<String,LinkedList<Integer>> dictionary)
	{
		List<List<Object>> result = new ArrayList<List<Object>>();
		for (int i=0;i<querys.size();i++)
		{
			// Get terms and their posting lists
			int[] pointers = new int[querys.get(i).size()];
			List<LinkedList<Integer>> postings = new ArrayList<LinkedList<Integer>>();
			List<String> terms = new ArrayList<String>();
			for (int j=0;j<pointers.length;j++) // get term names and their posting lists, j iterates term
			{
				terms.add(querys.get(i).get(j));
				postings.add( dictionary.get(querys.get(i).get(j)));
				System.out.println(terms.get(j)+" " + postings.get(j));
			}
			// Daat AND begins
			boolean flag_endPosting=false; // the flag of any posting list reaching its end
			int totalnumcomp=0;// number of comparison
			List<Integer> resultlist = new LinkedList<Integer>();// the list containing the retrieved docID
			while (flag_endPosting!=true)
			{
				// find index(s) of terms whose current posting IDs are minimum of all
				List<Integer> minIndex= new ArrayList<Integer>();
				int temp=0; // Initialize index with min ID as 0. j iterates all terms' pointer
				int numcomp=-1;// Initialize number of comparisons for one round of moving pointers. 
				//numcomp=-1 because it needs n-1 comparison for n terms in FORloop, not n
				for (int j=0;j<pointers.length;j++) // loop current IDs of all terms to find the min 
				{
					int ID = postings.get(j).get(pointers[j]);
					if (ID == postings.get(temp).get(pointers[temp])) // jth posting, pointers[j]th ID
					{
						minIndex.add(j);
					}else if (ID < postings.get(temp).get(pointers[temp]))
					{
						minIndex.clear();
						minIndex.add(j);
						temp=j;
					}
					numcomp=numcomp+1;
				}
				int minID= postings.get(minIndex.get(0)).get(pointers[minIndex.get(0)]);// min ID
//				System.out.println("The min ID is "+minID+" The min ID index are "+minIndex);//test print
				if (minIndex.size()==pointers.length)// current IDs of all terms are identical, AND is true
				{
					resultlist.add(minID); // apply AND
				}
				if (minIndex.size()>1)// if the min docIDs are more than one, move pointers one step
				{
					for (int j=0;j<minIndex.size();j++)// advance pointers of minID one step
					{
						pointers[minIndex.get(j)]++;
						
					}
				}else// if there is only one min docID, move pointer depending on situations
				{
					int indexterm = minIndex.get(0);// the index of terms(or postings), 0~n-1, n is number of terms
					//pointers[indexterm] is current pointer value(length), the skip step is sqrt(total length)
					int skipstep=(int) Math.sqrt(postings.get(indexterm).size());
					boolean skip= true;// assume skip is true
					if ((pointers[indexterm]) % skipstep !=0 | (pointers[indexterm]+skipstep)>=postings.get(indexterm).size())
					{
						skip=false;// no skip, if indexterm is not skip point, or the index after skipping is out of bound
					}else
					{
						for (int j=0;j<postings.size();j++)//if it is skip point and not out of bound, loop other posting lists to decide if it can skips
						{
							if (j!=minIndex.get(0))// no need to compare the posting list with min docID with itself. maximum comparison is postingsize-1
							{
								numcomp=numcomp+1;
								if (postings.get(indexterm).get(pointers[indexterm]+skipstep)>postings.get(j).get(pointers[j]))
								{
									skip = false; // if any of current docID in posting list is less than the target, skip is false
									break;
								}
							}
						}
					}
					if (skip==true)
					{
						pointers[indexterm]=pointers[indexterm]+Math.max(skipstep, 1);// if skipstep<1, at least advance pointer one step
					}else
					{
						pointers[indexterm]++;					
					}
				}
				
				for (int j=0;j<pointers.length;j++) // loop pointers of all terms to find if any goes to end of list
				{
					if (pointers[j]>=postings.get(j).size())
					{
						flag_endPosting=true;
						break;
					}
				}
				totalnumcomp=totalnumcomp+numcomp;// update total number of comparisons
			}
			System.out.println("the appended posting is " + resultlist);
			System.out.println("the number of comparison is "+totalnumcomp);
			List<Object> obj= Arrays.asList(terms,postings,resultlist,totalnumcomp);
			result.add(obj);
		}
		return result;
	}
	public List<List<Object>> getDaator(List<ArrayList<String>> querys,HashMap<String,LinkedList<Integer>> dictionary)
	{
		List<List<Object>> result = new ArrayList<List<Object>>();
		for (int i=0;i<querys.size();i++)
		{
			// Get terms and their posting lists
			int[] pointers = new int[querys.get(i).size()];
			List<LinkedList<Integer>> postings = new ArrayList<LinkedList<Integer>>();
			List<String> terms = new ArrayList<String>();
			for (int j = 0; j < pointers.length; j++) // get term names and their posting lists, j iterates term
			{
				terms.add(querys.get(i).get(j));
				postings.add(dictionary.get(querys.get(i).get(j)));
				System.out.println(terms.get(j) + " " + postings.get(j));
			}
			// Daat OR begins
			boolean flag_endPosting=false; // the flag of all posting lists reaching their ends
			int totalnumcomp=0;// number of comparison
			List<Integer> resultlist = new LinkedList<Integer>();
			while (flag_endPosting!=true)
			{
				// find index(s) of terms whose current posting IDs are minimum of all
				List<Integer> minIndex= new ArrayList<Integer>();// record all index of min IDs in a list
				// temp is initialized as the first term index whose posting list pointer does not reach end
				int temp=0; 
				for (int j=0;j<pointers.length;j++)
				{
					if (pointers[j]<postings.get(j).size())
					{
						temp=j;// found the term index and break out the loop
						break;
					}
				}
				int numcomp=-1;// Initialize number of comparisons for one round of moving pointers
				for (int j=0;j<pointers.length;j++) // loop current IDs of all terms to find the min 
				{
					
					if (pointers[j]>=postings.get(j).size())
					{
						
					}else
					{
						int ID = postings.get(j).get(pointers[j]);
						if (ID == postings.get(temp).get(pointers[temp])) // jth posting, pointers[j]th ID
						{
							minIndex.add(j);
						}else if (ID < postings.get(temp).get(pointers[temp]))
						{
							minIndex.clear();
							minIndex.add(j);
							temp=j;
						}
						numcomp=numcomp+1;
					}
				}
				int minID= postings.get(minIndex.get(0)).get(pointers[minIndex.get(0)]);// min ID
				resultlist.add(minID); // apply OR, append the min ID
				for (int j=0;j<minIndex.size();j++)// advance pointers of all min IDs one step
				{
					pointers[minIndex.get(j)]++;// the minIndex(th) posting list may still have docIDs left					
				}
				temp=1;// temp can only be one or zero
				for (int j=0;j<pointers.length;j++) // loop pointers of all terms see if they all reach end
				{
					if (pointers[j]>=postings.get(j).size()) // if one pointer reaches end, temp times 1
					{
						temp=1*temp;
					}else
					{
						temp=0*temp;// if one pointer does not reach end, temp times 0
					}
				}
				if (temp==0) // at least one pointer does not reach end
				{
					flag_endPosting=false;
				}else
				{
					flag_endPosting=true;// all pointer reach end
				}
				totalnumcomp=totalnumcomp+numcomp;// update total number of comparisons
			}
			System.out.println("the appended posting is " + resultlist);
			System.out.println("the number of comparison is "+totalnumcomp);
			List<Object> obj= Arrays.asList(terms,postings,resultlist,totalnumcomp);
			result.add(obj);
		}
		return result;
	}
	public List<Object> twoMergeAND(LinkedList<Integer> p0,LinkedList<Integer> p1)
	{
		LinkedList<Integer> result= new LinkedList<Integer>();
		int numcomp= 0;
		int[] pointers = new int[2];
		int skipstep0=(int) Math.sqrt(p0.size());// fixed skip step and skip point
		int skipstep1=(int) Math.sqrt(p1.size());
		while (pointers[0]<p0.size() & pointers[1]<p1.size())
		{
			if (p0.get(pointers[0])<p1.get(pointers[1]))
			{
				if ((pointers[0]) % skipstep0 !=0 | (pointers[0]+skipstep0)>=p0.size()) // no skip,if not skip point or index after skipping is out of boundary
				{
					pointers[0]++;
				}else
				{
					if (p0.get(pointers[0]+skipstep0)<=p1.get(pointers[1]))// if docID at skipped step is less than the current docID in another list, advance multiple step
					{
						pointers[0]=pointers[0]+Math.max(skipstep0, 1);// at least advance one step, or it will be endless
					}else// otherwise, advance one step
					{
						pointers[0]++;					
					}
					numcomp=numcomp+1;// comparison add one because of checking if it can skip
				}
				numcomp=numcomp+1;// comparison add one because of comparing current IDs
			}
			else if (p0.get(pointers[0])>p1.get(pointers[1]))//check if skip happens on the other list
			{
				if ((pointers[1]) % skipstep1 !=0 | (pointers[1]+skipstep1)>=p1.size())
				{
					pointers[1]++;
				}else
				{
					if (p0.get(pointers[0])>=p1.get(pointers[1]+skipstep1))
					{
						pointers[1]=pointers[1]+Math.max(skipstep1, 1);
					}else
					{
						pointers[1]++;					
					}
					numcomp=numcomp+1;// comparison add one because of checking if it can skip
				}
				numcomp=numcomp+1;
			}
			else
			{
				result.add(p0.get(pointers[0]));
				pointers[0]++;
				pointers[1]++;
				numcomp=numcomp+1;
			}
			
		}
		
		return Arrays.asList(result,numcomp);// store merged list and number of comparison as an object list
	}
	public List<Object> twoMergeOR(LinkedList<Integer> p0,LinkedList<Integer> p1)
	{
		LinkedList<Integer> result= new LinkedList<Integer>();
		int numcomp= 0;
		int[] pointers = new int[2];
		while (pointers[0]<p0.size() | pointers[1]<p1.size())
		{
			if (pointers[0]<p0.size() & pointers[1]<p1.size())
			{
				if (p0.get(pointers[0])<p1.get(pointers[1]))
				{
					result.add(p0.get(pointers[0]));
					pointers[0]++;
					numcomp=numcomp+1;
				}
				else if (p0.get(pointers[0])>p1.get(pointers[1]))
				{
					result.add(p1.get(pointers[1]));
					pointers[1]++;
					numcomp=numcomp+1;
				}
				else
				{
					result.add(p0.get(pointers[0]));
					pointers[0]++;
					pointers[1]++;
					numcomp=numcomp+1;
				}
			}else if (pointers[0]>=p0.size() & pointers[1]<p1.size())
			{
				result.add(p1.get(pointers[1]));
				pointers[1]++;
			}else if (pointers[0]<p0.size() & pointers[1]>=p1.size())
			{
				result.add(p0.get(pointers[0]));
				pointers[0]++;
			}
		}
		return Arrays.asList(result,numcomp);
	}
	public ArrayList<String> getFieldname(IndexReader reader) throws IOException
	{	
		ArrayList<String> field_name=new ArrayList<String>();
		Fields field=MultiFields.getFields(reader);// using MultiFields.getFields is a slow way(from lucence document)
		for (Iterator<String> field_iter=field.iterator();field_iter.hasNext();)
		{
			field_name.add(field_iter.next());
		}
		System.out.println(field_name);
		return field_name;
	}
	public void getIndexinfo(IndexReader reader) throws IOException
	{
		Fields field= MultiFields.getFields(reader);
		System.out.println("The number of documnet contained in this index is: "+reader.numDocs());
		int numterms=0;// initialize total number of terms in the index
		for (Iterator<String> field_iter=field.iterator();field_iter.hasNext();)
		{
			String field_name=field_iter.next();// Remember: variables defined in loop cannot be used directly outside loop
			Terms term=field.terms(field_name);
			TermsEnum termenum=term.iterator();
			int count=0;// initialize total number of terms in this field
			if (field_name.equals("id"))
			{
				System.out.println(field_name+" :Not counting the number of terms in this field");
			}else
			{
				while(termenum.next()!=null) // iterate all terms in this field and count the total number
				{
					count++;
				}
				System.out.println(field_name+" :the number of terms in this field is "+count);
				numterms=count+numterms;// add total number of terms in this field to the total number of terms in the index
				
			}
		}
		System.out.println("The number of terms in the index is :"+numterms);
		
	}
	public HashMap<String,LinkedList<Integer>> getPosting(IndexReader reader) throws IOException
	{
		Fields field= MultiFields.getFields(reader);
		HashMap<String,LinkedList<Integer>> dictionary = new HashMap<String, LinkedList<Integer>>();
		for (Iterator<String> field_iter=field.iterator();field_iter.hasNext();)// field should be accessed by iterator
		{
			String field_name=field_iter.next();
			if (field_name.equals("id"))
			{
//				System.out.println("Do not print the terms in field "+field_name);
			}else
			{
				TermsEnum term_iter=MultiFields.getTerms(reader, field_name).iterator();/* iterate 
			all terms for a field  */
				LinkedList<Integer> postings = new LinkedList<Integer>();
				while (term_iter.next()!=null)
				{
					BytesRef term_name=term_iter.term();// term_name is not utf8 code
					PostingsEnum post=term_iter.postings(null);
					if (dictionary.get(term_name.utf8ToString())==null)
					{
						dictionary.put(term_name.utf8ToString(), new LinkedList<Integer>());
					}
					while (post.nextDoc()!=PostingsEnum.NO_MORE_DOCS)
					{
						dictionary.get(term_name.utf8ToString()).add(post.docID());
					}
				}
				
			}
						
		}
//		System.out.println("The size of the dictionary is "+dictionary.size());
		
		return dictionary;
	}
	public LinkedList<Integer> getQueryPosting(HashMap<String,LinkedList<Integer>> dictionary,String queryterms)
	{
		LinkedList<Integer> queryposting=dictionary.get(queryterms);
		System.out.println(queryterms+"\n"+"Postings list: "+queryposting);
		return queryposting;
	}
	public String getOutputdata(List<List<List<Object>>> allresult)// result after one operation, i.e. taatand, daator
	{
		String data="";
		for (int i=0;i<allresult.get(0).size();i++)
		{
			List<Object> resTaatAnd=allresult.get(0).get(i);// TaatAnd, query 0
			List<Object> resTaatOr=allresult.get(1).get(i);// TaatOr, query 0
			List<Object> resDaatAnd=allresult.get(2).get(i);// TaatOr, query 0
			List<Object> resDaatOr=allresult.get(3).get(i);// TaatOr, query 0
			
			List<String> terms = (List<String>) resTaatAnd.get(0);
			List<LinkedList<Integer>> postings = (List<LinkedList<Integer>>) resTaatAnd.get(1);
			List<Integer> resultlist= (List<Integer>) resTaatAnd.get(2);
			int numcomp = (int) resTaatAnd.get(3);
			String headerpost = getHeader(terms,postings);// get headers of posting lists
			String headerOper= getHeaderOper(terms,resultlist,numcomp,"TaatAnd");
			data =data + headerpost + headerOper;
			
			terms = (List<String>) resTaatOr.get(0);
			postings = (List<LinkedList<Integer>>) resTaatOr.get(1);
			resultlist= (List<Integer>) resTaatOr.get(2);
			numcomp = (int) resTaatOr.get(3);
			headerOper= getHeaderOper(terms,resultlist,numcomp,"TaatOr");
			data =data + headerOper;
			
			terms = (List<String>) resDaatAnd.get(0);
			postings = (List<LinkedList<Integer>>) resDaatAnd.get(1);
			resultlist= (List<Integer>) resDaatAnd.get(2);
			numcomp = (int) resDaatAnd.get(3);
			headerOper= getHeaderOper(terms,resultlist,numcomp,"DaatAnd");
			data =data + headerOper;
			
			terms = (List<String>) resDaatOr.get(0);
			postings = (List<LinkedList<Integer>>) resDaatOr.get(1);
			resultlist= (List<Integer>) resDaatOr.get(2);
			numcomp = (int) resDaatOr.get(3);
			headerOper= getHeaderOper(terms,resultlist,numcomp,"DaatOr");
			data =data + headerOper;
//			System.out.println(data);
		}
		
		return data;
	}
	public void writeResult(String data)
	{
		File file= new File(outputfile);
		
		FileWriter fr =null;
		try
		{
			fr = new FileWriter(file);
			fr.write(data);
		} catch (IOException e)
		{
			e.printStackTrace();
		}finally
		{
			try
			{
				fr.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	public String getHeader(List<String> terms,List<LinkedList<Integer>> postings)
	{
		String data = "";
		String header="GetPostings\n";
		String termposting="";
		for (int i=0;i<terms.size();i++)// list "get posting", terms and their posting lists
		{
			String p ="";
			for (int ii=0;ii<postings.get(i).size();ii++)// get posting list string
			{
				p = p+" "+postings.get(i).get(ii);
			}
			termposting= terms.get(i)+"\nPosting list:"+p+"\n";
			data= data+ header + termposting;
		}
		return data;

	}
	public String getHeaderOper(List<String> terms,List<Integer> resultlist,int numcomp,String oper)
	{
		String header2=oper+"\n";
		for (String s : terms)
		{
			header2 = header2 + s + " ";
		}
		header2 = header2+ "\n";
		header2= header2 +"Results: ";
		if (resultlist.isEmpty())
		{
			header2 = header2 + "empty";
		}else
		{
			for (Integer s : resultlist)
			{
				header2 = header2 +s +" ";
			}
		}
		header2 = header2 + "\n";// add new line
		header2 = header2 + "Number of documents in results: "+resultlist.size()+"\n";
		header2 = header2 + "Number of comparisons: "+numcomp+"\n";
		return header2;
	}
	public static void main(String arg[]) {
		
		ReadIndex3 testReader = new ReadIndex3(arg[0],arg[1],arg[2]);// create an instance of class with arguments path, output.txt. input.txt
		try
		{
			Directory dir = FSDirectory.open(Paths.get(PATH));// set directory path for reading index
			IndexReader reader=DirectoryReader.open(dir);// set index reader, which is directoryReader
			HashMap<String,LinkedList<Integer>> pl=testReader.getPosting(reader);// get all the posting lists and store them in hashmap
			List<ArrayList<String>> queryterms= testReader.readInput(ReadIndex3.PATH, ReadIndex3.inputfile);// read query terms from the given input file
			// Get operation results
			List<List<Object>> TaatAnd = testReader.getTaatand(queryterms, pl);
			List<List<Object>> TaatOr= testReader.getTaator(queryterms, pl);
			List<List<Object>> DaatAnd = testReader.getDaatand(queryterms, pl);
			List<List<Object>> DaatOr = testReader.getDaator(queryterms, pl);
			//Combine the results
			List<List<List<Object>>> allresult= Arrays.asList(TaatAnd,TaatOr,DaatAnd,DaatOr);
			String data = testReader.getOutputdata(allresult);// generate String w.r.t. operation results
			testReader.writeResult(data);// write the String data into output file
			
//			testReader.getQueryPosting(pl, "a");
//			testReader.getFieldname(reader);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
//LinkedList<Integer> a = new LinkedList<Integer>();
//LinkedList<Integer> b = new LinkedList<Integer>();
//a.add(1);
//a.add(4);
//a.add(7);
//a.add(10);
//b.add(2);
//b.add(4);
//b.add(10);
//List<Object> forprint = testReader.twoMergeOR(a, b);
//System.out.println("first list is "+a);
//System.out.println("second list is "+b);
//System.out.println("result list is "+forprint.get(0)+"\nnumber of comparison is "+forprint.get(1));

/*	List<LeafReaderContext> reader2=reader.leaves();
Iterator<FieldInfo> kk=reader2.get(0).reader().getFieldInfos().iterator();
while (kk.hasNext())
{
	System.out.println(kk.next().name);
}*/
package utilities;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.FPTA;
import optimization.Frontier;


public class LateXReportGen {

	List<Point1> points;

	HashMap<String,String> reference;
	public LateXReportGen() {
		points = new ArrayList<Point1>();
		reference = new HashMap<String, String>();
		reference.put("PSO", "James Kennedy, Russell C. Eberhart: \\emph{Particle swarm optimization}. In: ICNN (1995)\n");
		reference.put("WO","Seyedali Mirjalili, Andrew Lewis: \\emph{The whale optimization algorithm}. Advances in Engineering Software 95, 51–67 (2016)\n");
		reference.put("CS","Xin-She Yang, Suash Deb: \\emph{Cuckoo search via Lévy flights}. In: World Congress on Nature & Biologically Inspired Computing (NaBIC), pp. 210–214 (2009)\n");
		reference.put("DE","Rainer Martin Storn, Kenneth Price: \\emph{Differential evolution - A simple and efficient adaptive scheme for global optimization over continuous spaces}. Tech. Rep. TR-95-012, International Computer Science Institute, 1947 Center Street, Berkeley (1995)\n");
		reference.put("FA","Xin-She Yang: \\emph{Nature-Inspired Metaheuristic Algorithms (2008)}.\n");
		reference.put("GSA","\\emph{A multi-objective gravitational search algorithm}. In: CICN, pp. 7–12 (2010)\n");
		reference.put("ACO","Marco Dorigo, Mauro Birattari, Thomas Stutzle: \\emph{Ant colony optimization}. IEEE Comput. Intell. Mag. 1(4), 28–39 (2006)\n");
		reference.put("SOS","Absalom E. Ezugwu, Doddy Prayogo: \\emph{Symbiotic organisms search algorithm: Theory, recent advances and applications}. Expert Syst. Appl. 119, 184–209 (2019)\n");	 
		reference.put("GEN", "Hanan Alkhammash, Artem Polyvyanyy, Alistair Moffat: \\emph{Stochastic directly-follows process discovery using grammatical inference}. In: CAiSE, LNCS, vol. 14663, pp. 87–103 (2024))\n");
		reference.put("DFvM","Sander J.J. Leemans, Erik Poppe, Moe T. Wynn: \\emph{Directly Follows-Based Process Mining: Exploration \\& a Case Study}. In: ICPM, pp. 25–32 (2019)\n");
		reference.put("fed","Hootan Zhian, Rajkumar Buyya, Artem Polyvyanyy: \\emph{Federated Sthocastic Process Discovery Using Gramatical Inference}. In: CAiSE, pp. 25–32 (2025)\n");
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public void addDFFAlist(List<Point1> flist)
	{
		points.addAll(flist);
	}
	public void clearDominatedPoints(List<Frontier> list) {
		
		List<Frontier> removelist = new ArrayList<Frontier>();
		
		for(Frontier f:list)
		{
			for(Frontier f1:list)
				if(f.checkDominated(f1))
				{
					removelist.add(f);
					
					break;
				}
		}
		list.removeAll(removelist);
	}
	
	public void writeDFGModel(File file,FPTA fpta) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write("{\n");
			fw.write("  \"initialState\": I, \n");
			fw.write("  \"transitions\": [");
			boolean flag=false;
			DecimalFormat df = new DecimalFormat("0.000");
			for(String state:fpta.states)
			{
				for(String symbol:fpta.alphabet)
				{
					if(fpta.getTransitionFunction().containsKey(state+symbol))
					{
						String target= fpta.getTransitionFunction().get(state+symbol);
						double frequency =Double.parseDouble(df.format(fpta.getTransitionFrequencies().get(state).get(symbol).get(target)));
						String source =state;
						String destination =target;
						if(state.compareTo("")==0)
							source="I";
						if(destination.compareTo("")==0)
							destination="I";
						if(flag)
						{
							fw.write(",\n");
						}
						else
						{
							fw.write("\n");
							flag=true;
							
						}
						fw.write("  {\"from\":"+source+",\"to\":"+destination+",\"label\":\""+symbol+"\",\"frequency\":"+frequency+"}");
						
					}
				}	
			}

			
			fw.write("\n  ]\n}");
			fw.close();
			fw.close();
		}catch (IOException e) {
	// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeSDAGModel(File file,FPTA fpta) {
		try {
			FileWriter fw = new FileWriter(file);
			
					fw.write("{\n");
			fw.write("  \"initialState\": I,\n");
			fw.write("  \"nodes\": [");
			boolean flag=false;
			for(String state:fpta.states)
			{
				String source = state;
				if(state.compareTo("")==0)
					source="I";
				if(flag)
				{
					fw.write(",\n");
				}
				else
				{
					fw.write("\n");
					flag=true;
					
				}
				fw.write("  { \"label\":\""+source.charAt(0)+"\", \"id\": \""+source+"\"}");
			}
			fw.write("\n  ],\n");
			fw.write("  \"transitions\": [");
			flag=false;
			for(String state:fpta.states)
			{
				for(String symbol:fpta.alphabet)
				{
					if(fpta.getTransitionFunction().containsKey(state+symbol))
					{
						String target= fpta.getTransitionFunction().get(state+symbol);
						double frequency = fpta.getTransitionFrequencies().get(state).get(symbol).get(target);
						String source =state;
						String destination =target;
						if(state.compareTo("")==0)
							source="I";
						if(destination.compareTo("")==0)
							destination="I";
						if(flag)
						{
							fw.write(",\n");
						}
						else
						{
							fw.write("\n");
							flag=true;
							
						}
						fw.write("  {\"from\":"+source+",\"to\":"+destination+",\"label\":\""+symbol+"\",\"frequency\":"+frequency+"}");
						
					}
				}	
			}

			
			fw.write("\n  ]\n}");
			fw.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeDFFAModel(File file,FPTA fpta) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write("{\n");
			fw.write("  \"initialState\": λ,\n");
			fw.write("  \"nodes\": [");
			boolean flag = false;
			for(String state:fpta.states)
			{
				String source =state;
				if(state.compareTo("")==0)
					source="λ";
				double finalFreq = fpta.getFinalFrequency(state);
				double iniFreq = fpta.getInitialFrequencies().get(state)!=null?fpta.getInitialFrequencies().get(state):0;
				if(flag)
				{
					fw.write(",\n");
				}
				else
				{
					fw.write("\n");
					flag=true;
					
				}
				fw.write("  { \"label\":\""+source+"\", \"initial frequency\": "+iniFreq+", \"final frequency\": "+finalFreq+"}");
			}
			fw.write("\n  ],\n");
			fw.write("  \"transitions\": [");
			flag=false;
			for(String state:fpta.states)
			{
				for(String symbol:fpta.alphabet)
				{
					if(fpta.getTransitionFunction().containsKey(state+symbol))
					{
						String target= fpta.getTransitionFunction().get(state+symbol);
						double frequency = fpta.getTransitionFrequencies().get(state).get(symbol).get(target);
						String source =state;
						String destination =target;
						if(state.compareTo("")==0)
							source="λ";
						if(destination.compareTo("")==0)
							destination="λ";
						if(flag)
						{
							fw.write(",\n");
						}
						else
						{
							fw.write("\n");
							flag=true;
							
						}
						fw.write("  {\"from\":"+source+",\"to\":"+destination+",\"label\":\""+symbol+"\",\"frequency\":"+frequency+"}");
						
					}
				}	
			}

			
			fw.write("\n  ]\n}");
				//	+ ""
					//+ ""
					//+ "\n}");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public HashMap<String,Integer> countDominance(List<String> algorithms){
		HashMap<String,Integer> res = new HashMap<String, Integer>();
		for(String s:algorithms)
			res.put(s, 0);
		for(Point1 f:points)
			if(res.containsKey(f.name))
				res.put(f.name, res.get(f.name)+1);
			else
				res.put(f.name, 1);
		return res;
	}
	public void generateReports(String eventLogName,HashMap<String,Double> algorithms,HashMap<String,String> parms) {
		//clearDominatedPoints();
		eventLogName = eventLogName.replaceAll("\\_", "\\\\_");
		double y_min = Double.MAX_VALUE;
		double y_max=Double.MIN_VALUE;
		double x_min = Double.MAX_VALUE;
		double x_max=Double.MIN_VALUE;
	
		for(Point1 f:points)
		{
			if(f!=null)
			{
				if(f.er>x_max)
					x_max = f.er;
				if(f.er<x_min)
					x_min = f.er;
				if(f.size>y_max)
					y_max = f.size;
				if(f.size<y_min)
					y_min = f.size;
			}
		}
		
		try {
			
			FileWriter myWriter = new FileWriter("report.tex");	
			myWriter.write("\\documentclass{article}\n");
			myWriter.write("\\setlength{\\paperheight}{232.8mm}\n");
			myWriter.write("\\setlength{\\paperwidth}{151.5mm}\n");
			myWriter.write("\\setlength\\voffset{-26mm}\n");
			myWriter.write("\\setlength\\hoffset{-32mm}\n\n");
			myWriter.write("\\bibliographystyle{plain}\n\n");
			myWriter.write("\\usepackage{pgfplots}\n");
			myWriter.write("\\usepgfplotslibrary{groupplots}\n");
			myWriter.write("\\pgfplotsset{compat=1.18}\n");
			myWriter.write("\\usepackage{longtable}\n");
			myWriter.write("\\usepackage{hyperref}\n");
			myWriter.write("\\usepackage{datetime}\n");
			myWriter.write("\\usepackage[capitalise,nameinlink]{cleveref}\n");
			myWriter.write("\\usepackage{booktabs}\n");
			myWriter.write("\\usepackage{caption}\n");
			myWriter.write("\\usepackage{subcaption}\n");
			myWriter.write("\\usepackage{ragged2e}\n");
			myWriter.write("\\begin{document}\n");
			myWriter.write("\\date{Compiled at {\\ampmtime} on {\\today}}\n");
			myWriter.write("\\title{Sharing Pearls, Not Pebbles Privacy-Preserving Federated Stochastic Process Discovery via Partial Model Disclosure}\n");
			myWriter.write("%\\author{}\n");
			myWriter.write("\\maketitle\n");
			myWriter.write("\\section{Introduction}\n");

			myWriter.write("This report summarizes the setup and results of a process discovery experiment conducted using the tool prepared for the paper titled “Sharing Pearls, Not Pebbles: Privacy-Preserving Federated Stochastic Process Discovery via Partial Model Disclosure.” \n");
			//myWriter.write("tool~\\cite{CITE-TOOL-PAPER}.\n");
			myWriter.write("The following section outlines the discovery techniques used and their configurations in the experiment.\n");
			myWriter.write("\\Cref{sec:quality} specifies the evaluation criteria used to assess the quality of the discovered models and to compare the performance of the discovery techniques.\n");
			myWriter.write("Finally, \\cref{sec:results} presents the experimental results.\n");
			myWriter.write("\\section{Discovery}\n");

			myWriter.write("The command line used to execute the experiment is listed below:\n");

			//myWriter.write("\\begin{verbatim}\n");
			String command="\\begin{justify}\n -mspd -el \""+parms.get("LOG_DIRECTORY").replaceAll("\\_", "\\\\_")+"\" ";
			int i=1;
	
	
			for(String s:algorithms.keySet())
				command+=" m"+(i++)+" ";
			command+="-p "+parms.get("POPULATION")+" ";
			command+="-mxItr "+parms.get("MAX_GENERATION")+" ";
			command+="-t "+ parms.get("TIME_LIMITATION")+" ";
			command+="-mms "+ parms.get("Maximum Model Size")+" ";
			command+="-pfs "+parms.get("PARETO_LIST_SIZE")+" ";
			command+="-ebrm "+parms.get("Entropic Relevance Background Model")+" ";
			command+="- optm "+parms.get("Optimal Model")+" \n\\end{justify}";
			
			myWriter.write(command);
			//myWriter.write("TODO: command line\n");
			//myWriter.write("\\end{verbatim}\n");
			myWriter.write("\\noindent\n");
			myWriter.write("\\Cref{tbl:experiment} summarizes the key characteristics of the experimental setup used for performance comparison.\n");

			
		/*	myWriter.write("\\documentclass{article}\n");
			myWriter.write("\\usepackage{pgfplots}\n");
			myWriter.write("\\usepgfplotslibrary{groupplots}\n");
			myWriter.write("\\usepackage{hyperref}\n");
			myWriter.write("\\begin{document}\n");
			myWriter.write("\\title{Report}\n");
			myWriter.write("\\section{Performance Evaluation}\n");
			myWriter.write("Performance is evaluated using two metrics: dominance count and Diversity Comparison\r\n"
					+ "Indicator (DCI) [1]. Dominance count measures how many solutions from\r\n"
					+ "one algorithm dominate solutions from the other algorithms in the global Pareto front\r\n"
					+ "all solutions from all algorithms, while DCI quantifies solution diversity by analyzing\r\n"
					+ "their distribution in the objective space. To compare the performance of the discovery.\r\n"
					+ "Table 1 summarizes the characteristics of the experiment.\n");*/
		
			
			List<Point1>list= new ArrayList<Point1>();
			list.addAll(points);
			List<Point1> noFilterList = new ArrayList<Point1>();
			noFilterList.addAll(list);
			generateConfigTable(myWriter,parms,eventLogName,algorithms);
			generateLatexPlots(myWriter,eventLogName,algorithms);
			generateDominantCountTable(myWriter,eventLogName,algorithms,list);	
			generateDCITable(myWriter,eventLogName,algorithms,Integer.parseInt(parms.get("DCI_SIZE")),noFilterList);
			generateExecutionTimeTable(myWriter, eventLogName, algorithms);
			myWriter.write("\\begin{thebibliography}{9}\n");
			myWriter.write("\\bibitem{zhian2025B}\n");
			myWriter.write("Hootan Zhian, Rajkumar Buyya, and Artem Polyvyanyy: \\emph{Multi-Objective Metaheuristics for Effective and Efficient Stochastic Process Discovery}. Proceedings of the 23rd International Conference on Business Process Management (BPM) Seville, Spain, August 31–September 5, 2025.");
			myWriter.write("\\bibitem{AlkhammashPMG22}\n");
			myWriter.write("Hanan Alkhammash, Artem Polyvyanyy, Alistair Moffat, Luciano Garc{\\'{\\i}}a{-}Ba{\\~{n}}uelos:");
			myWriter.write("\\emph{Stochastic Directly-Follows Process Discovery Using Grammatical Inference}.");
			myWriter.write("Inf. Syst. 107: 101922 (2022)\n");
			myWriter.write("\\bibitem{Hanan2024}\n");
			myWriter.write("Hanan Alkhammash, Artem Polyvyanyy, Alistair Moffat: \\emph{Stochastic Directly-Follows Process Discovery Using Grammatical Inference}.  In: CAiSE, LNCS, vol. 14663, pp. 87–103, Springer (2024).");
			
			myWriter.write("\\bibitem{zhian2025C}\n");
			myWriter.write("Hootan Zhian, Rajkumar Buyya, and Artem Polyvyanyy: \\emph{Federated Stochastic Process Discovery Using Gramatical Inference}.  In: CAiSE, LNCS, vol. 15702, pp. 76–93, Springer (2025).");
			
			myWriter.write("\\bibitem{zhian2026D}\n");
			myWriter.write("Hootan Zhian, Rajkumar Buyya, Artem Polyvyanyy: \\emph{Sharing Pearls, Not Pebbles: Privacy-Preserving Federated Stochastic Process Discovery via Partial Model Disclosure}. Under review.\n");

			myWriter.write("\\end{thebibliography}\n");
			myWriter.write("\\appendix\n");
			myWriter.write("\\end{document}");
		
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void generateDominantCountTable(FileWriter myWriter, String eventLogName,HashMap<String,Double>algorithms, List<Point1> list) throws IOException {
		
		
		
	}
	
	public void generateAppendix(FileWriter myWriter,List<Frontier> list,boolean flag)throws IOException
	{
		
		DecimalFormat df = new DecimalFormat("0.000");
/*		myWriter.write("\\begin{table}[h!]\n");
		myWriter.write("\\centering\n");
		myWriter.write("\\caption{Model characteristics: size and entropic relevance}\n");
		myWriter.write("\\label{tbl:models}\n");*/
	
		
		myWriter.write("\\begin{longtable}{llllrr}\n");
		myWriter.write("\\caption{Model characteristics: size and entropic relevance}\n");
		myWriter.write("\\setlength{\\tabcolsep}{6pt}\n");
		myWriter.write("\\fontsize{8}{10}\\selectfont\n");
		myWriter.write("\\label{tbl:models} \\\\ \n");
		myWriter.write("\\toprule\n");
		myWriter.write("Method & Type & Name & Visualization & Size & Entropic relevance \\\\ \n");
		myWriter.write("\\endfirsthead\n");
		myWriter.write("\\midrule\n");

		myWriter.write("\\toprule\n");
		myWriter.write("Method & Type & Name & Visualization & Size & Entropic relevance \\\\ \n");
		myWriter.write("\\midrule\n");
		myWriter.write("\\endhead\n");
		
	/*	myWriter.write("\\begin{tabular}{llllrr}\n");
		myWriter.write("\\toprule\n");
		myWriter.write("Method & Type & Name & Visualization & Size & Entropic relevance \\\\\n");
		myWriter.write("\\midrule\n");*/
		HashMap<String,Integer> count = new HashMap<String, Integer>();
		for(Frontier f:list)
		{
			myWriter.write(f.getName()+"&"+f.getType().substring(0,f.getType().indexOf("-"))+" &"+ "\\href{"+f.getPath()+"}{"+f.getType().substring(f.getType().indexOf("-")+1)+"} & x &"+f.getFitness()[1]+"&"+df.format(f.getFitness()[0])+"\\\\\n");
		}
		//		DE & SDAG & model1.sdag & model1.sdag.png & 123 & 12.345 \\
		//		DE & DFFA & model1.dffa & model1.dffa.png & 345 & 15.678 \\
		myWriter.write("\\bottomrule\n");
		myWriter.write("\\end{longtable}\n");
		
	}
	public void generateConfigTable(FileWriter myWriter,HashMap<String,String> parms,String logname,HashMap<String,Double>algorithms) throws IOException{
		
		myWriter.write("\\begin{table}[h!] \n");
		myWriter.write("\\centering \n");
		myWriter.write("\\caption{Characteristics of the experiment} \n");
		myWriter.write("\\label{tbl:experiment} \n");
		myWriter.write("\\setlength{\\tabcolsep}{6pt} % Adjust column spacing \n");
		myWriter.write("\\fontsize{9}{11}\\selectfont \n");
		myWriter.write("\\begin{tabular}{@{}lr@{}} \n");
		myWriter.write("\\toprule \n");
		myWriter.write("\\textbf{Parameter} & \\textbf{Value} \\\\ \n");
		myWriter.write("\\midrule\n");
		myWriter.write("Population size &"+parms.get("POPULATION")+"\\\\ \n");
		myWriter.write("Iterations &"+parms.get("MAX_GENERATION")+"\\\\ \n");
		myWriter.write("Time limit (s) &"+parms.get("TIME_LIMITATION")+"\\\\ \n");
		myWriter.write("Pareto front size &"+parms.get("PARETO_LIST_SIZE")+"\\\\ \n");
		myWriter.write("Maximum model size &"+parms.get("Maximum Model Size")+"\\\\ \n");
		myWriter.write("Optimized models &"+parms.get("Optimal Model")+"\\\\ \n");
		myWriter.write("Entropic relevance background model &"+parms.get("Entropic Relevance Background Model")+"\\\\ \n");
		myWriter.write("Event log & \\texttt{"+logname+"} \\\\ \n");
		DecimalFormat df = new DecimalFormat("0.000");
		myWriter.write("Fitness coefficient $\\alpha$ &"+parms.get("cof")+"\\\\ \n");
		myWriter.write("Fitness Fuction &"+"$\\frac{\\alpha}{Er}+"+"\\frac{1-\\alpha}{Size}$\\\\ \n");
		myWriter.write("Entropic relevance degradation threshold (epsilon) &"+parms.get("epsilon")+"\\\\ \n");
		myWriter.write("\\bottomrule \n");
		myWriter.write("\\end{tabular}\n");
		myWriter.write("\\end{table} \n");
		myWriter.write("\\noindent \n");

		myWriter.write("\nThese discovery techniques are used in the experiment:\n");

		myWriter.write("\\begin{itemize}\n");
		myWriter.write("\\item FedGASPD ~\\cite{zhian2025C};\n");
			myWriter.write("\\item P2FedGASPD;\n");
		myWriter.write("\\end{itemize}\n");	
		myWriter.write("\\section{Quality}\n");
		myWriter.write("\\label{sec:quality}\n");
		myWriter.write("The quality of the discovered models is assessed using three criteria: \\emph{entropic relevance}, which captures stochastic precision and recall~\\cite{AlkhammashPMG22}, model \\emph{size}, defined as the number of nodes and edges, which reflects model simplicity, and \\emph{privacy} which is presented in ~\\cite{zhian2026D}.\n\n");
		myWriter.write("\\section{Results}\n");
		myWriter.write("\\label{sec:results}\n");
		myWriter.write("\\Cref{fig:quality} presents the discovered deterministic frequency finite automata (DFFA) models with respect to entropic relevance and model size, where lower values are preferred for both criteria.\n");	
	}
	
	public void generateExecutionTimeTable(FileWriter myWriter, String eventLogName,HashMap<String,Double>algorithms) throws IOException {	
		
		myWriter.write("\\Cref{tbl:execution:time} reports the execution time (in iterations per second) for each input event log and method.\n");

		myWriter.write("\\begin{table}[h!]\n");
		myWriter.write("\\centering\n");
		myWriter.write("\\caption{Execution time comparison (iterations per second)}\n");
		myWriter.write("\\label{tbl:execution:time}\n");
		myWriter.write("\\setlength{\\tabcolsep}{6pt}\n");
		myWriter.write("\\fontsize{8}{10}\\selectfont\n");
		String colheader="l";
		
		String columnNames="Event log ";
		for(String s:algorithms.keySet())
		{
			columnNames+="&"+s;
			colheader+="c";
		}
		myWriter.write("\\begin{tabular}{"+colheader+"}\n");
		
		myWriter.write("\\toprule\n");
		myWriter.write(columnNames);
		myWriter.write("\\\\\n");
		myWriter.write("\\midrule\n");
		String value=eventLogName+"&";
		for(String s:algorithms.keySet())
			value+=String.format("%.2f",algorithms.get(s))+"&";
		value=value.substring(0,value.length()-1);
		value+="\\\\";
		myWriter.write(value+"\n");
		myWriter.write("\\bottomrule\n");
		myWriter.write("\\end{tabular}\n");
		myWriter.write("\\end{table}\n");
		
	}
	
	public void generateDCITable(FileWriter myWriter, String eventLogName,HashMap<String,Double>algorithms,int DCI,List<Point1> list) throws IOException {
		
		Map<String,List<DoublePair>>groupcounter2= new LinkedHashMap<>();
	      
	
	}
	
	public void generateLatexPlots(FileWriter myWriter, String eventLogName,HashMap<String,Double>algorithms) throws IOException {
	
		eventLogName=eventLogName.substring(0, eventLogName.indexOf("."));
		List<String> symbols=new ArrayList<String>();
		symbols.add("*,red");
		symbols.add("x,blue");
		symbols.add("square,green");
		symbols.add("triangle*,orange");
		symbols.add("pentagon*,black");
		symbols.add("triangle,orange");
		symbols.add("pentagon,purple");
		symbols.add("o,blue");
	/*	for(Frontier f : paretolist)
		{
			if(f!=null)
			{
				PerformanceAnalyser pa = new PerformanceAnalyser();
				int val = pa.calculateSize(f.getFpta());
				f.getFpta().show(f.getFpta(), f.getName()+" ----> "+f.getFitness()[0]+" , "+f.getFitness()[1]+" "+val);
			}
		}*/
		double y_min = Double.MAX_VALUE;
		double y_max=Double.MIN_VALUE;
		double x_min = Double.MAX_VALUE;
		double x_max=Double.MIN_VALUE;
	
		for(Point1 f:points)
		{
			if(f!=null)
			{
				if(f.er>x_max)
					x_max = f.er;
				if(f.er<x_min)
					x_min = f.er;
				if(f.size>y_max)
					y_max = f.size;
				if(f.size<y_min)
					y_min = f.size;
			}
		}
		
		myWriter.write("\\begin{figure}[h!]\n");
		myWriter.write("\\centering\n");
		myWriter.write("\\begin{subfigure}[b]{0.99\\textwidth}\n");
		myWriter.write("\\centering\n");
		myWriter.write("\\begin{tikzpicture}\n");
		myWriter.write("\\begin{axis}[ \n");
		myWriter.write("width=45mm, height=45mm,\n");
		myWriter.write("xlabel={Entropic relevance}, \n");
		myWriter.write("ylabel={Size}, \n");
		myWriter.write("title={}, \n");
		myWriter.write("grid=both, \n");
		myWriter.write("ymin=0, ymax="+(y_max+y_min/80)+", \n");
		myWriter.write("xmin="+(x_min-x_min/80)+", xmax="+(x_max+x_min/80)+",\n");
		myWriter.write("tick label style={font=\\footnotesize}, \n");
		myWriter.write("ylabel style={yshift=-5pt}, \n");
		myWriter.write("title style={yshift=-5pt}, \n");
		myWriter.write("legend style={at={(0.5,-0.3)}, anchor=north, legend columns=4, /tikz/every even column/.append }");
		myWriter.write("]\n");
		myWriter.write("\\addplot [scatter, only marks, point meta=explicit symbolic, scatter/classes={\n");
		int i=0;
		for(String name:algorithms.keySet())
		{
			if(i<algorithms.size()-1)
				myWriter.write(name+"={mark="+symbols.get(i)+"},\n");
			else
				myWriter.write(name+"={mark="+symbols.get(i)+"}\n");
			i++;
		}
		myWriter.write("}]\n");
		myWriter.write("table [meta=label] {\n");
		myWriter.write(" x           y     label \n");
		for(Point1 f: points)
		{
			myWriter.write(f.er+"  "+ f.size+"  "+f.name+"\n");
		}
		myWriter.write("};\n");
		String legend="\\legend{";
		
		for(String s:algorithms.keySet())
			legend+=s+",";
		legend=legend.substring(0, legend.length()-1)+"},\n";
		myWriter.write(legend);
		myWriter.write("\\end{axis}\n");
		myWriter.write("\\end{tikzpicture}\n");
		myWriter.write("\\label{fig:quality:dffa}\n");
		myWriter.write("\\end{subfigure}\n");
		
			
			
		y_min = Double.MAX_VALUE;
		y_max=Double.MIN_VALUE;
		x_min = Double.MAX_VALUE;
		x_max=Double.MIN_VALUE;
	
		for(Point1 f:points)
		{
			if(f!=null)
			{
				if(f.privacy>x_max)
					x_max = f.privacy;
				if(f.privacy<x_min)
					x_min = f.privacy;
				if(f.size>y_max)
					y_max = f.size;
				if(f.size<y_min)
					y_min = f.size;
			}
		}
			
			myWriter.write("\\caption{Discovered models plotted by size and entropic relevance.}\n");
			myWriter.write("\\label{fig:quality}\n");
			myWriter.write("\\centering\n");
			myWriter.write("\\begin{subfigure}[b]{0.99\\textwidth}\n");
			myWriter.write("\\centering\n");
			myWriter.write("\\begin{tikzpicture}\n");
			myWriter.write("\\begin{axis}[ \n");
			myWriter.write("width=45mm, height=45mm,\n");
			myWriter.write("xlabel={Privacy}, \n");
			myWriter.write("ylabel={Size}, \n");
			myWriter.write("title={}, \n");
			myWriter.write("grid=both, \n");
			myWriter.write("ymin=0, ymax="+(y_max+y_min/80)+", \n");
			myWriter.write("xmin="+(0.0)+", xmax="+(1.0)+",\n");
			myWriter.write("tick label style={font=\\footnotesize}, \n");
			myWriter.write("ylabel style={yshift=-5pt}, \n");
			myWriter.write("title style={yshift=-5pt}, \n");
			myWriter.write("legend style={at={(0.5,-0.3)}, anchor=north, legend columns=4, /tikz/every even column/.append }");
			myWriter.write("]\n");
			myWriter.write("\\addplot [scatter, only marks, point meta=explicit symbolic, scatter/classes={\n");
			int i1=0;
			for(String name:algorithms.keySet())
			{
				if(i1<algorithms.size()-1)
					myWriter.write(name+"={mark="+symbols.get(i1)+"},\n");
				else
					myWriter.write(name+"={mark="+symbols.get(i1)+"}\n");
				i1++;
			}
			myWriter.write("}]\n");
			myWriter.write("table [meta=label] {\n");
			myWriter.write(" x           y     label \n");
			for(Point1 f: points)
			{
				myWriter.write(f.privacy+"  "+ f.size+"  "+f.name+"\n");
			}
			myWriter.write("};\n");
			String legend1="\\legend{";
			
			for(String s:algorithms.keySet())
				legend1+=s+",";
			legend1=legend1.substring(0, legend1.length()-1)+"},\n";
			myWriter.write(legend1);
			myWriter.write("\\end{axis}\n");
			myWriter.write("\\end{tikzpicture}\n");
			myWriter.write("\\label{fig:quality:dffa1}\n");
			myWriter.write("\\end{subfigure}\n");

			myWriter.write("\\caption{Discovered models plotted by size and privacy.}\n");
				myWriter.write("\\label{fig:quality1}\n");
			myWriter.write("\\end{figure}\n\n");
			myWriter.write("\\noindent\n");
			
			
	}

}

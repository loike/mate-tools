package se.lth.cs.srl.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;
import is2.tools.Tool;

import se.lth.cs.srl.preprocessor.tokenization.StanfordChineseSegmenterWrapper;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;
import se.lth.cs.srl.util.BohnetHelper;

public class Preprocessor {

	private final Tokenizer tokenizer;
	private final Tool lemmatizer;
	private final Tagger tagger;
	private final is2.mtag.Tagger mtagger;
	private final Parser parser;
	
	public Preprocessor(Tokenizer tokenizer,Lemmatizer lemmatizer,Tagger tagger,is2.mtag.Tagger mtagger,Parser parser){
		this.tokenizer=tokenizer;
		this.lemmatizer=(Tool) lemmatizer;
		this.tagger=tagger;
		this.mtagger=mtagger;
		this.parser=parser;
	}
	
	public long tokenizeTime=0;
	public long lemmatizeTime=0;
	public long tagTime=0;
	public long mtagTime=0;
	public long dpTime=0;
	
	//TODO add loading times for each module too.
	public long tokenizerLoadTime=0;
	public long lemmatizerLoadTime=0;
	public long tagLoadTime=0;
	public long mtagLoadTime=0;
	public long dpLoadTime=0;
	

	/**
	 * Executes all loaded components on these forms
	 * 
	 * @param forms the forms, including the root token
	 * @return a SentenceData09 object filled out by all the components
	 */
	public SentenceData09 preprocess(String[] forms){
		SentenceData09 instance=new SentenceData09();
		instance.init(forms);
		return preprocess(instance);
	}
	/**
	 * Executes all loaded components on this SentenceData09 object. It is assumed to have the forms filled out only
	 * 
	 * @param instance the instance to process
	 * @return the same object as it was passed, but with some arrays filled out
	 */
	public SentenceData09 preprocess(SentenceData09 instance){
		if(lemmatizer!=null){
			long start=System.currentTimeMillis();
			lemmatizer.apply(instance);
			lemmatizeTime+=System.currentTimeMillis()-start;
		}
		if(mtagger!=null){
			long start=System.currentTimeMillis();
			mtagger.apply(instance);
			mtagTime+=System.currentTimeMillis()-start;
		} else { //TODO lose this else block when the Sentence class has been lost.
			instance.pfeats=new String[instance.forms.length];
		}
		if(tagger!=null){
			long start=System.currentTimeMillis();
			tagger.apply(instance);
			tagTime+=System.currentTimeMillis()-start;
		}
		if(parser!=null){
			long start=System.currentTimeMillis();
			instance=parser.apply(instance);
			dpTime+=System.currentTimeMillis()-start;
		}
		return instance;
	}
	
	
	public String[] tokenize(String sentence) {
		long start=System.currentTimeMillis();
		String[] words=tokenizer.tokenize(sentence);
		tokenizeTime+=(System.currentTimeMillis()-start);
		return words;
	}
	
	
	

	public static void main(String[] args) throws Exception{
		
		File desegmentedInput=new File("chi-desegmented.out");
		Tokenizer tokenizer=new StanfordChineseSegmenterWrapper(new File("/home/anders/Download/stanford-chinese-segmenter-2008-05-21/data"));
		Lemmatizer lemmatizer=new SimpleChineseLemmatizer();
		Tagger tagger=BohnetHelper.getTagger(new File("models/chi/tag-chn.model"));
		Preprocessor pp=new Preprocessor(tokenizer,lemmatizer,tagger,null,null);
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(desegmentedInput),"UTF-8"));
		String line;
		while((line=reader.readLine())!=null){
			String[] tokens=pp.tokenize(line);
			SentenceData09 s=pp.preprocess(tokens);
			System.out.println(s);
		}

	}
	
	
}

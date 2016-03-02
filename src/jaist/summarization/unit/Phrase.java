package jaist.summarization.unit;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CollectionUtils;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import intoxicant.analytics.coreNlp.StopwordAnnotator;
import jaist.summarization.AnnotatorHub;
import jaist.summarization.StopwordRemover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by chientran on 9/29/15.
 */
public class Phrase {
    String content = null;
    Boolean isNP = true;
    Double score = 0.0d;
    Integer parentId = -1;
    int id;

    int sentenceLength = 0;

    private static int _npID = 0;
    private static int _vpID = 0;
    private static String[] pronouns = {"it", "i", "you", "he", "they", "we", "she", "who", "them", "me", "him", "one", "her", "us", "something", "nothing", "anything", "himself", "everything", "someone", "themselves", "everyone", "itself", "anyone", "myself"};

    private HashSet<String> concepts = null;

    public Phrase(String content, Boolean isNP){
        this.content = content;
        this.isNP = isNP;
        if (isNP){
            id = _npID;
            _npID += 1;
        }else{
            id = _vpID;
            _vpID += 1;
        }
    }

    public Phrase(String content, Boolean isNP, Integer parentId){
        this(content, isNP);
        this.parentId = parentId;
    }

    public String getContent(){ return this.content; }
    public void setContent(String content){ this.content = content; }

    public Boolean isNP(){
        return this.isNP;
    }

    public void setScore(Double value){
        this.score = value;
    }

    public Double getScore(){
        if (this.score.isNaN()){
            System.out.println("What the hell" + this.toString());
        }
        return this.score;
    }

    public String toString(){
        return this.content + ": " + this.score;
    }

    public Integer getId(){ return this.id; }

    public void generateConcepts(){
        if (concepts != null) return;

        Annotation doc = new Annotation(content);
        AnnotatorHub.getInstance().getPipeline().annotate(doc);

        List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class);

        tokens = StopwordRemover.removeStopwords(tokens);

        concepts = new HashSet<>();

        List<String> unigrams = new ArrayList<>();

        for(CoreLabel token: tokens){
            // get lemma tokens which are not stopwords
            String textLemma = token.get(CoreAnnotations.LemmaAnnotation.class);

            concepts.add(textLemma);

            unigrams.add(textLemma);
        }

        for (List<String> bigramToken: CollectionUtils.getNGrams(unigrams, 2, 2)){
            concepts.add(String.join(" ", bigramToken));
        }

        // get named entity from phrase
        AnnotatorHub.getInstance().getEntityMentionsAnnotator().annotate(doc);

        for (CoreMap mention: doc.get(CoreAnnotations.MentionsAnnotation.class)) {
            String ner = mention.get(CoreAnnotations.TextAnnotation.class);
            concepts.add(ner);
        }
    }
    public HashSet<String> getConcepts(){
        if (concepts == null){
            generateConcepts();
        }

        return concepts;
    }

    public Integer getParentId(){ return parentId; }

    public Integer getSentenceLength(){ return sentenceLength; }
    public void setSentenceLength(int value){ this.sentenceLength = value; }

    public Boolean isPronoun(){
        return isNP() && Arrays.asList(pronouns).contains(this.content.toLowerCase());
    }

    public int getWordLength(){
        return this.content.split("[\\W]").length;
    }

    public boolean equals(String phrase){
        return this.content.equals(phrase);
    }

    public static void main(String[] args){
        Phrase p = new Phrase("This is just a test.", false);
        System.out.println(p.getWordLength());
    }
}
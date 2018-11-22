package com.itheima.test;

import com.hankcs.lucene.HanLPAnalyzer;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LuceneTest {
    /*
    *写入数据库
    */
 @Test
    public void indexTest() throws IOException {

     Directory d = FSDirectory.open(Paths.get("D:\\lucene\\index"));
     //IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
     //IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());
     IndexWriterConfig config = new IndexWriterConfig(new HanLPAnalyzer());
     IndexWriter indexWriter = new IndexWriter(d,config);
     File sourcefile  = new File("D:\\lucene\\source");
     File[] files = sourcefile.listFiles();
     for (File file : files) {
         //文件标题
         String fileName = file.getName();
         //文件内容
         String fileContent = FileUtils.readFileToString(file);
         //文件路径
         String filePath = file.getPath();
         //文件大小
          long fileSize = FileUtils.sizeOf(file);
          Field fname = new TextField("fname", fileName, Field.Store.YES);
          Field fcontent=new TextField("fcontent",fileContent,Field.Store.YES);
          Field fpath=new StoredField("fpath",filePath);
         // Field fsize=new StoredField("fsize",fileSize);
          Field fsize=new LongPoint("fsize",fileSize);
         Document doc = new Document();
         doc.add(fname);
         doc.add(fcontent);
         doc.add(fpath);
         doc.add(fsize);
         indexWriter.addDocument(doc);
     }
        indexWriter.close();

 }

 /*
 *删除索引库
 */
 @Test
  public  void  delIndex()throws IOException {
      Directory d = FSDirectory.open(Paths.get("D:\\lucene\\index"));
      IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
      IndexWriter indexWriter = new IndexWriter(d,config);
      indexWriter.deleteAll();
      indexWriter.close();
  }

  /*
  * 从索引库中查询数据
  */

  @Test
    public  void indexReader() throws IOException, ParseException {
      Directory directory = FSDirectory.open(Paths.get("D:\\lucene\\index"));
      IndexReader indexReader=DirectoryReader.open(directory);
      IndexSearcher indexSearcher = new IndexSearcher(indexReader);
      Query query1 = new MatchAllDocsQuery();
      Query query2 = new TermQuery(new Term("fname","中国人"));
      Query query3 = LongPoint.newRangeQuery("fsize",16,666);
      BooleanClause bc1= new BooleanClause(query1,BooleanClause.Occur.MUST);
      BooleanClause bc2 = new BooleanClause(query2,BooleanClause.Occur.MUST_NOT);
      Query query4 = new BooleanQuery.Builder().add(bc1).add(bc2).build();

      QueryParser parser = new QueryParser("fname", new HanLPAnalyzer());
      Query query5=parser.parse("spring");

      new MultiFieldQueryParser(new String[]{"fname","fcontent"},new HanLPAnalyzer());
      Query query6 = parser.parse("spring");

      TopDocs topDocs = indexSearcher.search(query6, 10);
      int totalHits=topDocs.totalHits;
      System.out.println("----------------->总数量："+totalHits);
      ScoreDoc[] scoreDocs = topDocs.scoreDocs;
      for (ScoreDoc scoreDoc : scoreDocs) {
          int doc = scoreDoc.doc;
          System.out.println("===================>文档编号："+doc);
          Document document = indexSearcher.doc(doc);
          System.out.println(document.get("fname"));
          System.out.println(document.get("fcontent"));
          System.out.println(document.get("fpath"));
          System.out.println(document.get("fsize"));
      }
           indexReader.close();

  }
}

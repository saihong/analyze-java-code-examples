package me.tomassetti.examples;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import me.tomassetti.support.DirExplorer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class EmptyCatchCheck {

    public static void listMethodCalls(File projectDir) {
        StringBuilder console = new StringBuilder();

        System.out.println(projectDir.getName()+"...");
        new DirExplorer((level, path, file) -> path.endsWith(".java") && path.indexOf("DAO.java")==-1, (level, path, file) -> {

            try {
                new VoidVisitorAdapter<Object>() {
//                    @Override
//                    public void visit(TryStmt n, Object arg) {
//                        super.visit(n, arg);
//                        System.out.println("trycatch [L " + n.getBegin().get().line + "] " + n);
//                    }


                    @Override
                    public void visit(CatchClause n, Object arg) {
                        super.visit(n, arg);
                        if (n.getBody().getStatements().size()==0) {
                            console.append(path).append("\r\n");
                            console.append(Strings.repeat("=", path.length())).append("\r\n");
                            console.append(" [L " + n.getBegin().get().line + "] " + n).append("\r\n");

//
//                            if (n.getBody().toString().indexOf(" throw ")==-1) {
//                                System.out.println(path);
//                                System.out.println(Strings.repeat("=", path.length()));
//
//                                System.out.println(" [L " + n.getBegin().get().line + "] " + n);
//                            }
                        }
                    }

//                    @Override
//                    public void visit(MethodCallExpr n, Object arg) {
//                        super.visit(n, arg);
//                        System.out.println(" [L " + n.getBegin().get().line + "] " + n);
//                    }
                }.visit(JavaParser.parse(file, Charset.forName("GBK")), null);
//                System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);


        BufferedWriter out = null;
        try {
            if (console.toString().trim().length() > 0) {
                File outputfile = new File("report/bg20830_emptycatch/" + projectDir.getName() + "_emptycatch.txt");
                outputfile.getParentFile().mkdirs();
                out = new BufferedWriter(new FileWriter(outputfile));
                out.write(console.toString());
                out.close();
                System.out.println(projectDir.getName() + " Error !");
            } else {
                System.out.println(projectDir.getName() + " PASS !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
//        String ptn="ac|ad|ae|am|ar|cix|da|dd|de|dr|ds|du|dw|dx|gp|ib|ibo|ic|ico|id|ih|iho|ihp|il|ilx|ip|is|iso|ivt|ix|kpx|max|mbx|mex|mhx|mox|mwx|mzx|od|ofp|oft|oj|ojo|ol|pf|po|pod|poo|pop|pot|sa|sb|sbx|sby|sg|sh|shx|sl|so|sob|sox|soy|sr|srx|ss|st|tc|tj|tm|tp|tq|ty|tyd|tyo|typ|wb|wbo|wd|wh|who|whp|ws|wso|wss|wz|wzo|wzp|wzt|zaf|zwp" ;
//        String ptn="^id|ih|iho|is|po|sl|ss|ty$" ;

        String ptn="^[acmkpzitsz]\\w+$" ;
//        String ptn="^[z]\\w+$" ;
//        String ptn="^[wits]\\w+$" ;
        String path = "\\\\icscdc01\\aplink\\bg2test";

//        String path = "\\\\icscdc01\\fileserver\\Temp\\I20496\\bg2-src\\dedi\\bg2";
        File projectDir = new File(path);

//        File projectDir = new File("\\\\icscdc01\\FileServer\\Temp\\I20496\\bg2-src\\src");
        File[] files = projectDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if ( name.matches("WEB\\-INF|work|waslogs|config|html|public|image(s)?") ) {
                    return false;
                }
                return name.matches(ptn);
            }
        });

        for (File dir : files) {
            listMethodCalls(dir);
        }

        System.out.println("finished !");
    }
}

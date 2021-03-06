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
import java.nio.file.Files;
import java.util.List;

public class MethodCallsExample {

    public static void listMethodCalls(File projectDir) {
        StringBuilder console = new StringBuilder();

        System.out.println(projectDir.getName()+"...");
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {

//            System.out.println(path);
//            System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
//                    @Override
//                    public void visit(TryStmt n, Object arg) {
//                        super.visit(n, arg);
//                        System.out.println("trycatch [L " + n.getBegin().get().line + "] " + n);
//                    }

                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                        if (!n.getNameAsString().equals("xctl")) {
                            return ;
                        }
                        console.append(path).append("\r\n");
                        console.append(Strings.repeat("=", path.length())).append("\r\n");

//                        System.out.println("Method:" + n.getNameAsString());
                        List<Node> childNodes = n.getChildNodes();
                        for (Node node : childNodes) {
                            if (node instanceof BlockStmt) {
                                for(Node cn:node.getChildNodes()) {
                                    if (cn instanceof TryStmt) {
                                        TryStmt tryStmt = (TryStmt) cn;
                                        NodeList<CatchClause> catchClauses = tryStmt.getCatchClauses();

                                        for (CatchClause catchClause : catchClauses) {
                                            console.append("method: xctl=>").append("\r\n");
                                            console.append(catchClause).append("\r\n");
                                        }
                                    }
                                }
                            }
                        }

//                        System.out.println("MethodDeclaration [L " + n.getBegin().get().line + "] " + n);
                    }

//                    @Override
//                    public void visit(MethodCallExpr n, Object arg) {
//                        super.visit(n, arg);
//                        System.out.println(" [L " + n.getBegin().get().line + "] " + n);
//                    }
                }.visit(JavaParser.parse(file), null);
//                System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);


        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("report/"+projectDir.getName()+"_xctl_catch.txt"));
            out.write(console.toString());
            out.close();
            System.out.println(projectDir.getName()+" OK !");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
//        String ptn="ac|ad|ae|am|ar|cix|da|dd|de|dr|ds|du|dw|dx|gp|ib|ibo|ic|ico|id|ih|iho|ihp|il|ilx|ip|is|iso|ivt|ix|kpx|max|mbx|mex|mhx|mox|mwx|mzx|od|ofp|oft|oj|ojo|ol|pf|po|pod|poo|pop|pot|sa|sb|sbx|sby|sg|sh|shx|sl|so|sob|sox|soy|sr|srx|ss|st|tc|tj|tm|tp|tq|ty|tyd|tyo|typ|wb|wbo|wd|wh|who|whp|ws|wso|wss|wz|wzo|wzp|wzt|zaf|zwp" ;
        String ptn="^m\\w+$" ;
        File projectDir = new File("\\\\icscdc01\\FileServer\\Temp\\I20496\\bg2-src\\src");
        File[] files = projectDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.equals("src")) {
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

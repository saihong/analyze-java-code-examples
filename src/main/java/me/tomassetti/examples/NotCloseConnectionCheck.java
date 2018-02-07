package me.tomassetti.examples;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import me.tomassetti.support.DirExplorer;


import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotCloseConnectionCheck {


    public static void listMethodCalls(File projectDir) {

        Pattern getConPtn = Pattern.compile("de301\\.getConnection");
        Pattern closePtn = Pattern.compile("(de301|con|\\w+con[n]?)\\.close\\(\\)");

        StringBuilder console = new StringBuilder();

        System.out.println(projectDir.getName() + "...");
        new DirExplorer((level, path, file) -> path.indexOf("backup") == -1 && path.endsWith(".java") && !path.endsWith("VO.java") && !path.endsWith("DAO.java"), (level, path, file) -> {

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
                        if (n.getType().asString().equals("Connection")) {
                            return ;
                        }
                        String body = n.getBody().toString();
                        // remove comment
                        body = body.replaceAll("//.+\\r?\\n", "");
                        body = body.replaceAll("\\/\\*[\\s\\S]+\\*\\/", "");

                        if (body.indexOf("getConnection") == -1 || n.getNameAsString().matches("main|getCon|getConnection|setConnection|beginTransa\\w+")) {
                            return;
                        }
                        if (body.indexOf("try ") == -1) {
                            return;
                        }

                        if (body.trim().length() == 0) {
                            System.out.println("Body is empty !");
                            return;
                        }
                        Matcher getConMatcher = getConPtn.matcher(body);
                        Matcher closeMatcher = closePtn.matcher(body);
                        int connectionCount = 0;
                        int closeCount = 0;
                        while (getConMatcher.find()) {
                            connectionCount++;
                        }
                        while (closeMatcher.find()) {
                            closeCount++;
                        }
                        if (connectionCount > closeCount) {
                            console.append(path).append("\r\n");
                            console.append(Strings.repeat("=", path.length())).append("\r\n");
                            console.append(" [L " + n.getBegin().get().line + "] " + connectionCount + " vs " + closeCount + " \r\n" + n).append("\r\n");
                        }
                    }

                    //
//                    @Override
//                    public void visit(ForStmt n, Object arg) {
//                        super.visit(n, arg);
//                        if (n.getBody().toString().indexOf("getConnection") > -1) {
//                            console.append(path).append("\r\n");
//                            console.append(Strings.repeat("=", path.length())).append("\r\n");
//                            console.append(" [L " + n.getBegin().get().line + "] " + n).append("\r\n");
//                        }
//                    }

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

//                File outputfile = new File("report/bg2-dedi/notclosecon/" + projectDir.getName() + "_notclosecon.txt");
                File outputfile = new File("report/bg2-src-0911/notclosecon/" + projectDir.getName() + "_notclosecon.txt");
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
//        String ptn="^ss$" ;
        String ptn = "^[acmkpwitsz]\\w+$";

        //        String ptn="^[z]\\w+$" ;
//        String ptn="^[witsz]\\w+$" ;
//        String ptn="^ic$" ;
//        String ptn="^[witsz]\\w+$" ; done
        String path = "\\\\icscdc01\\aplink\\bg2test";

//        String path = "\\\\icscdc01\\fileserver\\Temp\\I20496\\bg2-src\\dedi\\bg2";
        File projectDir = new File(path);
//        File projectDir = new File("\\\\icscdc01\\FileServer\\Temp\\I20496\\bg2-src\\src");
        File[] files = projectDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if ( name.matches("WEB\\-INF|work|waslogs|config|html|public|images") ) {
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

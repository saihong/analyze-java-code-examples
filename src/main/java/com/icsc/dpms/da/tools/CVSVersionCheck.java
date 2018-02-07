package com.icsc.dpms.da.tools;


import com.icsc.dpms.da.tools.support.DirExplorer;

import java.io.*;

public class CVSVersionCheck {

    public static void checkCVSVersion(File projectDir) {

        StringBuilder console = new StringBuilder();

        System.out.println(projectDir.getName() + "...");
        new DirExplorer((level, path, file) -> path.indexOf("backup") == -1 && path.endsWith(".java"), (level, path, file) -> {

            StringBuilder contentBuilder = new StringBuilder();

            boolean found = false;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    if (sCurrentLine.indexOf("CLASS_VERSION") > -1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    console.append(path + "\r\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).explore(projectDir);


        BufferedWriter out = null;
        try {
            if (console.toString().trim().length() > 0) {

//                File outputfile = new File("report/bg2-dedi/notclosecon/" + projectDir.getName() + "_notclosecon.txt");
                File outputfile = new File("report/"+projectDir.getParentFile().getName()+ "/noversion/" + projectDir.getName() + "_noversion.txt");
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
        if (args.length == 0) {
            System.out.println("projectDir is missed! ex: bg2test; hbjyjttest; pttest ...");
            return ;
        }

        String ptn = "^[acdefghikmopqrstuvwxz]\\w+$";

        //        String ptn="^[z]\\w+$" ;
//        String ptn="^[witsz]\\w+$" ;
//        String ptn="^ic$" ;
//        String ptn="^[witsz]\\w+$" ; done
        String path =  args[0];

//        String path = "\\\\icscdc01\\fileserver\\Temp\\I20496\\bg2-src\\dedi\\bg2";
        File projectDir = new File(path);
//        File projectDir = new File("\\\\icscdc01\\FileServer\\Temp\\I20496\\bg2-src\\src");
        File[] files = projectDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.matches("WEB\\-INF|work|waslogs|config|html|public|images")) {
                    return false;
                }
                return name.matches(ptn);
            }
        });

        for (File dir : files) {
            checkCVSVersion(dir);
        }

        System.out.println("finished !");
    }
}

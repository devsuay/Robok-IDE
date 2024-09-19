package org.gampiot.robok.ui.fragments.project.create.util;

import android.content.Context;
import android.os.Environment;

import org.gampiot.robok.feature.template.code.java.JavaClassTemplate;
import org.gampiot.robok.feature.template.code.android.game.logic.GameScreenLogicTemplate;
import org.gampiot.robok.ui.fragments.project.template.model.ProjectTemplate;
import org.gampiot.robok.feature.component.terminal.RobokTerminalWithRecycler;

import org.robok.aapt2.compiler.CompilerTask;
import org.robok.aapt2.model.Project;
import org.robok.aapt2.model.Library;
import org.robok.aapt2.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProjectManager {

    private CreationListener creationListener;
    private Context context;
    private File outputPath;
    
    public ProjectManager () {}
    
    public ProjectManager (Context context) {
         this.context = context;
    }
    
    public void setProjectPath(File value) {
         outputPath = value;
    }
    
    public File getProjectPath() {
         return outputPath;
    }
    
    public String getProjectPath() {
         return outputPath.getAbsolutePath();
    }

    public void create(String projectName, String packageName, ProjectTemplate template) {
        try {
            InputStream zipFileInputStream = context.getAssets().open(template.zipFileName);
            
            if (!outputPath.exists()) {
                outputPath.mkdirs();
            }

            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(zipFileInputStream));
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    String entryName = zipEntry.getName();
                    String outputFileName = entryName
                            .replace(template.name, projectName)
                            .replace("game/logic/$pkgName", "game/logic/" + packageName.replace('.', '/'));
                    
                    File outputFile = new File(outputPath, outputFileName);

                    if (!outputFile.getParentFile().exists()) {
                        outputFile.getParentFile().mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.close();
                }
                zipInputStream.closeEntry();
            }

            zipInputStream.close();
            
            createJavaClass(projectName, packageName);

        } catch (IOException e) {
            e.printStackTrace();
            creationListener.onProjectCreateError();
        }
    }

    private void createJavaClass(String projectName, String packageName) {
        try {
            GameScreenLogicTemplate template = new GameScreenLogicTemplate();
            template.setCodeClassName("MainScreen");
            template.setCodeClassPackageName(packageName);
            template.configure();
            
            String classFilePath = "game/logic/" + packageName.replace('.', '/') + "/" + template.getClassName() + ".java";
            File javaFile = new File(outputPath, classFilePath);

            if (!javaFile.getParentFile().exists()) {
                javaFile.getParentFile().mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(javaFile);
            fos.write(template.getCodeClassContent().getBytes());
            fos.close();
            creationListener.onProjectCreate();

        } catch (IOException e) {
            e.printStackTrace();
            creationListener.onProjectCreateError();
        }
    }
    
    public void build () {
         var terminal = new RobokTerminalWithRecycler(context);
         var logger = new Logger();
         logger.attach(terminal.getRecyclerView());
         Project project = new Project();
         project.setLibraries(Library.fromFile(new File("")));
         project.setResourcesFile(new File("/sdcard/Robok/.projects/project/game/res/"));
         project.setOutputFile(new File("/sdcard/Robok/.projects/project/build/"));
         project.setJavaFile(new File("/sdcard/Robok/.projects/project/game/logic/"));
         project.setManifestFile(new File("/sdcard/Robok/.projects/project/game/AndroidManifest.xml"));
         project.setLogger(logger);
         project.setMinSdk(21);
         project.setTargetSdk(28);
         CompilerTask task = new CompilerTask(context);
         task.execute(project);
         terminal.show();
    }
    
    public void setListener (CreationListener creationListener) {
         this.creationListener = creationListener;
    }
    
    public interface CreationListener {
         public void onProjectCreate();
         public void onProjectCreateError();
    }
}

package by.it.tolstik.jd01_15;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class TaskC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input;
        String directory = getFirstFileName(TaskC.class, "");
        Path path = Paths.get(directory);

        while (true) {
            System.out.print(path + "> ");
            input = sc.nextLine();
            //resolveCommands
            resolveCommands(input);

            if (input.equalsIgnoreCase("end")) {
                return;
            }
            //switchState
            path = switchState(input, path);
        }
    }

    private static Path switchState(String input, Path path) {
        Date lastModDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh.mm");
        String sDate;
        int countFiles = 0;
        int countDirs = 0;
        int filesLength = 0;

        switch (currentCommand) {
            case ChangeDirParent:
                path = getFileName("", path.getParent());
                break;
            case ChangeDirAddress:
                path = getFileName(input.replace("cd ", ""), path);
                break;
            case Directory:
                File f = new File(path.toString());
                File[] dirs = f.listFiles();
                for (File dir : Objects.requireNonNull(dirs)) {
                    lastModDate = new Date(dir.lastModified());
                    sDate = dateFormat.format(lastModDate);
                    if (dir.isDirectory()) {
                        System.out.printf("%s%15s%7s %s%n", sDate, "<DIR>", "", dir.getName());
                        countDirs++;
                    } else if (dir.isFile()) {
                        System.out.printf("%s%7s%15d %s%n", sDate, "", dir.length(), dir.getName());
                        countFiles++;
                        filesLength += dir.length();
                    }
                }
                String fi;
                String di;
                //nounCaseChange
                if (countFiles == 1) {
                    fi = "файл";
                } else if (countFiles >= 2 && countFiles < 5) {
                    fi = "файла";
                } else {
                    fi = "файлов";
                }

                if (countDirs == 1) {
                    di = "папка";
                } else if (countDirs >= 2 && countDirs < 5) {
                    di = "папки";
                } else {
                    di = "папок";
                }
                System.out.printf("%15d %s %12d байт%n%15d %s %12d байт свободно %n",
                        countFiles, fi, filesLength, countDirs, di, f.getFreeSpace());

                break;
            case Unresolved:
                System.out.println("\"" + input + "\" не является внутренней или внешней ");
                System.out.println("командой, исполняемой программой или пакетным файлом.");
                break;
            default:
                break;
        }
        return path;
    }

    private static void resolveCommands(String input) {
        if (input.equals("cd ..") || input.equals("cd..")) {
            currentCommand = Command.ChangeDirParent;
        } else if (input.startsWith("cd") && !input.contains("..")) {
            currentCommand = Command.ChangeDirAddress;
        } else if (input.equals("dir")) {
            currentCommand = Command.Directory;
        } else {
            currentCommand = Command.Unresolved;
        }
    }

    private enum Command {
        ChangeDirParent,
        ChangeDirAddress,
        Directory,
        Unresolved
    }

    public static Command currentCommand = Command.Directory;

    public static Path getFileName(String dirName, Path curPath) {

        if (!dirName.contains(File.separator) || !dirName.contains("\\")) {
            curPath = Paths.get(curPath.toString() + File.separator + dirName);
        } else {
            curPath = Paths.get(dirName);
        }
        return curPath;
    }

    public static String getFirstFileName(Class<?> aClass, String fileName) {
        return System.getProperty("user.dir") +
                File.separator +
                "src" +
                File.separator +
                aClass.getName().replace(".", File.separator).
                        replace(aClass.getSimpleName(), "")
                + fileName;
    }
}

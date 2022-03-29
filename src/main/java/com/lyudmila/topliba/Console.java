package com.lyudmila.topliba;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Console {
    private static String path = "log.txt";

    static {
        try(FileWriter writer = new FileWriter(path, false))
        {
            writer.write("");
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    public static void output(String text){
        output(text, false);
    }

    public static void output(String text, boolean writeToFile){
        text = text == null ? "�������������� ������" : text;
        System.out.println(text);
        if(!writeToFile){
            return;
        }

        try(FileWriter writer = new FileWriter(path, true))
        {
            writer.write("\r\n");
            writer.write(text);
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    public static String input(String text){
        System.out.println(text);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}

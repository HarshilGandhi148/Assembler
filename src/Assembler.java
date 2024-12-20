/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package src;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author HarshilGandhi148
 */
public class Assembler {
    
    //initialize variables
    public File codeFile;
    public int nextAvailable;
    ArrayList<String> code;
    HashMap<String, Integer> symbols = new HashMap<>();
    HashMap<String, String> comps = new HashMap<>();
    
    public Assembler(String path)
    {
        //first available memeory location is 16
        nextAvailable = 16;
        codeFile = new File(path);
        code = new ArrayList<>();
        symbols = new HashMap<>();
        
        //autocreated symbols
        symbols.put("SP", 0);
        symbols.put("LCL", 1);
        symbols.put("ARG", 2);
        symbols.put("THIS", 3);
        symbols.put("THAT", 4);
        symbols.put("R0", 0);
        symbols.put("R1", 1);
        symbols.put("R2", 2);
        symbols.put("R3", 3);
        symbols.put("R4", 4);
        symbols.put("R5", 5);
        symbols.put("R6", 6);
        symbols.put("R7", 7);
        symbols.put("R8", 8);
        symbols.put("R9", 9);
        symbols.put("R10", 10);
        symbols.put("R11", 11);
        symbols.put("R12", 12);
        symbols.put("R13", 13);
        symbols.put("R14", 14);
        symbols.put("R15", 15);
        symbols.put("SCREEN", 16384);
        symbols.put("KBD", 24576);
        
        //used to determine computation bits
        comps.put("0", "101010");
        comps.put("1", "111111");
        comps.put("-1", "111010");
        comps.put("D", "001100");
        comps.put("A", "110000");
        comps.put("!D", "001101");
        comps.put("!A", "110001");
        comps.put("-D", "001111");
        comps.put("-A", "110011");
        comps.put("D+1", "011111");
        comps.put("A+1", "110111");
        comps.put("D-1", "001110");
        comps.put("A-1", "110010");
        comps.put("D+A", "000010");
        comps.put("D-A", "010011");
        comps.put("A-D", "000111");
        comps.put("D&A", "000000");
        comps.put("D|A", "010101");
    }
    public static void main(String[] args) {
        Assembler parser = new Assembler("src\\Pong.asm");
        
        parser.loadFile();
        parser.obtainSymbols();
        
        //outputs to hack file
        try {   
            FileWriter fw = new FileWriter("src\\Output.hack");
            for (int i = 0; i < parser.code.size(); i++)
            {
                String out = parser.convertLine(parser.code.get(i));
                //System.out.println(parser.code.get(i) + ": " + out);
                fw.write(out + "\n");
            } 
            fw.close();
        } catch (IOException e)
            {
               System.out.println("File not found (file reader)");
            }
    }
    
    //extracts all symbols (labels and variables) and adds them to symbol hash set
    public void obtainSymbols()
    {   
        int i=0;
        
        //labels
        while(i < code.size()-1)
        {
            String line = code.get(i);
            if (line.contains("("))
            {
                symbols.put(line.substring(line.indexOf("(") + 1, line.indexOf(")")), i);
                code.remove(i);
            } else
            {
                i++;
            }
        }
        
        //new variables
        i = 0;
        while (i < code.size() - 1)
        {
            String line = code.get(i);
            if (line.contains("@"))
            {
                String symbol = line.substring(line.indexOf("@") + 1, line.length());
                if (!symbols.containsKey(symbol) && !symbol.matches("\\d+(\\.\\d+)?"))                  
                {
                    symbols.put(symbol, nextAvailable);
                    //System.out.println(symbol + ":" + nextAvailable);
                    nextAvailable++;
                }
                i++;
            } else
            {
                i++;
            }
        }
    }
    
    //converts each line to a 16 bit CPU instruction
    public String convertLine(String line)
    {
        String convertedLine = "";
        
        //a instruction
        if(line.substring(0,1).equals("@"))
        {
            int memoryAddress = 0;
            convertedLine = convertedLine + "0";
            if (!line.substring(1, line.length()).matches("\\d+(\\.\\d+)?"))
            {
                memoryAddress = symbols.get(line.substring(1, line.length()));
            } else
            {
               memoryAddress =  Integer.parseInt(line.substring(1, line.length()));
            }
            
            //convert decimal to binary
            return convertedLine + decimalToBinary(String.valueOf(memoryAddress));
            
        } 
        
        //c intruction
        else
        {  
            convertedLine = convertedLine + "111";
            String dest = "";
            String jump = "000";
            String comp = "";
            String m_bit = "0";
            
            //destination bits
            if (line.contains("="))
            {
                String d = line.substring(0, line.indexOf("="));
                if (d.contains("A"))
                {
                    dest = dest + "1";
                } else
                {
                    dest = dest + "0";
                }
                
                if (d.contains("D"))
                {
                    dest = dest + "1";
                } else
                {
                    dest = dest + "0";
                }
                
                if (d.contains("M"))
                {
                    dest = dest + "1";
                } else
                {
                    dest = dest + "0";
                }
                line = line.substring(line.indexOf("=") + 1, line.length());
            } else
            {
                dest = "000";
            }
            
            //jump bits
            if (line.contains(";"))
            {
                if (line.contains("JGT"))
                {
                    jump = "001";
                } else if (line.contains("JEQ"))
                {
                    jump = "010";
                }  else if (line.contains("JGE"))
                {
                    jump = "011";
                }  else if (line.contains("JLT"))
                {
                    jump = "100";
                } else if (line.contains("JNE"))
                {
                    jump = "101";
                } else if (line.contains("JLE"))
                {
                    jump = "110";
                } else if (line.contains("JMP"))
                {
                    jump = "111";
                }
                line = line.substring(0, line.indexOf(";"));
            } else
            {
                jump = "000";
            }
            
            //A or M bit
            if (line.contains("M"))
            {
                m_bit = "1";
            }
            
            //computation bits
            line = line.replaceAll("M", "A");
            comp = comps.get(line);
            return convertedLine + m_bit + comp + dest + jump;   
        }
    }
    
    //converts a decimal number to 15 bit binary
    public String decimalToBinary(String number)
    {
        int addressI = Integer.parseInt(number);
        double quotient = addressI;
        String remainder = "";
        while (quotient!=0)
        {
            double temp = 2*(quotient/2 - Math.floor(quotient/2));
            quotient = Math.floor(quotient/2);
            remainder = String.valueOf((int)temp) + remainder;
        }
        
        if (remainder.length() < 15)
        {
            String additional = "";
            for(int i = 0; i < (15-remainder.length()); i++)
            {
                additional = additional + "0";
            }
            remainder = additional + remainder;
        }
        return remainder;
    }
    
    //loads an assembly file to be converted
    public void loadFile()
    {   
        try
        {
            Scanner fileReader = new Scanner(codeFile);
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                data = data.replaceAll("\\s+","");
                if (data.contains("//"))
                {
                    data = data.substring(0, data.indexOf("/"));
                }
                if (!data.equals(""))
                {
                    //System.out.println(data);
                    code.add(data);
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) 
        {
            System.out.println("Text file path not found!");
            e.printStackTrace();
        }
    }
}

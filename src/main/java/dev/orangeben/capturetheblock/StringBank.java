package dev.orangeben.capturetheblock;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public class StringBank {

    /**
     * Returns "s" if count is 1, "" otherwise
     * @param count
     * @return
     */
    public static String pluralize(int count) {
        return (count != 1) ? "s" : "";
    }

    /**
     * Formats a number of seconds into minutes and seconds
     * The output will be in the format m minutes s seconds unless the number of minutes or seconds is 0, in which case that field will be omitted.
     * @param seconds The number of seconds to format
     * @return The formatted string.
     */
    public static String formatTime(int seconds) {
        int s = seconds % 60;
        int m = seconds / 60;
        String out = "";
        if(m > 0) {
            out += m + " minute" + pluralize(m);
        }
        if(m > 0 && s > 0) {
            out += " ";
        }
        if(s > 0 || m == 0) {
            out += s + " second" + pluralize(s);
        }
        return out;
    }

    private Map<String, String> bank;

    public StringBank(File file) {
        bank = new HashMap<String, String>();

        try {
            Scanner s = new Scanner(file);

            while(s.hasNextLine()) {
                String line = s.nextLine();
                if(line.length() > 0 && line.charAt(0) != '#') {
                    int ci = line.indexOf(":");
                    String key = line.substring(0, ci).replace(" ", "");
                    String val = line.substring((line.charAt(ci+1)==' ') ? ci+2 : ci+1);
                    
                    // Colors are in the format &{NAME} or &{#HEXCOD}
                    Pattern p = Pattern.compile("(^|[^\\\\])&\\{(#?[a-zA-Z0-9_]+)\\}");
                    Matcher m = p.matcher(val);
                    StringBuffer buffer = new StringBuffer();

                    while(m.find()) {
                        String replace = m.group(1) + ChatColor.of(m.group(2));
                        m.appendReplacement(buffer, replace);
                    }
                    m.appendTail(buffer);
                    
                    bank.put(key, buffer.toString());
                }
            }

            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Process any string that references another string
        boolean mamdeChange = true;
        int runct = 0;
        while(mamdeChange & runct < bank.size()) {
            mamdeChange = false;
            for(String key : bank.keySet()) {
                String val = bank.get(key);
                
                // Replacements are in the format {NAME}
                Pattern p = Pattern.compile("(^|[^\\\\])\\{([a-zA-Z0-9_.]+)\\}");
                Matcher m = p.matcher(val);
                StringBuffer buffer = new StringBuffer();
                
                while(m.find()) {
                    String replace = m.group(1) + bank.get(m.group(2));
                    m.appendReplacement(buffer, replace);
                    mamdeChange = true;
                }
                m.appendTail(buffer);
                
                bank.put(key, buffer.toString());
            }
        }

        for(String key : bank.keySet()) {
            System.out.println(key + ":" + bank.get(key));
        }

    }
    
    public String get(String key, Object... parts) {
        System.out.println(key + " : " + bank.get(key) + "?" + parts);
        if(bank.containsKey(key)) {
            try (Formatter f = new Formatter(Locale.getDefault())) {
                return f.format(bank.get(key), parts).toString();
            }
        } else {
            throw new NoSuchElementException("String with key " + key + " doesn't exist");
        }
    }
}

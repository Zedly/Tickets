/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zedly.tickets;

import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.ChatColor;

/**
 *
 * @author Dennis
 */
public class StringUtil {

    private static final char[] HEX_CHARS = {0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66};

    private static final HashMap<Character, Integer> CHAR_WIDTHS = new HashMap<>();

    public static String joinAndFormat(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        boolean space = false;
        for (int i = startIndex; i < args.length; i++) {
            if (space) {
                sb.append(" ");
            }
            sb.append(ChatColor.translateAlternateColorCodes('&', args[i]));
            space = true;
        }
        return sb.toString();
    }

    public static String generateHLineTitle(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.DARK_GRAY).append(ChatColor.STRIKETHROUGH).append("     ");
        sb.append(ChatColor.BLUE).append(" ").append(title).append(" ");
        sb.append(ChatColor.DARK_GRAY).append(ChatColor.STRIKETHROUGH);
        for (int i = getTextWidth(ChatColor.stripColor(title)) + 21; i < 310; i += 4) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static int getTextWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            if (CHAR_WIDTHS.containsKey(c)) {
                width += CHAR_WIDTHS.get(c) + 1;
            } else {
                width += 6;
            }
        }
        return width;
    }

    public static String padToWidth(String string, int desiredWidth) {
        int actualWidth = getTextWidth(string);
        int missingWidth = desiredWidth - actualWidth;
        if (missingWidth < 4) {
            return string;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(string);

        for (int i = 4; i < missingWidth; i += 4) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String padToWidthMod(String string, int desiredMod) {
        int actualWidth = getTextWidth(string) % desiredMod;
        int missingWidth = desiredMod - actualWidth;
        if (missingWidth < 4) {
            return string;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(string);
        for (int i = 4; i < missingWidth; i += 4) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String hexString(byte[] binary) {
        char[] hex = new char[binary.length * 2];
        for (int i = 0; i < binary.length; i++) {
            hex[2 * i] = HEX_CHARS[(binary[i] >> 4) & 0xF];
            hex[2 * i + 1] = HEX_CHARS[binary[i] & 0xF];
        }
        return new String(hex);
    }

    public static String extract(String text, String start, String end) {
        int left = text.indexOf(start) + start.length();
        if (left == -1) {
            return null;
        }
        int right = text.indexOf(end, left);
        if (right == -1) {
            return null;
        }
        return text.substring(left, right);
    }

    public static String extract(String text, String start, String end, int offset) {
        int left = text.indexOf(start, offset);
        if (left == -1) {
            return null;
        }
        int right = text.indexOf(end, left + start.length());
        if (right == -1) {
            return null;
        }
        return text.substring(left + start.length(), right);
    }

    public static String[] extractAll(final String text, final String start, final String end) {
        LinkedList<String> res_ = new LinkedList<>();
        int offset = 0;
        while (true) {
            int left = text.indexOf(start, offset);
            if (left == -1) {
                String[] res = new String[res_.size()];
                res = res_.toArray(res);
                return res;
            }
            int right = text.indexOf(end, left + start.length());
            if (right == -1) {
                String[] res = new String[res_.size()];
                res = res_.toArray(res);
                return res;
            }
            res_.add(text.substring(left + start.length(), right));
            offset = right + end.length();
        }
    }

    static {
        CHAR_WIDTHS.put('I', 3);
        CHAR_WIDTHS.put('i', 1);
        CHAR_WIDTHS.put('k', 4);
        CHAR_WIDTHS.put('l', 2);
        CHAR_WIDTHS.put('t', 4);
        CHAR_WIDTHS.put('_', 5);
        CHAR_WIDTHS.put('-', 5);
        CHAR_WIDTHS.put(' ', 5);
        CHAR_WIDTHS.put('!', 1);
        CHAR_WIDTHS.put('@', 6);
        CHAR_WIDTHS.put('(', 4);
        CHAR_WIDTHS.put(')', 4);
        CHAR_WIDTHS.put('{', 4);
        CHAR_WIDTHS.put('}', 4);
        CHAR_WIDTHS.put('[', 3);
        CHAR_WIDTHS.put(']', 3);
        CHAR_WIDTHS.put(':', 1);
        CHAR_WIDTHS.put(';', 1);
        CHAR_WIDTHS.put('"', 3);
        CHAR_WIDTHS.put('\'', 1);
        CHAR_WIDTHS.put('<', 4);
        CHAR_WIDTHS.put('>', 4);
        CHAR_WIDTHS.put('|', 1);
        CHAR_WIDTHS.put('.', 1);
        CHAR_WIDTHS.put(',', 1);
        CHAR_WIDTHS.put(' ', 3);
    }
}

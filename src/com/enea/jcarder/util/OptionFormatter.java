/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2. See the
 * accompanying file LICENSE.txt for details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class for an OptionParser.
 */
class OptionFormatter {
    private final int mOptionIndent;
    private final int mDescrIndent;
    private final int mMaxWidth;

    public OptionFormatter(int optionIndent, int descrIndent, int maxWidth) {
        mOptionIndent = optionIndent;
        mDescrIndent = descrIndent;
        mMaxWidth = maxWidth;
    }

    public void format(StringBuilder sb, String option, String descr) {
        addSpace(sb, mOptionIndent);
        sb.append(option);
        final int firstIndent;
        if (mOptionIndent + option.length() >= mDescrIndent) {
            sb.append("\n");
            firstIndent = mDescrIndent;
        } else {
            firstIndent = mDescrIndent - (mOptionIndent + option.length());
        }
        if (descr == null) {
            sb.append('\n');
        } else {
            List<String> descrLines = wrapText(descr, mMaxWidth - mDescrIndent);
            indentText(sb, descrLines, mDescrIndent, firstIndent);
        }
    }

    static void addSpace(StringBuilder sb, int n) {
        for (int i = 0; i < n; ++i) {
            sb.append(' ');
        }
    }

    static List<String> wrapText(String text, int maxWidth) {
        ArrayList<String> result = new ArrayList<String>();
        int pos = 0;
        StringBuilder sb = new StringBuilder();

        for (String word : text.split("\\s+")) {
            if (sb.length() > 0 && word.length() + 1 > maxWidth - sb.length()) {
                result.add(sb.toString());
                sb.setLength(0);
                sb.append(word);
                pos = word.length();
            } else {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(word);
                pos += word.length();
            }
        }
        if (sb.length() > 0) {
            result.add(sb.toString());
        }

        return result;
    }

    static void indentText(StringBuilder sb,
                           List<String> lines,
                           int indent,
                           int firstIndent) {
        boolean first = true;
        addSpace(sb, firstIndent);
        for (String line : lines) {
            if (first) {
                first = false;
            } else {
                addSpace(sb, indent);
            }
            sb.append(line);
            sb.append('\n');
        }
    }
}

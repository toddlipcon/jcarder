package com.enea.jcarder.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class parses command-line options.
 */
public class OptionParser {
    private class OptionData {
        String valueName;
        String description;
    }

    private final List<String> mArguments = new ArrayList<String>();
    private final Map<String, String> mParsedOptions =
        new HashMap<String, String>();
    private final Map<String, OptionData> mValidOptions =
        new HashMap<String, OptionData>();

    public OptionParser() {
    }

    /**
     * Tell the parser to recognize a new option.
     *
     * Options can be given on the following forms:
     *
     * <ul>
     * <li>-option</li>
     * <li>-option foo</li>
     * </ul>
     *
     * The first form specifies an option without a value and the second
     * specifies an option with a value.
     *
     * @param option
     *            The option.
     * @param description
     *            Description of the option.
     */
    public void addOption(String option, String description) {
        final int spacePos = option.indexOf(' ');
        final String flag;
        final String valueName;
        if (spacePos == -1) {
            flag = option;
            valueName = null;
        } else {
            flag = option.substring(0, spacePos);
            valueName = option.substring(spacePos + 1);
        }
        OptionData data = new OptionData();
        data.valueName = valueName;
        data.description = description;
        mValidOptions.put(flag, data);
    }

    /**
     * Get arguments remaining after options (and their values) have been
     * parsed.
     *
     * @return The arguments.
     */
    public List<String> getArguments() {
        return mArguments;
    }

    /**
     * Get a string containing help text describing available options.
     *
     * @return The help text.
     */
    public String getOptionHelp() {
        StringBuilder sb = new StringBuilder();
        OptionFormatter formatter = new OptionFormatter(2, 23, 79);
        ArrayList<String> options = new ArrayList<String>();
        options.addAll(mValidOptions.keySet());
        Collections.sort(options);
        for (String option : options) {
            final OptionData data = mValidOptions.get(option);
            final String optAndVal;
            if (data.valueName == null) {
                optAndVal = option;
            } else {
                optAndVal = option + " " + data.valueName;
            }
            formatter.format(sb, optAndVal, data.description);
        }
        return sb.toString();
    }

    /**
     * Get parsed options.
     *
     * The map is keyed on option and the values are the option values (null if
     * no parameter was expected).
     *
     * @return The option map.
     */
    public Map<String, String> getOptions() {
        return mParsedOptions;
    }

    /**
     * Parse arguments.
     *
     * @param arguments
     *            The arguments to parse.
     * @throws InvalidOptionException
     *             If an invalid option is encountered.
     */
    public void parse(String[] arguments) throws InvalidOptionException {
        mArguments.clear();
        boolean reachedNonOption = false;
        for (int i = 0; i < arguments.length; ++i) {
            if (!reachedNonOption
                    && (arguments[i].length() == 0
                            || arguments[i].charAt(0) != '-')) {
                reachedNonOption = true;
            }
            if (reachedNonOption) {
                mArguments.add(arguments[i]);
            } else {
                String option = arguments[i];
                if (mValidOptions.containsKey(option)) {
                    OptionData data = mValidOptions.get(option);
                    String optionArgument;
                    if (data.valueName != null) {
                        ++i;
                        if (i < arguments.length) {
                            optionArgument = arguments[i];
                        } else {
                            String message = "value missing to flag " + option;
                            throw new InvalidOptionException(message);
                        }
                    } else {
                        optionArgument = null;
                    }
                    mParsedOptions.put(option, optionArgument);
                } else {
                    throw new InvalidOptionException("invalid flag: "
                                                     + arguments[i]);
                }
            }
        }
    }
}

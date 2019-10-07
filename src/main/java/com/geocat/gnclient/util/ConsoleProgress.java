package com.geocat.gnclient.util;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Class to display a progress percentage in the console.
 *
 */
public class ConsoleProgress {
    private AtomicLong current;
    private long total;
    private String counterValue;
    private boolean displayCurrentValue;

    public ConsoleProgress(long total, String label) {
       this(total, label, true);
    }

    public ConsoleProgress(long total, String label,
                           boolean displayCurrentValue) {
        current = new AtomicLong(0);

        this.total = total;
        this.displayCurrentValue = displayCurrentValue;

        counterValue = "0%";

        // Display the progress
        System.out.print(label + counterValue);
    }


    public void updateProgress() {
        updateProgress("");
    }


    public void updateProgress(String additionalInfo) {
        if (current.get() < total) {
            int counterSize = counterValue.length();
            long currVal = current.incrementAndGet();

            counterValue = (currVal * 100 / total) + "%";

            if (displayCurrentValue) {
                counterValue += " (" + currVal + "/" + total + ")";
            }

            if (StringUtils.isNotEmpty(additionalInfo)) {
                counterValue += " " + additionalInfo;
            }

            // Updates the progress percentage
            System.out.print(StringUtils.repeat("\b", counterSize) + counterValue);
        }
    }
}

package com.go2super.service.trade;

public class TradeAuditUserHistory {

    private static final int MAX_ENTRIES = 100;
    private static final long TIME_THRESHOLD = 3 * 60 * 1000;

    public final String username;
    public final int guid;

    private int index;
    private int lastReport;
    private boolean canReport;
    private final int[] historyPage;
    private final long[] historyTime;

    public TradeAuditUserHistory(String username, int guid) {

        this.username = username;
        this.guid = guid;

        this.historyPage = new int[MAX_ENTRIES];
        this.historyTime = new long[MAX_ENTRIES];

        this.canReport = true;
    }

    public boolean addEntry(int page) {

        long currentTime = System.currentTimeMillis();
        long earliestTime = this.historyTime[this.index];

        this.index = ++this.index % MAX_ENTRIES;

        if (this.index == this.lastReport) {
            this.canReport = true;
        }

        this.historyPage[this.index] = page;
        this.historyTime[this.index] = currentTime;

        if (this.canReport) {
            this.lastReport = this.index;
            this.canReport = false;

            return currentTime - earliestTime < TIME_THRESHOLD;
        }

        return false;
    }

    public String getViewedPagesFormatted() {

        StringBuilder builder = new StringBuilder(MAX_ENTRIES * 3)
            .append(this.username)
            .append(" `ID: ")
            .append(this.guid)
            .append("` has viewed too many Auction pages too quickly! Pages viewed:```\n");
        for (int i = 1; i < MAX_ENTRIES; ++i) {
            builder.append(this.historyPage[(index + i) % MAX_ENTRIES])
                .append(", ");
        }
        builder.append(this.historyPage[index])
            .append("```");
        return builder.toString();
    }
}

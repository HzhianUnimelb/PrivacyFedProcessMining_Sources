package utilities;

import java.util.*;

public class EventLogFilter {

    private Map<String, Integer> frequencyMap;
    private double threshold;

    public EventLogFilter(double threshold) {
        this.threshold = threshold;
        this.frequencyMap = new HashMap<>();
    }

    public void addEventLog(String eventLog, int frequency) {
        frequencyMap.put(eventLog, frequency);
    }

    public void filterEventLogs() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(frequencyMap.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        int thresholdIndex = (int) Math.ceil(list.size() * (threshold / 100));
        System.out.println(thresholdIndex);
        list.subList(thresholdIndex, list.size()).clear();

        frequencyMap.clear();
        for (Map.Entry<String, Integer> entry : list) {
            frequencyMap.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Integer> getFilteredEventLogs() {
        return frequencyMap;
    }

    public static void main(String[] args) {
        EventLogFilter filter = new EventLogFilter(90); // filter out the 50% most frequent event logs

        filter.addEventLog("a,b,c", 10);
        filter.addEventLog("d,e,f", 5);
        filter.addEventLog("g,h,i", 8);
        filter.addEventLog("j,k,l", 12);
        filter.addEventLog("m,n,i", 3);
        filter.addEventLog("m,n,k", 3);
        filter.addEventLog("m,n,a", 3);
        filter.addEventLog("m,n,q", 3);
        filter.addEventLog("m,n,b", 3);
        filter.addEventLog("m,n,v", 3);
        filter.addEventLog("m,n,l", 3);
        filter.addEventLog("m,n,p", 3);
        filter.addEventLog("m,n,t", 3);
        filter.addEventLog("m,n,w", 3);
        filter.addEventLog("m,n,r", 3);
        filter.filterEventLogs();

        System.out.println(filter.getFilteredEventLogs());
    }
}
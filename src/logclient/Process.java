package logclient;


public class Process {
    private final String id;
    private final long startTime;
    private long endTime;

    public Process(final String id, final long startTime){
        this.id = id;
        this.startTime = startTime;
        endTime = -1;
    }

    public void setEndTime(final long endTime){
        this.endTime = endTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public long getStartTime(){
        return startTime;
    }

    public String getId(){
        return id;
    }
}

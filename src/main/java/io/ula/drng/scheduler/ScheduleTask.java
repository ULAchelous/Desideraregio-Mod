package io.ula.drng.scheduler;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ScheduleTask {
    private int delay;
    private int period;
    private String id;
    private boolean cancelled = false;
    private Runnable behaviour;
    Logger LOGGER = LogManager.getLogger("dr-ng/ServerSchedule:ScheduleTask");
    public ScheduleTask(String id ,Runnable run,int delay,int period){
        behaviour = run;
        this.id =id;
        this.delay = delay;
        this.period = period;
    }
    public ScheduleTask(String id ,Runnable run,int delay){
        this.id =id;
        behaviour = run;
        this.delay = delay;
        this.period = -1;
    }
    public String getId(){return this.id;}
    public void cancel(){cancelled = true;delay=0;}
    public boolean isCancelled(){return this.cancelled;}
    public void tickable(){
        if(this.isCancelled()) return;
        if(delay <= 0) {
            behaviour.run();
            if (period == -1) {
                this.cancel();
            } else {
                this.delay = period;
            }
        }else {
            delay--;
        }
    }
}

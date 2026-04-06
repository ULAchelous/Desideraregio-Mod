package io.ula.drng.scheduler;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class ServerScheduler {
    Logger LOGGER = LogManager.getLogger("dr-ng/ServerSchedule");
    public ServerScheduler(){
        LOGGER.info("Loaded ServerSchedule System");
    }
    private Map<String,ScheduleTask> tasks = new HashMap<>();
    public void runTask(ScheduleTask task){
        if(task.getId() == null || task.getId().isEmpty())
            return;
        LOGGER.info("Scheduled task \""+task.getId()+"\"");
        tasks.put(task.getId(),task);
    }
    public Map<String,ScheduleTask> getTasks(){
        return this.tasks;
    }
    public ScheduleTask getTask(String id){
        return tasks.get(id);
    }
    public void tickable(){
        for(Map.Entry<String,ScheduleTask> entry : tasks.entrySet()){
            ScheduleTask task = entry.getValue();
            if(task.isCancelled()){
                LOGGER.info("Removed task \""+task.getId()+"\"");
                tasks.remove(entry.getKey());
                continue;
            }
            task.tickable();
        }
    }
}

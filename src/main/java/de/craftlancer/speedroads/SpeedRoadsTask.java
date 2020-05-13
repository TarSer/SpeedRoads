package de.craftlancer.speedroads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class SpeedRoadsTask extends BukkitRunnable {
    private static final UUID MODIFIER_UUID = UUID.fromString("0d2d4303-c228-4075-9f94-00fa3036f40c");
    private static final String MODIFIER_NAME = "SpeedRoads";
    private static final AttributeModifier EMPTY_MODIFIER = new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, 0, Operation.ADD_SCALAR);
    private final SpeedRoads plugin;
    
    private Map<UUID, Double> currentSpeedMap = new HashMap<>();
    
    public SpeedRoadsTask(SpeedRoads plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(this::applyAttribute);
        if (!plugin.getAffectedEntities().isEmpty())
            Bukkit.getWorlds().forEach(a -> a.getEntitiesByClasses(plugin.getAffectedEntities().toArray(new Class[0])).forEach(this::applyAttribute));
    }
    
    private void applyAttribute(Entity a) {
        if (a.isValid() && a instanceof LivingEntity)
            applyAttribute((LivingEntity) a);
    }
    
    private void applyAttribute(LivingEntity a) {
        double currentSpeedMod = currentSpeedMap.getOrDefault(a.getUniqueId(), 0D);
        double targetSpeedMod = Double.NEGATIVE_INFINITY;
        AttributeInstance attrib = a.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        
        for (Road r : plugin.getRoads())
            if (r.getSpeedMod() > targetSpeedMod && r.isRoadBlock(a.getLocation().getBlock()))
                targetSpeedMod = r.getSpeedMod();
            
        if (targetSpeedMod == Double.NEGATIVE_INFINITY)
            targetSpeedMod = 0.0;
        
        attrib.removeModifier(EMPTY_MODIFIER);
        
        if (targetSpeedMod >= currentSpeedMod)
            currentSpeedMod = Math.min(currentSpeedMod + plugin.getStepSize(), targetSpeedMod);
        else
            currentSpeedMod = Math.max(currentSpeedMod - plugin.getStepSize(), 0);
        
        attrib.addModifier(new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, currentSpeedMod, Operation.ADD_SCALAR));
        
        currentSpeedMap.put(a.getUniqueId(), currentSpeedMod);
    }
}

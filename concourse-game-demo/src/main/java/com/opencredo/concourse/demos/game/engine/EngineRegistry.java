package com.opencredo.concourse.demos.game.engine;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public final class EngineRegistry {

    private final Map<String, Engine> registeredEngines = new HashMap<>();

    public Engine forRulesetVersion(String rulesetVersion) {
        Engine engine = registeredEngines.get(rulesetVersion);
        if (engine == null) {
            throw new IllegalArgumentException("No engine found for ruleset " + rulesetVersion);
        }
        return engine;
    }

    public void register(String rulesetVersion, Engine engine) {
        registeredEngines.put(rulesetVersion, engine);
    }
}

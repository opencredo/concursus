package com.opencredo.concursus.spring.commands.processing;

import com.opencredo.concursus.domain.commands.CommandType;
import com.opencredo.concursus.domain.commands.CommandTypeInfo;
import com.opencredo.concursus.domain.commands.CommandTypeMatcher;
import com.opencredo.concursus.mapping.commands.methods.reflection.CommandInterfaceInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ComponentScanningCommandTypeMatcher implements CommandTypeMatcher, ApplicationContextAware {

    private final Map<CommandType, CommandTypeInfo> commandTypeMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansWithAnnotation(CommandHandler.class).values().forEach(this::addTypeInfo);
    }

    private void addTypeInfo(Object handler) {
        Class<?> handlerInterface = CommandProcessorReflection.getHandlerInterface(handler);
        commandTypeMap.putAll(CommandInterfaceInfo.forInterface(handlerInterface).getTypeInfoMap());
    }

    @Override
    public Optional<CommandTypeInfo> match(CommandType commandType) {
        return Optional.ofNullable(commandTypeMap.get(commandType));
    }
}
